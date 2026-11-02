import React, { useEffect, useState, useMemo } from 'react';
import { useDataset } from '../context/DatasetContext';
import { networkService } from '../services/api';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import StatCard from '../components/common/StatCard';
import {
  Share2,
  Users,
  Activity,
  Award,
  Layers,
  Search,
  Sliders,
  ExternalLink
} from 'lucide-react';

export default function NetworkPage() {
  const { selectedDataset } = useDataset();
  const [graphData, setGraphData] = useState(null);
  const [influencers, setInfluencers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedNode, setSelectedNode] = useState(null);

  useEffect(() => {
    async function loadNetworkData() {
      if (!selectedDataset) return;
      try {
        setLoading(true);
        const [gRes, infRes] = await Promise.all([
          networkService.getGraph(selectedDataset.id, 80),
          networkService.getInfluencers(selectedDataset.id, 25),
        ]);
        if (gRes.data) setGraphData(gRes.data);
        if (infRes.data) setInfluencers(infRes.data);
      } catch (err) {
        console.error('Failed to load network data:', err);
      } finally {
        setLoading(false);
      }
    }
    loadNetworkData();
  }, [selectedDataset]);

  // Compute circular layout for interactive SVG visualization
  const layoutNodes = useMemo(() => {
    if (!graphData || !graphData.nodes || graphData.nodes.length === 0) return [];
    const count = graphData.nodes.length;
    const width = 600;
    const height = 450;
    const centerX = width / 2;
    const centerY = height / 2;
    const radius = Math.min(centerX, centerY) - 50;

    return graphData.nodes.map((node, i) => {
      const angle = (2 * Math.PI * i) / count;
      // Add slight jitter based on community
      const commOffset = (node.community % 3) * 15;
      const r = radius - commOffset;
      const x = centerX + r * Math.cos(angle);
      const y = centerY + r * Math.sin(angle);
      return {
        ...node,
        x,
        y,
        size: Math.max(6, Math.min(22, 6 + (node.pagerank || 0.01) * 350)),
      };
    });
  }, [graphData]);

  const layoutNodeMap = useMemo(() => {
    const map = {};
    layoutNodes.forEach((n) => {
      map[n.id] = n;
    });
    return map;
  }, [layoutNodes]);

  if (!selectedDataset) {
    return <EmptyState title="No Dataset Selected" description="Select a dataset to explore social interaction networks and influencer metrics." />;
  }

  if (loading && !graphData) {
    return <LoadingSpinner message="Constructing NetworkX graph topology & computing PageRank..." />;
  }

  const COMMUNITY_COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6', '#06b6d4'];

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold text-slate-900 dark:text-white tracking-tight">
          Social Interaction & Influencer Network Analysis
        </h2>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
          Graph topology constructed from observable social reply & mention interactions with NetworkX centrality algorithms.
        </p>
      </div>

      {/* Network Metrics Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatCard
          title="Interaction Nodes"
          value={graphData?.metrics?.totalNodes || 0}
          icon={Users}
          color="brand"
        />
        <StatCard
          title="Interaction Edges"
          value={graphData?.metrics?.totalEdges || 0}
          icon={Share2}
          color="indigo"
        />
        <StatCard
          title="Graph Density"
          value={graphData?.metrics?.density !== undefined ? Number(graphData.metrics.density).toFixed(4) : '0.0000'}
          icon={Activity}
          color="purple"
        />
        <StatCard
          title="Connected Components"
          value={graphData?.metrics?.connectedComponents || 0}
          icon={Layers}
          color="emerald"
        />
      </div>

      {/* Main Grid: Graph + Influencer Leaderboard */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Interactive Graph SVG Canvas */}
        <div className="lg:col-span-2 bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white">
                Topology Visualization
              </h3>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                Node size scaled by PageRank; color grouped by community cluster
              </p>
            </div>
            {selectedNode && (
              <span className="text-xs font-semibold px-2.5 py-1 rounded bg-brand-50 text-brand-700 dark:bg-brand-950 dark:text-brand-300">
                Selected: {selectedNode.label || selectedNode.id}
              </span>
            )}
          </div>

          <div className="mt-4 border border-slate-100 dark:border-slate-700/60 rounded-xl bg-slate-50/50 dark:bg-slate-900/60 flex items-center justify-center p-4 overflow-hidden">
            {layoutNodes.length > 0 ? (
              <svg viewBox="0 0 600 450" className="w-full h-80 max-h-96 select-none">
                {/* Edges */}
                <g className="edges">
                  {graphData?.edges?.map((edge, idx) => {
                    const u = layoutNodeMap[edge.source];
                    const v = layoutNodeMap[edge.target];
                    if (!u || !v) return null;
                    const isHighlighted = selectedNode && (selectedNode.id === edge.source || selectedNode.id === edge.target);
                    return (
                      <line
                        key={`edge-${idx}`}
                        x1={u.x}
                        y1={u.y}
                        x2={v.x}
                        y2={v.y}
                        stroke={isHighlighted ? '#0c87eb' : '#94a3b8'}
                        strokeOpacity={isHighlighted ? 0.9 : 0.3}
                        strokeWidth={isHighlighted ? 2.5 : Math.min(3, 0.5 + (edge.weight || 1) * 0.4)}
                      />
                    );
                  })}
                </g>

                {/* Nodes */}
                <g className="nodes">
                  {layoutNodes.map((node) => {
                    const isSelected = selectedNode?.id === node.id;
                    const color = COMMUNITY_COLORS[node.community % COMMUNITY_COLORS.length];
                    return (
                      <g
                        key={`node-${node.id}`}
                        onClick={() => setSelectedNode(node)}
                        className="cursor-pointer transition-all hover:opacity-80"
                      >
                        <circle
                          cx={node.x}
                          cy={node.y}
                          r={node.size}
                          fill={color}
                          stroke={isSelected ? '#ffffff' : color}
                          strokeWidth={isSelected ? 3 : 1}
                          className="transition-all"
                        />
                        {node.size > 14 && (
                          <text
                            x={node.x}
                            y={node.y + node.size + 11}
                            fontSize="9"
                            textAnchor="middle"
                            fill="#64748b"
                            className="font-mono font-medium pointer-events-none"
                          >
                            {node.label || node.id.slice(0, 7)}
                          </text>
                        )}
                      </g>
                    );
                  })}
                </g>
              </svg>
            ) : (
              <div className="text-center py-20 text-xs text-slate-400">
                No interactive interactions found in current dataset
              </div>
            )}
          </div>
        </div>

        {/* Selected Node Details or Instructions */}
        <div className="bg-white dark:bg-slate-800 p-5 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white">
              Node Network Profile
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Click any node in graph or table to view centrality
            </p>

            {selectedNode ? (
              <div className="mt-4 p-4 bg-slate-50 dark:bg-slate-700/40 rounded-xl border border-slate-200/80 dark:border-slate-700 space-y-3 text-xs">
                <div>
                  <span className="text-[10px] uppercase font-bold text-slate-400">Pseudonym Identifier</span>
                  <p className="text-base font-bold text-slate-900 dark:text-white font-mono">
                    {selectedNode.label || selectedNode.id}
                  </p>
                </div>
                <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-200 dark:border-slate-700">
                  <div>
                    <span className="text-slate-400">PageRank:</span>
                    <p className="font-bold text-brand-600">{Number(selectedNode.pagerank).toFixed(5)}</p>
                  </div>
                  <div>
                    <span className="text-slate-400">Betweenness:</span>
                    <p className="font-bold text-purple-600">{Number(selectedNode.betweenness).toFixed(5)}</p>
                  </div>
                  <div>
                    <span className="text-slate-400">Degree (Interactions):</span>
                    <p className="font-bold text-slate-700 dark:text-slate-200">{selectedNode.degree || 0}</p>
                  </div>
                  <div>
                    <span className="text-slate-400">Community:</span>
                    <p className="font-bold text-emerald-600">Cluster #{selectedNode.community || 0}</p>
                  </div>
                </div>
              </div>
            ) : (
              <div className="mt-6 p-6 text-center border-2 border-dashed border-slate-200 dark:border-slate-700 rounded-xl text-xs text-slate-400">
                Select a graph node to inspect betweenness centrality and community cluster metrics
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Influencer Leaderboard Table */}
      <div className="bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden">
        <div className="p-5 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
          <div>
            <h3 className="text-sm font-bold text-slate-900 dark:text-white flex items-center gap-2">
              <Award className="w-4 h-4 text-brand-600" />
              Influential Node Leaderboard (PageRank & Centrality)
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Ranked accounts based on observable interaction influence
            </p>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-700/50 text-slate-500 dark:text-slate-400 uppercase tracking-wider text-[10px]">
              <tr>
                <th className="py-3 px-5">Rank</th>
                <th className="py-3 px-4">Account Pseudonym</th>
                <th className="py-3 px-4">PageRank Score</th>
                <th className="py-3 px-4">Betweenness Centrality</th>
                <th className="py-3 px-4">In-Degree (Replies Received)</th>
                <th className="py-3 px-4">Out-Degree</th>
                <th className="py-3 px-5 text-right">Community Cluster</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700/60">
              {influencers.map((inf, idx) => (
                <tr
                  key={inf.nodeId}
                  onClick={() => setSelectedNode({ id: inf.nodeId, label: inf.pseudonym, ...inf })}
                  className="hover:bg-slate-50/50 dark:hover:bg-slate-700/30 cursor-pointer transition-colors"
                >
                  <td className="py-3.5 px-5 font-bold text-slate-400">
                    #{idx + 1}
                  </td>
                  <td className="py-3.5 px-4 font-mono font-semibold text-slate-900 dark:text-white">
                    {inf.pseudonym}
                  </td>
                  <td className="py-3.5 px-4 font-bold text-brand-600 dark:text-brand-400">
                    {Number(inf.pagerank).toFixed(5)}
                  </td>
                  <td className="py-3.5 px-4 font-medium text-purple-600 dark:text-purple-400">
                    {Number(inf.betweenness).toFixed(5)}
                  </td>
                  <td className="py-3.5 px-4 font-semibold text-slate-800 dark:text-slate-200">
                    {inf.inDegree}
                  </td>
                  <td className="py-3.5 px-4 text-slate-600 dark:text-slate-400">
                    {inf.outDegree}
                  </td>
                  <td className="py-3.5 px-5 text-right font-medium">
                    <span className="px-2 py-0.5 rounded bg-slate-100 dark:bg-slate-700 text-slate-700 dark:text-slate-300">
                      Cluster #{inf.communityId}
                    </span>
                  </td>
                </tr>
              ))}
              {influencers.length === 0 && (
                <tr>
                  <td colSpan={7} className="text-center py-8 text-slate-400">
                    No influencer records calculated
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
