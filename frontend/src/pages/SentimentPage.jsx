import React, { useEffect, useState } from 'react';
import { useDataset } from '../context/DatasetContext';
import { analyticsService } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import StatCard from '../components/common/StatCard';
import {
  Smile,
  Frown,
  Meh,
  PieChart as PieIcon,
  Activity,
  Info,
  Calendar
} from 'lucide-react';
import {
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  BarChart,
  Bar
} from 'recharts';

const SENTIMENT_COLORS = {
  POSITIVE: '#10b981',
  NEUTRAL: '#64748b',
  NEGATIVE: '#f43f5e',
};

const EMOTION_COLORS = ['#3b82f6', '#ec4899', '#8b5cf6', '#f59e0b', '#10b981', '#6366f1', '#64748b'];

export default function SentimentPage() {
  const { selectedDataset } = useDataset();
  const [sentiment, setSentiment] = useState(null);
  const [emotions, setEmotions] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function loadData() {
      if (!selectedDataset) return;
      try {
        setLoading(true);
        const [sentRes, emoRes] = await Promise.all([
          analyticsService.getSentiment(selectedDataset.id),
          analyticsService.getEmotions(selectedDataset.id),
        ]);
        if (sentRes.data) setSentiment(sentRes.data);
        if (emoRes.data) setEmotions(emoRes.data);
      } catch (err) {
        console.error('Failed to load sentiment/emotions:', err);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, [selectedDataset]);

  if (!selectedDataset) {
    return <EmptyState title="No Dataset Selected" description="Select a dataset to examine RoBERTa sentiment and emotion analytics." />;
  }

  if (loading) {
    return <LoadingSpinner message="Extracting public emotion vectors..." />;
  }

  const pieData = sentiment?.distribution
    ? Object.entries(sentiment.distribution).map(([key, value]) => ({
        name: key,
        value,
      }))
    : [];

  const emotionData = emotions?.distribution
    ? Object.entries(emotions.distribution).map(([key, value]) => ({
        name: key.toUpperCase(),
        value,
      }))
    : [];

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
          Public Sentiment & Emotion Intelligence
        </h2>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Pretrained Twitter-RoBERTa 3-Class Sentiment & DistilRoBERTa 7-Class Emotion classification pipelines.
        </p>
      </div>

      {/* Model Spec Alert */}
      <div className="p-4 bg-brand-50/70 dark:bg-brand-950/40 rounded-xl border border-brand-200 dark:border-brand-800 flex items-start gap-3">
        <Info className="w-5 h-5 text-brand-600 dark:text-brand-400 shrink-0 mt-0.5" />
        <div className="text-xs text-slate-600 dark:text-slate-300 space-y-1">
          <p className="font-semibold text-slate-900 dark:text-white">
            Pretrained Model Specifications & Academic Scope:
          </p>
          <p>
            Sentiment inference powered by <code className="text-brand-700 dark:text-brand-300">cardiffnlp/twitter-roberta-base-sentiment-latest</code> and emotion classification by <code className="text-brand-700 dark:text-brand-300">j-hartmann/emotion-english-distilroberta-base</code>. Models operate primarily on English semantics; Hindi-English code-mixed posts are handled via lexical normalization.
          </p>
        </div>
      </div>

      {/* Quick Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <StatCard
          title="Positive Posts"
          value={sentiment?.distribution?.POSITIVE || 0}
          icon={Smile}
          color="emerald"
        />
        <StatCard
          title="Negative Posts"
          value={sentiment?.distribution?.NEGATIVE || 0}
          icon={Frown}
          color="rose"
        />
        <StatCard
          title="Neutral Posts"
          value={sentiment?.distribution?.NEUTRAL || 0}
          icon={Meh}
          color="brand"
        />
        <StatCard
          title="Net Polarity Score"
          value={sentiment?.netSentimentScore !== undefined ? sentiment.netSentimentScore.toFixed(3) : '0.000'}
          icon={Activity}
          color="purple"
          subtitle="Range: -1.0 (Bearish) to +1.0 (Bullish)"
        />
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Sentiment Distribution Pie */}
        <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
          <h3 className="text-sm font-bold text-slate-900 dark:text-white">
            Sentiment Class Proportions
          </h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Normalized across analyzed unique and duplicate post corpora
          </p>
          <div className="h-64 mt-4">
            {pieData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} innerRadius={60} outerRadius={90} paddingAngle={4} dataKey="value">
                    {pieData.map((entry) => (
                      <Cell key={`cell-${entry.name}`} fill={SENTIMENT_COLORS[entry.name] || '#94a3b8'} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', borderRadius: '8px', color: '#fff', fontSize: '12px' }} />
                  <Legend verticalAlign="bottom" height={36} iconType="circle" />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-full text-xs text-slate-400">No data</div>
            )}
          </div>
        </div>

        {/* Emotion Distribution Bar */}
        <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
          <h3 className="text-sm font-bold text-slate-900 dark:text-white">
            7-Class Emotion Spectrum
          </h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
            Dominant emotion: <strong className="text-brand-600 dark:text-brand-400">{emotions?.primaryEmotion || 'NEUTRAL'}</strong> ({emotions?.primaryEmotionPercentage || 0}%)
          </p>
          <div className="h-64 mt-4">
            {emotionData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={emotionData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.2} />
                  <XAxis dataKey="name" stroke="#94a3b8" fontSize={10} tickLine={false} />
                  <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                  <Tooltip contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', borderRadius: '8px', color: '#fff', fontSize: '12px' }} />
                  <Bar dataKey="value" radius={[4, 4, 0, 0]} name="Count">
                    {emotionData.map((entry, index) => (
                      <Cell key={`cell-emo-${index}`} fill={EMOTION_COLORS[index % EMOTION_COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-full text-xs text-slate-400">No data</div>
            )}
          </div>
        </div>
      </div>

      {/* Sentiment Over Time Line Chart */}
      <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
        <h3 className="text-sm font-bold text-slate-900 dark:text-white">
          Sentiment Polarity Timeline
        </h3>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
          Temporal sentiment shifts capture emergent events and public opinion turning points
        </p>
        <div className="h-72 mt-4">
          {sentiment?.overTime && sentiment.overTime.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={sentiment.overTime}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.2} />
                <XAxis dataKey="date" stroke="#94a3b8" fontSize={11} tickLine={false} />
                <YAxis stroke="#94a3b8" fontSize={11} tickLine={false} />
                <Tooltip contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', borderRadius: '8px', color: '#fff', fontSize: '12px' }} />
                <Legend verticalAlign="bottom" height={36} iconType="circle" />
                <Line type="monotone" dataKey="positive" stroke="#10b981" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} name="Positive" />
                <Line type="monotone" dataKey="negative" stroke="#f43f5e" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} name="Negative" />
                <Line type="monotone" dataKey="neutral" stroke="#64748b" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} name="Neutral" />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-full text-xs text-slate-400">
              No temporal series available for current dataset
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

