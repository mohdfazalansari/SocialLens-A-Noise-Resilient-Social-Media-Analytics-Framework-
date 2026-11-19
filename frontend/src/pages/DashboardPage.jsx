import React, { useEffect, useState } from 'react';
import { useDataset } from '../context/DatasetContext';
import { analyticsService, trendService } from '../services/api';
import StatCard from '../components/common/StatCard';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import {
  MessageSquare,
  CopyCheck,
  ThumbsUp,
  ThumbsDown,
  MinusCircle,
  Hash,
  TrendingUp,
  AlertTriangle,
  ArrowUpRight,
  ShieldAlert
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
import { useNavigate } from 'react-router-dom';

const SENTIMENT_COLORS = {
  POSITIVE: '#10b981', // emerald
  NEUTRAL: '#64748b',  // slate
  NEGATIVE: '#f43f5e', // rose
};

const EMOTION_COLORS = ['#3b82f6', '#ec4899', '#8b5cf6', '#f59e0b', '#10b981', '#6366f1', '#64748b'];

export default function DashboardPage() {
  const navigate = useNavigate();
  const { selectedDataset, datasets, loading: datasetLoading } = useDataset();
  const [overview, setOverview] = useState(null);
  const [sentimentData, setSentimentData] = useState(null);
  const [emotionsData, setEmotionsData] = useState(null);
  const [topicsData, setTopicsData] = useState([]);
  const [trendAlerts, setTrendAlerts] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function loadDashboardData() {
      if (!selectedDataset) return;
      try {
        setLoading(true);
        const [ovRes, sentRes, emoRes, topRes, trendRes] = await Promise.all([
          analyticsService.getOverview(selectedDataset.id),
          analyticsService.getSentiment(selectedDataset.id),
          analyticsService.getEmotions(selectedDataset.id),
          analyticsService.getTopics(selectedDataset.id),
          trendService.getTrends(selectedDataset.id),
        ]);

        if (ovRes.data) setOverview(ovRes.data);
        if (sentRes.data) setSentimentData(sentRes.data);
        if (emoRes.data) setEmotionsData(emoRes.data);
        if (topRes.data) setTopicsData(topRes.data);
        if (trendRes.data && trendRes.data.alerts) setTrendAlerts(trendRes.data.alerts);
      } catch (err) {
        console.error('Failed to load dashboard data:', err);
      } finally {
        setLoading(false);
      }
    }

    loadDashboardData();
  }, [selectedDataset]);

  if (datasetLoading) {
    return <LoadingSpinner message="Checking datasets..." />;
  }

  if (!selectedDataset || datasets.length === 0) {
    return (
      <EmptyState
        title="No Dataset Uploaded"
        description="To view real analytics, public sentiment, and noise-resilient trends, please import a social media CSV or JSON dataset."
        actionText="Upload First Dataset"
        actionLink="/datasets"
      />
    );
  }

  if (selectedDataset.analysisStatus === 'PENDING') {
    return (
      <EmptyState
        title="Dataset Ready for AI Analysis"
        description={`The dataset "${selectedDataset.name}" has been imported (${selectedDataset.totalRecords} posts). Trigger the AI pipeline to extract duplicates, sentiment, topics, and trend anomalies.`}
        actionText="Run AI Analysis"
        actionLink="/datasets"
        icon={TrendingUp}
      />
    );
  }

  if (loading && !overview) {
    return <LoadingSpinner message="Loading analytics insights..." />;
  }

  // Format sentiment pie chart data
  const pieData = sentimentData?.distribution
    ? Object.entries(sentimentData.distribution).map(([key, value]) => ({
        name: key,
        value,
      }))
    : [];

  // Format emotion bar chart data
  const emotionChartData = emotionsData?.distribution
    ? Object.entries(emotionsData.distribution).map(([name, count]) => ({
        name: name.toUpperCase(),
        count,
      }))
    : [];

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
            Social Media Intelligence Dashboard
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
            Analyzing dataset: <span className="font-semibold text-slate-700 dark:text-slate-200">{selectedDataset.name}</span> ({selectedDataset.totalRecords} total posts)
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/trends')}
            className="inline-flex items-center gap-2 px-3.5 py-2 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 dark:bg-brand-950/60 dark:text-brand-300 dark:hover:bg-brand-900/60 rounded-lg border border-brand-200 dark:border-brand-800 transition-colors"
          >
            <TrendingUp className="w-4 h-4" />
            Noise-Resilient Trends
          </button>
          <button
            onClick={() => navigate('/evaluation')}
            className="inline-flex items-center gap-2 px-3.5 py-2 text-xs font-semibold text-white bg-brand-600 hover:bg-brand-500 rounded-lg shadow-sm transition-colors"
          >
            <ArrowUpRight className="w-4 h-4" />
            Research Evaluation
          </button>
        </div>
      </div>

      {/* Top Summary Stat Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-7 gap-4">
        <StatCard
          title="Total Posts"
          value={overview?.totalPosts || 0}
          icon={MessageSquare}
          color="brand"
        />
        <StatCard
          title="Unique Posts"
          value={overview?.uniquePosts || 0}
          icon={CopyCheck}
          color="indigo"
          subtitle={`${overview?.totalPosts ? Math.round(((overview.uniquePosts) / overview.totalPosts) * 100) : 100}% unique`}
        />
        <StatCard
          title="Positive"
          value={overview?.positivePosts || 0}
          icon={ThumbsUp}
          color="emerald"
        />
        <StatCard
          title="Negative"
          value={overview?.negativePosts || 0}
          icon={ThumbsDown}
          color="rose"
        />
        <StatCard
          title="Neutral"
          value={overview?.neutralPosts || 0}
          icon={MinusCircle}
          color="slate"
        />
        <StatCard
          title="Active Topics"
          value={overview?.activeTopics || 0}
          icon={Hash}
          color="purple"
        />
        <StatCard
          title="Trend Alerts"
          value={overview?.emergingTrends || 0}
          icon={TrendingUp}
          color="amber"
          subtitle={overview?.emergingTrends ? `${overview.emergingTrends} alerts active` : "0 alerts"}
        />
      </div>

      {/* Sentiment & Emotion Visualizations */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sentiment Distribution Pie */}
        <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Sentiment Distribution
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Twitter-RoBERTa 3-Class Sentiment Model
            </p>
          </div>
          <div className="h-64 mt-4">
            {pieData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieData}
                    innerRadius={55}
                    outerRadius={85}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {pieData.map((entry) => (
                      <Cell
                        key={`cell-${entry.name}`}
                        fill={SENTIMENT_COLORS[entry.name] || '#94a3b8'}
                      />
                    ))}
                  </Pie>
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
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-full text-xs text-slate-400">
                No sentiment data available
              </div>
            )}
          </div>
        </div>

        {/* Sentiment Over Time Line Chart */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Sentiment Dynamics Over Time
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Tracking positive, negative, and neutral volume across time windows
            </p>
          </div>
          <div className="h-64 mt-4">
            {sentimentData?.overTime && sentimentData.overTime.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={sentimentData.overTime}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.2} />
                  <XAxis dataKey="date" stroke="#94a3b8" fontSize={11} tickLine={false} />
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
                  <Line type="monotone" dataKey="positive" stroke="#10b981" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} name="Positive" />
                  <Line type="monotone" dataKey="negative" stroke="#f43f5e" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} name="Negative" />
                  <Line type="monotone" dataKey="neutral" stroke="#64748b" strokeWidth={2} dot={{ r: 3 }} activeDot={{ r: 5 }} name="Neutral" />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-full text-xs text-slate-400">
                Temporal distribution unavailable or single timestamp dataset
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Topics & Emotion Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Emotion Distribution */}
        <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Public Emotion Breakdown
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                DistilRoBERTa 7-Class Emotion Analysis (Primary: <span className="font-semibold text-brand-600 dark:text-brand-400">{emotionsData?.primaryEmotion || 'NEUTRAL'}</span>)
              </p>
            </div>
          </div>
          <div className="h-64 mt-4">
            {emotionChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={emotionChartData}>
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
                  <Bar dataKey="count" radius={[4, 4, 0, 0]} name="Posts">
                    {emotionChartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={EMOTION_COLORS[index % EMOTION_COLORS.length]} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="flex items-center justify-center h-full text-xs text-slate-400">
                Emotion classification data unavailable
              </div>
            )}
          </div>
        </div>

        {/* Top Discovered Semantic Topics */}
        <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Discovered BERTopics
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                Semantic clustering via Sentence-Transformers + Class-based TF-IDF
              </p>
            </div>
            <button
              onClick={() => navigate('/topics')}
              className="text-xs text-brand-600 dark:text-brand-400 font-semibold hover:underline"
            >
              View All
            </button>
          </div>

          <div className="mt-4 space-y-3">
            {topicsData.slice(0, 5).map((topic, idx) => (
              <div
                key={topic.id || idx}
                className="p-3 bg-slate-50 dark:bg-slate-700/40 rounded-lg flex items-center justify-between border border-slate-200/60 dark:border-slate-700/60"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-900 dark:text-white">
                      #{idx + 1} {topic.name}
                    </span>
                    <span className="text-[10px] px-1.5 py-0.5 rounded bg-brand-100 dark:bg-brand-900/60 text-brand-700 dark:text-brand-300 font-medium">
                      {topic.postCount} posts
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                    Keywords: {topic.topKeywords?.slice(0, 4).join(', ') || 'N/A'}
                  </p>
                </div>
                <div className="text-right">
                  <span className="text-xs font-semibold text-slate-600 dark:text-slate-300">
                    {Math.round((topic.representativeCoherence || 0.7) * 100)}%
                  </span>
                  <p className="text-[10px] text-slate-400">Coherence</p>
                </div>
              </div>
            ))}
            {topicsData.length === 0 && (
              <div className="text-center py-10 text-xs text-slate-400">
                No semantic topics generated yet
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Emerging Trend Alerts Section */}
      <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="p-2 bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400 rounded-lg">
              <AlertTriangle className="w-4 h-4" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Active Noise-Resilient Trend Alerts
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Alerts filtered by duplicate dampening, rolling Z-score temporal anomaly, and sentiment divergence
              </p>
            </div>
          </div>
          <button
            onClick={() => navigate('/trends')}
            className="text-xs font-semibold text-brand-600 dark:text-brand-400 hover:underline"
          >
            Explore Trend Details &rarr;
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-700/50 text-slate-500 dark:text-slate-400 uppercase tracking-wider text-[10px]">
              <tr>
                <th className="py-2.5 px-4 rounded-l-lg">Topic</th>
                <th className="py-2.5 px-4">Level</th>
                <th className="py-2.5 px-4">Classification</th>
                <th className="py-2.5 px-4">Trend Score</th>
                <th className="py-2.5 px-4">Growth %</th>
                <th className="py-2.5 px-4">Spam Filtered</th>
                <th className="py-2.5 px-4 rounded-r-lg">Reason</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/60">
              {trendAlerts.slice(0, 5).map((alert) => (
                <tr key={alert.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-700/30 transition-colors">
                  <td className="py-3 px-4 font-semibold text-slate-900 dark:text-white">
                    {alert.topicName}
                  </td>
                  <td className="py-3 px-4">
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
                  <td className="py-3 px-4 font-medium text-slate-700 dark:text-slate-300">
                    {alert.classification}
                  </td>
                  <td className="py-3 px-4 font-bold text-brand-600 dark:text-brand-400">
                    {Number(alert.trendScore).toFixed(3)}
                  </td>
                  <td className="py-3 px-4 text-emerald-600 dark:text-emerald-400 font-semibold">
                    +{Number(alert.growthPercentage).toFixed(1)}%
                  </td>
                  <td className="py-3 px-4 text-slate-500 dark:text-slate-400">
                    {Math.round((alert.duplicateRatio || 0) * 100)}%
                  </td>
                  <td className="py-3 px-4 text-slate-600 dark:text-slate-300 max-w-xs truncate" title={alert.alertReason}>
                    {alert.alertReason}
                  </td>
                </tr>
              ))}
              {trendAlerts.length === 0 && (
                <tr>
                  <td colSpan={7} className="text-center py-6 text-slate-400">
                    No active trend alerts detected for current baseline thresholds
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

