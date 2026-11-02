import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('sociallens_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('sociallens_token');
      localStorage.removeItem('sociallens_user');
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
    }
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred';
    return Promise.reject(new Error(message));
  }
);

export const authService = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getCurrentUser: () => api.get('/auth/me'),
};

export const datasetService = {
  preview: (formData) => api.post('/datasets/preview', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  upload: (formData) => api.post('/datasets/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  getAll: (page = 0, size = 20) => api.get(`/datasets?page=${page}&size=${size}`),
  getById: (id) => api.get(`/datasets/${id}`),
  delete: (id) => api.delete(`/datasets/${id}`),
  triggerAnalysis: (id, config) => api.post(`/datasets/${id}/analyze`, config),
  getStatus: (id) => api.get(`/datasets/${id}/status`),
};

export const postService = {
  getPosts: (params) => api.get('/posts', { params }),
  getById: (id) => api.get(`/posts/${id}`),
};

export const analyticsService = {
  getOverview: (datasetId) => api.get(`/analytics/overview?datasetId=${datasetId}`),
  getSentiment: (datasetId) => api.get(`/analytics/sentiment?datasetId=${datasetId}`),
  getEmotions: (datasetId) => api.get(`/analytics/emotions?datasetId=${datasetId}`),
  getTopics: (datasetId) => api.get(`/analytics/topics?datasetId=${datasetId}`),
};

export const trendService = {
  getTrends: (datasetId, method = 'PROPOSED_NOISE_RESILIENT') =>
    api.get(`/trends?datasetId=${datasetId}&method=${method}`),
  getAlertById: (alertId) => api.get(`/trends/${alertId}`),
};

export const networkService = {
  getGraph: (datasetId, maxNodes = 100) => api.get(`/network/graph?datasetId=${datasetId}&maxNodes=${maxNodes}`),
  getInfluencers: (datasetId, limit = 20) => api.get(`/network/influencers?datasetId=${datasetId}&limit=${limit}`),
};

export const evaluationService = {
  runEvaluation: (datasetId, name, groundTruthSource) =>
    api.post(`/evaluation/run?datasetId=${datasetId}${name ? `&name=${encodeURIComponent(name)}` : ''}${groundTruthSource ? `&groundTruthSource=${encodeURIComponent(groundTruthSource)}` : ''}`),
  getRuns: (datasetId) => api.get(`/evaluation/runs?datasetId=${datasetId}`),
};

export default api;
