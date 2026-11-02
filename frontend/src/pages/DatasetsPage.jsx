import React, { useState, useRef } from 'react';
import { useDataset } from '../context/DatasetContext';
import { datasetService } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import {
  UploadCloud,
  FileText,
  Trash2,
  Play,
  CheckCircle2,
  Clock,
  AlertCircle,
  Settings2,
  Table,
  Check,
  X
} from 'lucide-react';

export default function DatasetsPage() {
  const { datasets, selectedDataset, selectDataset, fetchDatasets, triggerAnalysis } = useDataset();
  const fileInputRef = useRef(null);

  const [uploadFile, setUploadFile] = useState(null);
  const [datasetName, setDatasetName] = useState('');
  const [datasetDescription, setDatasetDescription] = useState('');
  const [previewData, setPreviewData] = useState(null);
  const [columnMappings, setColumnMappings] = useState({});
  const [previewing, setPreviewing] = useState(false);
  const [importing, setImporting] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Analysis Config Modal state
  const [showConfigModal, setShowConfigModal] = useState(false);
  const [targetDatasetId, setTargetDatasetId] = useState(null);
  const [config, setConfig] = useState({
    duplicateSimilarityThreshold: 0.85,
    minTopicSize: 3,
    trendAlpha: 0.40,
    trendBeta: 0.30,
    trendGamma: 0.20,
    trendDelta: 0.10,
    windowSizeHours: 24,
  });

  const handleFileSelect = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploadFile(file);
    setDatasetName(file.name.replace(/\.[^/.]+$/, ''));
    setError('');
    setPreviewing(true);

    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await datasetService.preview(formData);
      if (res.data) {
        setPreviewData(res.data);
        setColumnMappings(res.data.suggestedColumnMappings || {});
      }
    } catch (err) {
      setError(err.message || 'Failed to preview file');
      setUploadFile(null);
    } finally {
      setPreviewing(false);
    }
  };

  const handleImport = async (e) => {
    e.preventDefault();
    if (!uploadFile) return;

    setImporting(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('file', uploadFile);
      formData.append('name', datasetName);
      formData.append('description', datasetDescription);
      formData.append('columnMapping', JSON.stringify(columnMappings));

      const res = await datasetService.upload(formData);
      setSuccessMessage('Dataset imported successfully!');
      setUploadFile(null);
      setPreviewData(null);
      setDatasetName('');
      setDatasetDescription('');
      await fetchDatasets();
      if (res.data) selectDataset(res.data);
    } catch (err) {
      setError(err.message || 'Import failed');
    } finally {
      setImporting(false);
    }
  };

  const handleDelete = async (id, e) => {
    e.stopPropagation();
    if (!window.confirm('Are you sure you want to delete this dataset and all associated analysis records?')) return;
    try {
      await datasetService.delete(id);
      await fetchDatasets();
    } catch (err) {
      alert(err.message || 'Failed to delete dataset');
    }
  };

  const openAnalysisModal = (id, e) => {
    e.stopPropagation();
    setTargetDatasetId(id);
    setShowConfigModal(true);
  };

  const handleStartAnalysis = async () => {
    if (!targetDatasetId) return;
    try {
      await triggerAnalysis(targetDatasetId, config);
      setShowConfigModal(false);
      setSuccessMessage('AI Analytics pipeline triggered successfully in the background!');
    } catch (err) {
      alert(err.message || 'Failed to start analysis');
    }
  };

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
          Dataset Management & Ingestion
        </h2>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Import social media posts in CSV or JSON format, configure custom schema column mappings, and trigger the AI analytics engine.
        </p>
      </div>

      {error && (
        <div className="p-4 bg-rose-500/10 border border-rose-500/20 rounded-xl text-rose-600 dark:text-rose-400 text-xs font-medium flex items-center justify-between">
          <span>{error}</span>
          <button onClick={() => setError('')}><X className="w-4 h-4" /></button>
        </div>
      )}

      {successMessage && (
        <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-emerald-600 dark:text-emerald-400 text-xs font-medium flex items-center justify-between">
          <span className="flex items-center gap-2"><Check className="w-4 h-4" />{successMessage}</span>
          <button onClick={() => setSuccessMessage('')}><X className="w-4 h-4" /></button>
        </div>
      )}

      {/* Upload & Preview Section */}
      <div className="bg-white dark:bg-slate-800 p-6 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
        <h3 className="text-sm font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
          <UploadCloud className="w-4 h-4 text-brand-600" />
          Upload New Dataset
        </h3>

        {!uploadFile ? (
          <div
            onClick={() => fileInputRef.current?.click()}
            className="border-2 border-dashed border-slate-300 dark:border-slate-600 hover:border-brand-500 rounded-2xl p-8 text-center cursor-pointer transition-colors bg-slate-50/50 dark:bg-slate-900/30"
          >
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileSelect}
              accept=".csv,.json"
              className="hidden"
            />
            <div className="p-3 bg-brand-50 dark:bg-brand-950/50 text-brand-600 rounded-full w-12 h-12 mx-auto flex items-center justify-center mb-3">
              <UploadCloud className="w-6 h-6" />
            </div>
            <p className="text-sm font-semibold text-slate-700 dark:text-slate-200">
              Click or drag & drop CSV or JSON file here
            </p>
            <p className="text-xs text-slate-400 mt-1">
              Supports standard posts with columns for text, author_id, post_id, created_at, likes, etc.
            </p>
          </div>
        ) : previewing ? (
          <LoadingSpinner message="Scanning schema and parsing sample records..." />
        ) : (
          <form onSubmit={handleImport} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-300 mb-1.5">
                  Dataset Name
                </label>
                <input
                  type="text"
                  required
                  value={datasetName}
                  onChange={(e) => setDatasetName(e.target.value)}
                  className="w-full px-3.5 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg text-sm text-slate-900 dark:text-white focus:ring-2 focus:ring-brand-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold uppercase tracking-wider text-slate-600 dark:text-slate-300 mb-1.5">
                  Description
                </label>
                <input
                  type="text"
                  value={datasetDescription}
                  onChange={(e) => setDatasetDescription(e.target.value)}
                  placeholder="e.g. Electric Vehicle Twitter sentiment & trends"
                  className="w-full px-3.5 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg text-sm text-slate-900 dark:text-white focus:ring-2 focus:ring-brand-500"
                />
              </div>
            </div>

            {/* Column Mapping Section */}
            {previewData && (
              <div className="border border-slate-200 dark:border-slate-700 rounded-xl p-4 bg-slate-50/50 dark:bg-slate-900/40 space-y-4">
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
                      Schema Column Mapping
                    </h4>
                    <p className="text-[11px] text-slate-500 dark:text-slate-400">
                      Detected {previewData.totalDetectedRows} records. Map required fields to your dataset columns.
                    </p>
                  </div>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 text-xs">
                  {['text', 'author_id', 'post_id', 'platform', 'created_at', 'like_count', 'reply_count', 'share_count', 'reply_to_post_id'].map((field) => (
                    <div key={field} className="space-y-1">
                      <label className="font-semibold text-slate-600 dark:text-slate-400">
                        {field} {field === 'text' && <span className="text-rose-500">*</span>}
                      </label>
                      <select
                        value={columnMappings[field] || ''}
                        onChange={(e) => setColumnMappings({ ...columnMappings, [field]: e.target.value })}
                        className="w-full px-2 py-1.5 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-md text-xs"
                      >
                        <option value="">-- Auto/None --</option>
                        {previewData.headers?.map((h) => (
                          <option key={h} value={h}>{h}</option>
                        ))}
                      </select>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Sample Preview Table */}
            {previewData && previewData.sampleRows && previewData.sampleRows.length > 0 && (
              <div className="border border-slate-200 dark:border-slate-700 rounded-xl overflow-hidden">
                <div className="bg-slate-100 dark:bg-slate-700/60 px-4 py-2 border-b border-slate-200 dark:border-slate-700 flex items-center gap-2">
                  <Table className="w-4 h-4 text-slate-500" />
                  <span className="text-xs font-bold text-slate-700 dark:text-slate-300">Sample Records Preview</span>
                </div>
                <div className="overflow-x-auto max-h-48">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-50 dark:bg-slate-800 text-slate-500 border-b border-slate-200 dark:border-slate-700">
                      <tr>
                        {previewData.headers?.map((h) => (
                          <th key={h} className="py-2 px-3 whitespace-nowrap">{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                      {previewData.sampleRows.map((row, idx) => (
                        <tr key={idx} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                          {previewData.headers?.map((h) => (
                            <td key={h} className="py-2 px-3 whitespace-nowrap text-slate-600 dark:text-slate-300 max-w-xs truncate">
                              {row[h]}
                            </td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            <div className="flex items-center gap-3">
              <button
                type="submit"
                disabled={importing}
                className="px-5 py-2.5 bg-brand-600 hover:bg-brand-500 text-white rounded-lg text-xs font-semibold shadow-sm transition-all disabled:opacity-50"
              >
                {importing ? 'Importing Dataset...' : 'Confirm & Ingest Posts'}
              </button>
              <button
                type="button"
                onClick={() => {
                  setUploadFile(null);
                  setPreviewData(null);
                }}
                className="px-4 py-2.5 bg-slate-100 hover:bg-slate-200 dark:bg-slate-700 dark:hover:bg-slate-600 text-slate-700 dark:text-slate-200 rounded-lg text-xs font-semibold transition-all"
              >
                Cancel
              </button>
            </div>
          </form>
        )}
      </div>

      {/* Available Datasets Table */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden">
        <div className="p-5 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Imported Datasets
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Select an active dataset or launch AI analytics
            </p>
          </div>
          <span className="text-xs font-semibold px-2.5 py-1 bg-slate-100 dark:bg-slate-700 rounded-full text-slate-600 dark:text-slate-300">
            {datasets.length} Total
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-700/50 text-slate-500 dark:text-slate-400 uppercase tracking-wider text-[10px]">
              <tr>
                <th className="py-3 px-5">Dataset Name</th>
                <th className="py-3 px-4">Format</th>
                <th className="py-3 px-4">Total Posts</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">Uploaded At</th>
                <th className="py-3 px-5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/60">
              {datasets.map((d) => {
                const isSelected = selectedDataset?.id === d.id;
                return (
                  <tr
                    key={d.id}
                    onClick={() => selectDataset(d)}
                    className={`cursor-pointer transition-colors ${
                      isSelected
                        ? 'bg-brand-50/70 dark:bg-brand-950/40'
                        : 'hover:bg-slate-50/80 dark:hover:bg-slate-700/40'
                    }`}
                  >
                    <td className="py-3.5 px-5 font-semibold text-slate-900 dark:text-white">
                      <div className="flex items-center gap-2">
                        <FileText className="w-4 h-4 text-brand-600" />
                        <span>{d.name}</span>
                        {isSelected && (
                          <span className="text-[10px] bg-brand-600 text-white px-2 py-0.5 rounded-full font-bold">
                            Active
                          </span>
                        )}
                      </div>
                      {d.description && (
                        <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5 font-normal">
                          {d.description}
                        </p>
                      )}
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-600 dark:text-slate-300">
                      {d.fileFormat}
                    </td>
                    <td className="py-3.5 px-4 font-bold text-slate-800 dark:text-slate-200">
                      {d.totalRecords?.toLocaleString()}
                    </td>
                    <td className="py-3.5 px-4">
                      {d.analysisStatus === 'COMPLETED' ? (
                        <span className="inline-flex items-center gap-1 text-emerald-600 dark:text-emerald-400 font-medium">
                          <CheckCircle2 className="w-3.5 h-3.5" /> Ready
                        </span>
                      ) : d.analysisStatus === 'PROCESSING' ? (
                        <span className="inline-flex items-center gap-1 text-amber-600 dark:text-amber-400 font-medium animate-pulse">
                          <Clock className="w-3.5 h-3.5" /> Processing...
                        </span>
                      ) : (
                        <span className="text-slate-500 dark:text-slate-400 font-medium">
                          Pending Analysis
                        </span>
                      )}
                    </td>
                    <td className="py-3.5 px-4 text-slate-500 dark:text-slate-400">
                      {new Date(d.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-3.5 px-5 text-right space-x-2">
                      <button
                        onClick={(e) => openAnalysisModal(d.id, e)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 bg-brand-600 hover:bg-brand-500 text-white rounded-md text-xs font-semibold shadow-xs transition-colors"
                        title="Run AI Analysis"
                      >
                        <Play className="w-3.5 h-3.5 fill-current" />
                        Run AI
                      </button>
                      <button
                        onClick={(e) => handleDelete(d.id, e)}
                        className="p-1.5 text-slate-400 hover:text-rose-600 rounded-md hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-colors"
                        title="Delete Dataset"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                );
              })}
              {datasets.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-center py-8 text-slate-400">
                    No datasets available. Upload a dataset using the form above.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Analysis Config Modal */}
      {showConfigModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="bg-white dark:bg-slate-800 rounded-2xl max-w-lg w-full p-6 border border-slate-200 dark:border-slate-700 shadow-2xl space-y-5">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Settings2 className="w-5 h-5 text-brand-600" />
                <h3 className="text-base font-bold text-slate-900 dark:text-white">
                  Configure AI Analytics Pipeline
                </h3>
              </div>
              <button onClick={() => setShowConfigModal(false)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-500 dark:text-slate-400">
              Tune parameters for duplicate detection, BERTopic extraction, and multi-signal noise-resilient trend scoring.
            </p>

            <div className="space-y-4 text-xs">
              <div>
                <label className="font-semibold text-slate-700 dark:text-slate-300 flex justify-between">
                  <span>Duplicate Cosine Threshold:</span>
                  <span className="font-mono text-brand-600">{config.duplicateSimilarityThreshold}</span>
                </label>
                <input
                  type="range"
                  min="0.5"
                  max="0.99"
                  step="0.01"
                  value={config.duplicateSimilarityThreshold}
                  onChange={(e) => setConfig({ ...config, duplicateSimilarityThreshold: parseFloat(e.target.value) })}
                  className="w-full mt-1.5"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="font-semibold text-slate-700 dark:text-slate-300">Min Topic Cluster Size</label>
                  <input
                    type="number"
                    min="2"
                    max="50"
                    value={config.minTopicSize}
                    onChange={(e) => setConfig({ ...config, minTopicSize: parseInt(e.target.value) })}
                    className="w-full mt-1 px-3 py-1.5 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg"
                  />
                </div>
                <div>
                  <label className="font-semibold text-slate-700 dark:text-slate-300">Window Size (Hours)</label>
                  <input
                    type="number"
                    min="1"
                    max="168"
                    value={config.windowSizeHours}
                    onChange={(e) => setConfig({ ...config, windowSizeHours: parseInt(e.target.value) })}
                    className="w-full mt-1 px-3 py-1.5 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg"
                  />
                </div>
              </div>

              <div className="p-3 bg-slate-50 dark:bg-slate-900/50 rounded-xl border border-slate-200 dark:border-slate-700 space-y-2">
                <span className="font-bold text-[11px] uppercase tracking-wider text-slate-600 dark:text-slate-400">
                  Trend Score Weight Distribution (Sum = 1.0)
                </span>
                <div className="grid grid-cols-2 gap-2 text-[11px]">
                  <div>α (Anomaly Z-Score): <strong>{config.trendAlpha}</strong></div>
                  <div>β (Growth Rate): <strong>{config.trendBeta}</strong></div>
                  <div>γ (Sentiment Shift): <strong>{config.trendGamma}</strong></div>
                  <div>δ (Engagement): <strong>{config.trendDelta}</strong></div>
                </div>
              </div>
            </div>

            <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-200 dark:border-slate-700">
              <button
                onClick={() => setShowConfigModal(false)}
                className="px-4 py-2 text-xs font-semibold text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-700 rounded-lg"
              >
                Cancel
              </button>
              <button
                onClick={handleStartAnalysis}
                className="px-5 py-2 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 rounded-lg shadow-sm"
              >
                Start AI Analysis
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
