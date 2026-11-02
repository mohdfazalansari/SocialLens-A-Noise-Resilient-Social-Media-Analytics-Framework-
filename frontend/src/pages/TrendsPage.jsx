import React, { useEffect, useState } from 'react';
import { useDataset } from '../context/DatasetContext';
import { trendService } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import {
  TrendingUp,
  AlertTriangle,
  Info,
  ShieldCheck,
  Sparkles,
  Layers,
  ArrowUpRight,
  Filter,
  CheckCircle,
  Eye,
  X
} from 'lucide-react';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend
} from 'recharts';

export default function TrendsPage() {
  const { selectedDataset } = useDataset();
  const [trends, setTrends] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedMethod, setSelectedMethod] = useState('PROPOSED_NOISE_RESILIENT');
  const [activeAlertModal, setActiveAlertModal] = useState(null);

  useEffect(() => {
    async function loadTrends() {
      if (!selectedDataset) return;
      try {
        setLoading(true);
        const res = await trendService.getTrends(selectedDataset.id, selectedMethod);
        if (res.data) setTrends(res.data);
      } catch (err) {
        console.error('Failed to load trends:', err);
      } finally {
        setLoading(false);
      }
    }
    loadTrends();
  }, [selectedDataset, selectedMethod]);

  if (!selectedDataset) {
    return <EmptyState title="No Dataset Selected" description="Select a dataset to run noise-resilient trend detection and anomaly alerting." />;
  }

  if (loading && !trends) {
    return <LoadingSpinner message="Calculating rolling anomaly Z-scores and sentiment shift vectors..." />;
  }

  // Format snapshot data for trend line visualization
  const snapshotTimeline = trends?.snapshots?.map((s) => ({
    time: new Date(s.windowStart).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit' }),
    topic: s.topicName,
    rawPosts: s.rawPostCount,
    uniquePosts: s.uniquePostCount,
    trendScore: s.trendScore,
    zScore: s.zScore,
  })) || [];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
              Noise-Resilient Trend Detection Engine
            </h2>
            <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-brand-100 text-brand-700 dark:bg-brand-950 dark:text-brand-300 border border-brand-200 dark:border-brand-800">
              Research Core
            </span>
          </div>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Investigating whether duplicate damping, temporal anomaly detection, and sentiment shifts improve emerging trend alerts over simple post-volume baselines.
          </p>
        </div>

        {/* Method Switcher */}
        <div className="flex items-center bg-slate-100 dark:bg-slate-800 p-1 rounded-xl border border-slate-200 dark:border-slate-700 self-start md:self-auto">
          {[
            { id: 'BASELINE_VOLUME', label: 'Baseline A (Volume)' },
            { id: 'BASELINE_GROWTH', label: 'Baseline B (Growth)' },
            { id: 'PROPOSED_NOISE_RESILIENT', label: 'Proposed (Multi-Signal)', recommended: true },
          ].map((m) => (
            <button
              key={m.id}
              onClick={() => setSelectedMethod(m.id)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                selectedMethod === m.id
                  ? 'bg-white dark:bg-slate-700 text-brand-600 dark:text-brand-300 shadow-xs'
                  : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
              }`}
            >
              {m.label}
            </button>
          ))}
        </div>
      </div>

      {/* Mathematical Formulation Explainer Banner */}
      <div className="p-4 bg-slate-50 dark:bg-slate-800/80 rounded-xl border border-slate-200 dark:border-slate-700 space-y-2">
        <div className="flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-brand-600" />
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-900 dark:text-white">
            Current Scoring Formulation: {selectedMethod}
          </h3>
        </div>
        <p className="text-xs text-slate-600 dark:text-slate-300 leading-relaxed font-mono">
          {selectedMethod === 'PROPOSED_NOISE_RESILIENT' && (
            <span>
              Score(k, Wt) = α · Sigmoid(Z(V_uniq)) + β · GrowthRate(V_uniq) + γ · |Δ Polarity| + δ · log(1 + Engagement)
              <br />
              <span className="text-[11px] font-sans text-slate-500">
                Filters out duplicate bot cascades, isolates temporal surges relative to rolling baseline (μ, σ), and detects sentiment inflection points.
              </span>
            </span>
          )}
          {selectedMethod === 'BASELINE_VOLUME' && (
            <span>
              Score(k, Wt) = Total Post Count(k, Wt)
              <br />
              <span className="text-[11px] font-sans text-slate-500">
                Naive post count: susceptible to spam campaigns, bot loops, and duplicate retweets.
              </span>
            </span>
          )}
          {selectedMethod === 'BASELINE_GROWTH' && (
            <span>
              Score(k, Wt) = [ (Vt - Vt-1) / Vt-1 ] · log2(1 + Engagement)
              <br />
              <span className="text-[11px] font-sans text-slate-500">
                Consecutive window growth percentage: prone to false alerts on low absolute volumes (e.g. 1 post to 4 posts = +300% growth).
              </span>
            </span>
          )}
        </p>
      </div>

      {/* Temporal Anomaly Trend Chart */}
      <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
        <h3 className="text-sm font-bold text-slate-900 dark:text-white">
          Unique Volume & Anomaly Trajectory
        </h3>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
          Comparing raw post count against duplicate-dampened unique volume across time windows
        </p>
        <div className="h-72 mt-4">
          {snapshotTimeline.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={snapshotTimeline}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.2} />
                <XAxis dataKey="time" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#1e293b',
                    borderColor: '#334155',
                    borderRadius: '8px',
                    color: '#fff',
                    fontSize: '12px'
                  }}
                />
                <Legend verticalAlign="bottom" height={36} iconType="circle" />
                <Line type="monotone" dataKey="rawPosts" stroke="#94a3b8" strokeDasharray="4 4" strokeWidth={2} name="Raw Volume (Baseline)" />
                <Line type="monotone" dataKey="uniquePosts" stroke="#0c87eb" strokeWidth={2.5} dot={false} name="Deduplicated Volume" />
                <Line type="monotone" dataKey="zScore" stroke="#f59e0b" strokeWidth={2} dot={false} name="Z-Score Anomaly" />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-full text-xs text-slate-400">
              Temporal snapshots not yet calculated for current dataset
            </div>
          )}
        </div>
      </div>

      {/* Trend Alerts Table */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden">
        <div className="p-5 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-amber-500" />
              Evidence-Backed Trend Alerts ({trends?.alerts?.length || 0})
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Verified anomalies meeting statistical significance thresholds
            </p>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-700/50 text-slate-500 dark:text-slate-400 uppercase tracking-wider text-[10px]">
              <tr>
                <th className="py-3 px-5">Topic</th>
                <th className="py-3 px-4">Level</th>
                <th className="py-3 px-4">Classification</th>
                <th className="py-3 px-4">Trend Score</th>
                <th className="py-3 px-4">Current / Baseline</th>
                <th className="py-3 px-4">Spam Filtered</th>
                <th className="py-3 px-4">Sentiment Shift</th>
                <th className="py-3 px-5 text-right">Evidence</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/60">
              {trends?.alerts?.map((alert) => (
                <tr key={alert.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-700/30 transition-colors">
                  <td className="py-3.5 px-5 font-bold text-slate-900 dark:text-white">
                    {alert.topicName}
                  </td>
                  <td className="py-3.5 px-4">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        alert.alertLevel === 'HIGH'
                          ? 'bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-400'
                          : 'bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-400'
                      }`}
                    >
                      {alert.alertLevel}
                    </span>
                  </td>
                  <td className="py-3.5 px-4 font-semibold text-brand-600 dark:text-brand-400">
                    {alert.classification}
                  </td>
                  <td className="py-3.5 px-4 font-bold text-slate-900 dark:text-white">
                    {Number(alert.trendScore).toFixed(4)}
                  </td>
                  <td className="py-3.5 px-4 font-medium text-slate-700 dark:text-slate-300">
                    {alert.currentVolume} / {Number(alert.baselineVolume).toFixed(1)} posts
                  </td>
                  <td className="py-3.5 px-4">
                    <span className="font-semibold text-amber-600 dark:text-amber-400">
                      {Math.round((alert.duplicateRatio || 0) * 100)}%
                    </span>
                  </td>
                  <td className="py-3.5 px-4 font-semibold text-purple-600 dark:text-purple-400">
                    {Number(alert.sentimentShift).toFixed(3)}
                  </td>
                  <td className="py-3.5 px-5 text-right">
                    <button
                      onClick={() => setActiveAlertModal(alert)}
                      className="inline-flex items-center gap-1 px-3 py-1 bg-slate-100 hover:bg-slate-200 dark:bg-slate-700 dark:hover:bg-slate-600 text-slate-700 dark:text-slate-200 rounded-md text-xs font-semibold transition-colors"
                    >
                      <Eye className="w-3.5 h-3.5" />
                      Inspect Why
                    </button>
                  </td>
                </tr>
              ))}
              {(!trends?.alerts || trends.alerts.length === 0) && (
                <tr>
                  <td colSpan={8} className="text-center py-8 text-slate-400">
                    No active trend alerts triggered under current thresholds
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Alert Reason & Post Evidence Modal */}
      {activeAlertModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4">
          <div className="bg-white dark:bg-slate-800 rounded-2xl max-w-xl w-full p-6 border border-slate-200 dark:border-slate-700 shadow-2xl space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider text-brand-600 dark:text-brand-400">
                  Trend Alert Evidence Dossier
                </span>
                <h3 className="text-lg font-bold text-slate-900 dark:text-white">
                  {activeAlertModal.topicName}
                </h3>
              </div>
              <button onClick={() => setActiveAlertModal(null)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-4 bg-slate-50 dark:bg-slate-900/60 rounded-xl border border-slate-200 dark:border-slate-700 text-xs space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-slate-500">Alert Classification:</span>
                <span className="font-bold text-brand-600">{activeAlertModal.classification} ({activeAlertModal.alertLevel} Alert)</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-500">Duplicate Spam Dampening:</span>
                <span className="font-bold text-amber-500">{Math.round((activeAlertModal.duplicateRatio || 0) * 100)}% filtered duplicates</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-500">Sentiment Shift (Δ Polarity):</span>
                <span className="font-bold text-purple-500">{Number(activeAlertModal.sentimentShift).toFixed(3)}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-500">Unique Volume Surge:</span>
                <span className="font-bold text-emerald-500">+{Number(activeAlertModal.growthPercentage).toFixed(1)}%</span>
              </div>
            </div>

            <div className="space-y-1.5">
              <span className="text-xs font-bold text-slate-700 dark:text-slate-300">
                Mathematical Justification
              </span>
              <p className="text-xs text-slate-600 dark:text-slate-400 leading-relaxed bg-brand-50/50 dark:bg-brand-950/30 p-3 rounded-lg border border-brand-100 dark:border-brand-900">
                {activeAlertModal.alertReason}
              </p>
            </div>

            <div className="space-y-1.5">
              <span className="text-xs font-bold text-slate-700 dark:text-slate-300">
                Supporting Post Record IDs ({activeAlertModal.supportingPostIds?.length || 0})
              </span>
              <div className="p-2.5 bg-slate-100 dark:bg-slate-900 rounded-lg text-xs font-mono text-slate-700 dark:text-slate-300 flex flex-wrap gap-1.5 max-h-32 overflow-y-auto">
                {activeAlertModal.supportingPostIds?.map((pid) => (
                  <span key={pid} className="px-2 py-0.5 bg-white dark:bg-slate-800 rounded border border-slate-200 dark:border-slate-700">
                    #{pid}
                  </span>
                ))}
              </div>
            </div>

            <div className="pt-2 flex justify-end">
              <button
                onClick={() => setActiveAlertModal(null)}
                className="px-4 py-2 bg-slate-800 dark:bg-slate-700 text-white rounded-lg text-xs font-semibold"
              >
                Close Dossier
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
