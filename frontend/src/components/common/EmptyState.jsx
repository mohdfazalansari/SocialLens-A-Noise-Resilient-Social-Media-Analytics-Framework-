import React from 'react';
import { Database, PlusCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function EmptyState({
  title = 'No Data Available',
  description = 'Upload a CSV or JSON social media dataset to begin analyzing sentiment, topics, and noise-resilient trends.',
  actionText = 'Go to Datasets',
  actionLink = '/datasets',
  icon: Icon = Database,
}) {
  const navigate = useNavigate();

  return (
    <div className="flex flex-col items-center justify-center p-12 text-center bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm max-w-lg mx-auto my-8">
      <div className="p-4 bg-brand-50 dark:bg-brand-950/50 text-brand-600 dark:text-brand-400 rounded-2xl mb-4 ring-8 ring-brand-50/50 dark:ring-brand-950/20">
        <Icon className="w-8 h-8" />
      </div>
      <h3 className="text-lg font-semibold text-slate-900 dark:text-white mb-2">{title}</h3>
      <p className="text-sm text-slate-500 dark:text-slate-400 mb-6 leading-relaxed">{description}</p>
      {actionText && (
        <button
          onClick={() => navigate(actionLink)}
          className="inline-flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-brand-600 hover:bg-brand-700 rounded-lg shadow-sm transition-all hover:shadow focus:ring-2 focus:ring-brand-500 focus:outline-none"
        >
          <PlusCircle className="w-4 h-4" />
          {actionText}
        </button>
      )}
    </div>
  );
}

