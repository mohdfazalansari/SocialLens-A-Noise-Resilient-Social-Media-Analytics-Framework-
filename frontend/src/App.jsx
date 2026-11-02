import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { DatasetProvider } from './context/DatasetContext';

import DashboardLayout from './layouts/DashboardLayout';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import DatasetsPage from './pages/DatasetsPage';
import PostsExplorerPage from './pages/PostsExplorerPage';
import SentimentPage from './pages/SentimentPage';
import TopicsPage from './pages/TopicsPage';
import TrendsPage from './pages/TrendsPage';
import NetworkPage from './pages/NetworkPage';
import EvaluationPage from './pages/EvaluationPage';
import LoadingSpinner from './components/common/LoadingSpinner';

function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-900 text-white">
        <LoadingSpinner message="Authenticating session..." />
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <DatasetProvider>
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Protected Routes */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <DashboardLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<DashboardPage />} />
              <Route path="datasets" element={<DatasetsPage />} />
              <Route path="posts" element={<PostsExplorerPage />} />
              <Route path="sentiment" element={<SentimentPage />} />
              <Route path="topics" element={<TopicsPage />} />
              <Route path="trends" element={<TrendsPage />} />
              <Route path="network" element={<NetworkPage />} />
              <Route path="evaluation" element={<EvaluationPage />} />
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </DatasetProvider>
      </AuthProvider>
    </Router>
  );
}
