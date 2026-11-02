import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Database,
  MessageSquare,
  Smile,
  Hash,
  TrendingUp,
  Share2,
  FileCheck2,
  Sparkles
} from 'lucide-react';

export default function Sidebar() {
  const navItems = [
    { label: 'Dashboard', path: '/', icon: LayoutDashboard },
    { label: 'Datasets', path: '/datasets', icon: Database },
    { label: 'Posts Explorer', path: '/posts', icon: MessageSquare },
    { label: 'Sentiment & Emotions', path: '/sentiment', icon: Smile },
    { label: 'Topic Discovery', path: '/topics', icon: Hash },
    { label: 'Noise-Resilient Trends', path: '/trends', icon: TrendingUp, highlighted: true },
    { label: 'Network & Influencers', path: '/network', icon: Share2 },
    { label: 'Research Evaluation', path: '/evaluation', icon: FileCheck2 },
  ];

  return (
    <aside className="w-64 bg-white dark:bg-slate-800 border-r border-slate-200 dark:border-slate-700 min-h-[calc(100vh-4rem)] flex flex-col justify-between py-6 px-4 transition-colors">
      <div className="space-y-6">
        <div>
          <span className="px-3 text-[11px] font-bold uppercase tracking-wider text-slate-400 dark:text-slate-500">
            Analytics Modules
          </span>
          <nav className="mt-3 space-y-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) =>
                    `flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
                      isActive
                        ? 'bg-brand-50 text-brand-700 dark:bg-brand-950/60 dark:text-brand-300 font-semibold shadow-xs'
                        : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100/80 dark:text-slate-400 dark:hover:text-slate-200 dark:hover:bg-slate-700/50'
                    }`
                  }
                >
                  <div className="flex items-center gap-3">
                    <Icon className="w-4 h-4" />
                    <span>{item.label}</span>
                  </div>
                  {item.highlighted && (
                    <span className="flex items-center gap-1 text-[10px] font-bold px-1.5 py-0.5 rounded bg-brand-500/10 text-brand-600 dark:text-brand-400">
                      <Sparkles className="w-2.5 h-2.5" />
                      Core
                    </span>
                  )}
                </NavLink>
              );
            })}
          </nav>
        </div>
      </div>

      {/* Research Methodology Card */}
      <div className="p-4 bg-slate-50 dark:bg-slate-700/40 rounded-xl border border-slate-200/80 dark:border-slate-700">
        <h4 className="text-xs font-semibold text-slate-900 dark:text-white flex items-center gap-1.5 mb-1.5">
          <Sparkles className="w-3.5 h-3.5 text-brand-500" />
          Research Focus
        </h4>
        <p className="text-[11px] text-slate-500 dark:text-slate-400 leading-relaxed">
          Benchmarking whether duplicate damping, anomaly Z-scores, and sentiment shifts improve emerging trend discovery over raw volume.
        </p>
      </div>
    </aside>
  );
}
