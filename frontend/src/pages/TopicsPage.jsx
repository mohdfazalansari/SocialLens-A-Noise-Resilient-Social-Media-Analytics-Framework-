import React, { useEffect, useState } from 'react';
import { useDataset } from '../context/DatasetContext';
import { analyticsService } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { Hash, Sparkles, Layers, BarChart2 } from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  Cell
} from 'recharts';

export default function TopicsPage() {
  const { selectedDataset } = useDataset();
  const [topics, setTopics] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedTopic, setSelectedTopic] = useState(null);

  useEffect(() => {
    async function loadTopics() {
      if (!selectedDataset) return;
      try {
        setLoading(true);
        const res = await analyticsService.getTopics(selectedDataset.id);
        if (res.data) {
          setTopics(res.data);
          if (res.data.length > 0) setSelectedTopic(res.data[0]);
        }
      } catch (err) {
        console.error('Failed to load topics:', err);
      } finally {
        setLoading(false);
      }
    }
    loadTopics();
  }, [selectedDataset]);

  if (!selectedDataset) {
    return <EmptyState title="No Dataset Selected" description="Select a dataset to view automatically discovered semantic BERTopics." />;
  }

  if (loading) {
    return <LoadingSpinner message="Clustering semantic embeddings via Sentence-Transformers..." />;
  }

  const chartData = topics.map((t) => ({
    name: t.name,
    count: t.postCount,
    coherence: Math.round((t.representativeCoherence || 0.7) * 100),
  }));

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
          BERTopic Semantic Topic Discovery
        </h2>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Unsupervised semantic clustering using Sentence-Transformers (<code className="text-brand-600 dark:text-brand-400">all-MiniLM-L6-v2</code>) and class-based TF-IDF keyword extraction ({topics.length} discovered clusters).
        </p>
      </div>

      {topics.length === 0 ? (
        <EmptyState
          title="No Topics Discovered"
          description="The AI pipeline has not yet extracted semantic clusters for this dataset. Please run the AI pipeline from the Datasets page."
          actionText="Go to Datasets"
          actionLink="/datasets"
        />
      ) : (
        <>
          {/* Topic Volume Chart */}
          <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Topic Volume & Representation
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Number of posts assigned to each discovered cluster
            </p>
            <div className="h-64 mt-4">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.2} />
                  <XAxis dataKey="name" stroke="#94a3b8" fontSize={11} tickLine={false} />
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
                  <Bar dataKey="count" fill="#0c87eb" radius={[4, 4, 0, 0]} name="Posts" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Discovered Topic Cards Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {topics.map((t, idx) => {
              const isSelected = selectedTopic?.id === t.id;
              return (
                <div
                  key={t.id || idx}
                  onClick={() => setSelectedTopic(t)}
                  className={`p-5 rounded-xl border transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-brand-50/60 dark:bg-brand-950/40 border-brand-500 ring-2 ring-brand-500/20'
                      : 'bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-700 hover:shadow-md'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-brand-600 dark:text-brand-400 flex items-center gap-1.5">
                      <Hash className="w-3.5 h-3.5" />
                      Topic #{idx + 1}
                    </span>
                    <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-200">
                      {t.postCount} posts
                    </span>
                  </div>

                  <h4 className="mt-2 text-base font-bold text-slate-900 dark:text-white">
                    {t.name}
                  </h4>

                  <div className="mt-3">
                    <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
                      Top c-TF-IDF Keywords
                    </span>
                    <div className="mt-1 flex flex-wrap gap-1.5">
                      {t.topKeywords?.map((kw, kwIdx) => (
                        <span
                          key={kwIdx}
                          className="px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300 text-[11px] font-medium"
                        >
                          {kw.trim()}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-700/60 flex items-center justify-between text-xs">
                    <span className="text-slate-500 dark:text-slate-400">Semantic Coherence</span>
                    <span className="font-bold text-slate-800 dark:text-slate-200">
                      {Math.round((t.representativeCoherence || 0.7) * 100)}%
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </>
      )}
    </div>
  );
}

