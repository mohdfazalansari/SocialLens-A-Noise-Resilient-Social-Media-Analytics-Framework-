import React from 'react';
import { useAuth } from '../../context/AuthContext';
import { useDataset } from '../../context/DatasetContext';
import {
  Layers,
  LogOut,
  Moon,
  Sun,
  User,
  Activity,
  AlertCircle,
  CheckCircle2,
  Clock
} from 'lucide-react';

export default function Navbar({ darkMode, setDarkMode }) {
  const { user, logout } = useAuth();
  const { datasets, selectedDataset, selectDataset } = useDataset();

  const getStatusBadge = (status) => {
    switch (status) {
      case 'COMPLETED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 dark:bg-emerald-950/40 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-800">
            <CheckCircle2 className="w-3.5 h-3.5" />
            Analyzed
          </span>
        );
      case 'PROCESSING':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400 border border-amber-200 dark:border-amber-800 animate-pulse">
            <Clock className="w-3.5 h-3.5" />
            Processing AI Pipeline...
          </span>
        );
      case 'FAILED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-400 border border-rose-200 dark:border-rose-800">
            <AlertCircle className="w-3.5 h-3.5" />
            Analysis Failed
          </span>
        );
      default:
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300">
            Pending Analysis
          </span>
        );
    }
  };

  return (
    <header className="sticky top-0 z-30 h-16 bg-white dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700 px-6 flex items-center justify-between transition-colors shadow-sm">
      <div className="flex items-center gap-6">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-brand-600 text-white rounded-lg shadow-sm">
            <Activity className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-base font-bold text-slate-900 dark:text-white leading-tight flex items-center gap-2">
              SocialLens
              <span className="text-[10px] font-semibold tracking-wider uppercase px-2 py-0.5 rounded bg-brand-100 text-brand-700 dark:bg-brand-900/60 dark:text-brand-300">
                CSE Minor Project
              </span>
            </h1>
          </div>
        </div>

        {/* Dataset selector pill */}
        {datasets.length > 0 && (
          <div className="flex items-center gap-3 pl-6 border-l border-slate-200 dark:border-slate-700">
            <div className="flex items-center gap-2">
              <Layers className="w-4 h-4 text-slate-400" />
              <select
                value={selectedDataset?.id || ''}
                onChange={(e) => {
                  const ds = datasets.find((d) => String(d.id) === e.target.value);
                  if (ds) selectDataset(ds);
                }}
                className="text-xs font-medium bg-slate-100 dark:bg-slate-700/60 text-slate-800 dark:text-slate-200 rounded-lg px-3 py-1.5 border-none focus:ring-2 focus:ring-brand-500 cursor-pointer"
              >
                {datasets.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name} ({d.totalRecords} posts)
                  </option>
                ))}
              </select>
            </div>
            {selectedDataset && getStatusBadge(selectedDataset.analysisStatus)}
          </div>
        )}
      </div>

      <div className="flex items-center gap-4">
        {/* Dark mode toggle */}
        <button
          onClick={() => setDarkMode(!darkMode)}
          className="p-2 text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-700/50 transition-colors"
          title={darkMode ? 'Switch to Light Theme' : 'Switch to Dark Theme'}
        >
          {darkMode ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
        </button>

        {/* User profile and logout */}
        {user && (
          <div className="flex items-center gap-3 pl-4 border-l border-slate-200 dark:border-slate-700">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center text-slate-700 dark:text-slate-300">
                <User className="w-4 h-4" />
              </div>
              <div className="hidden sm:block text-left">
                <p className="text-xs font-semibold text-slate-900 dark:text-white leading-tight">
                  {user.username}
                </p>
                <p className="text-[10px] text-slate-500 dark:text-slate-400 font-medium capitalize">
                  {user.role}
                </p>
              </div>
            </div>

            <button
              onClick={logout}
              className="p-2 text-slate-500 hover:text-rose-600 dark:text-slate-400 dark:hover:text-rose-400 rounded-lg hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-colors"
              title="Logout"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </header>
  );
}

