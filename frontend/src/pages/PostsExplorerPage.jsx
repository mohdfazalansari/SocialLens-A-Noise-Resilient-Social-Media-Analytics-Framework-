import React, { useState, useEffect } from 'react';
import { useDataset } from '../context/DatasetContext';
import { postService } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import {
  Search,
  Filter,
  CopyCheck,
  Heart,
  MessageCircle,
  Share2,
  Calendar,
  User,
  ThumbsUp,
  ThumbsDown,
  MinusCircle,
  Sparkles,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';

export default function PostsExplorerPage() {
  const { selectedDataset } = useDataset();
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [platformFilter, setPlatformFilter] = useState('');
  const [duplicateFilter, setDuplicateFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    async function loadPosts() {
      if (!selectedDataset) return;
      try {
        setLoading(true);
        const params = {
          datasetId: selectedDataset.id,
          page,
          size: 15,
          sortBy: 'id',
          sortDir: 'desc',
        };
        if (searchQuery.trim()) params.query = searchQuery.trim();
        if (platformFilter) params.platform = platformFilter;
        if (duplicateFilter === 'duplicate') params.isDuplicate = true;
        if (duplicateFilter === 'unique') params.isDuplicate = false;

        const res = await postService.getPosts(params);
        if (res.data) {
          setPosts(res.data.content || []);
          setTotalPages(res.data.totalPages || 1);
          setTotalElements(res.data.totalElements || 0);
        }
      } catch (err) {
        console.error('Failed to load posts:', err);
      } finally {
        setLoading(false);
      }
    }

    const timer = setTimeout(loadPosts, 250);
    return () => clearTimeout(timer);
  }, [selectedDataset, searchQuery, platformFilter, duplicateFilter, page]);

  if (!selectedDataset) {
    return (
      <EmptyState
        title="No Dataset Selected"
        description="Select or upload a dataset to explore individual posts, duplicate groups, and sentiment annotations."
      />
    );
  }

  const getSentimentPill = (label, confidence) => {
    switch (label?.toUpperCase()) {
      case 'POSITIVE':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800">
            <ThumbsUp className="w-3 h-3" /> Positive {confidence ? `(${Math.round(confidence * 100)}%)` : ''}
          </span>
        );
      case 'NEGATIVE':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-rose-50 text-rose-700 dark:bg-rose-950/50 dark:text-rose-400 border border-rose-200 dark:border-rose-800">
            <ThumbsDown className="w-3 h-3" /> Negative {confidence ? `(${Math.round(confidence * 100)}%)` : ''}
          </span>
        );
      case 'NEUTRAL':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-300">
            <MinusCircle className="w-3 h-3" /> Neutral
          </span>
        );
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
          Social Media Posts Explorer
        </h2>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Explore ingested posts with real-time text search, duplicate status verification, and RoBERTa annotations ({totalElements.toLocaleString()} matching records).
        </p>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white dark:bg-slate-800 p-4 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="relative w-full md:w-96">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            <Search className="w-4 h-4" />
          </div>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setPage(0);
            }}
            placeholder="Search keywords, phrases, #hashtags..."
            className="w-full pl-10 pr-4 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg text-xs text-slate-900 dark:text-white focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <select
            value={duplicateFilter}
            onChange={(e) => {
              setDuplicateFilter(e.target.value);
              setPage(0);
            }}
            className="px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg text-xs font-medium text-slate-700 dark:text-slate-300"
          >
            <option value="">All Posts (Unique & Dupes)</option>
            <option value="unique">Only Unique Posts</option>
            <option value="duplicate">Only Duplicates</option>
          </select>

          <select
            value={platformFilter}
            onChange={(e) => {
              setPlatformFilter(e.target.value);
              setPage(0);
            }}
            className="px-3 py-2 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg text-xs font-medium text-slate-700 dark:text-slate-300"
          >
            <option value="">All Platforms</option>
            <option value="TWITTER">Twitter</option>
            <option value="REDDIT">Reddit</option>
            <option value="TELEGRAM">Telegram</option>
          </select>
        </div>
      </div>

      {/* Posts List */}
      {loading ? (
        <LoadingSpinner message="Searching records..." />
      ) : posts.length === 0 ? (
        <div className="text-center py-12 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-400 text-xs">
          No posts matching current filters
        </div>
      ) : (
        <div className="space-y-3">
          {posts.map((post) => (
            <div
              key={post.id}
              className={`p-4 bg-white dark:bg-slate-800 rounded-xl border transition-all ${
                post.isDuplicate
                  ? 'border-amber-200/80 dark:border-amber-900/40 bg-amber-50/20 dark:bg-amber-950/10'
                  : 'border-slate-200 dark:border-slate-700 hover:shadow-xs'
              }`}
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-center gap-2 text-xs text-slate-500 dark:text-slate-400">
                  <div className="w-6 h-6 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-300">
                    <User className="w-3.5 h-3.5" />
                  </div>
                  <span className="font-semibold text-slate-700 dark:text-slate-200">
                    user_{post.authorId?.slice(0, 8) || post.authorId}
                  </span>
                  <span>&bull;</span>
                  <span className="font-mono uppercase text-[10px] px-1.5 py-0.5 rounded bg-slate-100 dark:bg-slate-700 text-slate-600 dark:text-slate-300 font-bold">
                    {post.platform}
                  </span>
                  <span>&bull;</span>
                  <span className="flex items-center gap-1 text-[11px]">
                    <Calendar className="w-3 h-3" />
                    {new Date(post.postCreatedAt || Date.now()).toLocaleString()}
                  </span>
                </div>

                <div className="flex items-center gap-2">
                  {post.isDuplicate ? (
                    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800 dark:bg-amber-950/80 dark:text-amber-300 border border-amber-300 dark:border-amber-800">
                      <CopyCheck className="w-3 h-3" /> Duplicate Spam (Group #{post.duplicateGroupId})
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-950/80 dark:text-emerald-300">
                      Unique
                    </span>
                  )}
                  {getSentimentPill(post.sentimentLabel, post.sentimentConfidence)}
                  {post.dominantEmotion && (
                    <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-purple-50 text-purple-700 dark:bg-purple-950/50 dark:text-purple-300 border border-purple-200 dark:border-purple-800">
                      {post.dominantEmotion}
                    </span>
                  )}
                </div>
              </div>

              <p className="mt-3 text-xs leading-relaxed text-slate-800 dark:text-slate-200">
                {post.postText}
              </p>

              {post.topics && post.topics.length > 0 && (
                <div className="mt-3 flex items-center gap-1.5 flex-wrap">
                  {post.topics.map((t, idx) => (
                    <span
                      key={idx}
                      className="inline-flex items-center gap-1 text-[10px] font-medium px-2 py-0.5 rounded bg-brand-50 text-brand-700 dark:bg-brand-950/60 dark:text-brand-300 border border-brand-200 dark:border-brand-800"
                    >
                      <Sparkles className="w-2.5 h-2.5" />
                      Topic: {t}
                    </span>
                  ))}
                </div>
              )}

              <div className="mt-3 pt-3 border-t border-slate-100 dark:border-slate-700/60 flex items-center gap-6 text-xs text-slate-400">
                <span className="flex items-center gap-1.5 hover:text-rose-500">
                  <Heart className="w-3.5 h-3.5" /> {post.likeCount || 0}
                </span>
                <span className="flex items-center gap-1.5 hover:text-blue-500">
                  <MessageCircle className="w-3.5 h-3.5" /> {post.replyCount || 0}
                </span>
                <span className="flex items-center gap-1.5 hover:text-emerald-500">
                  <Share2 className="w-3.5 h-3.5" /> {post.shareCount || 0}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between pt-4">
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Page {page + 1} of {totalPages}
          </p>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="p-2 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 disabled:opacity-50 text-xs font-medium"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="p-2 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 disabled:opacity-50 text-xs font-medium"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
