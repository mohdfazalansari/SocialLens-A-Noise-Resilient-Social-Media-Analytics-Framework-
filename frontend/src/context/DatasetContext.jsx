import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { datasetService } from '../services/api';
import { useAuth } from './AuthContext';

const DatasetContext = createContext(null);

export function DatasetProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [datasets, setDatasets] = useState([]);
  const [selectedDataset, setSelectedDataset] = useState(null);
  const [loading, setLoading] = useState(false);
  const [pollingStatus, setPollingStatus] = useState(false);

  const fetchDatasets = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      setLoading(true);
      const res = await datasetService.getAll(0, 50);
      if (res.data && res.data.content) {
        setDatasets(res.data.content);
        if (res.data.content.length > 0 && !selectedDataset) {
          const savedId = localStorage.getItem('sociallens_selected_dataset_id');
          const found = res.data.content.find((d) => String(d.id) === String(savedId));
          const toSelect = found || res.data.content[0];
          setSelectedDataset(toSelect);
          localStorage.setItem('sociallens_selected_dataset_id', toSelect.id);
        }
      }
    } catch (err) {
      console.error('Failed to load datasets:', err);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, selectedDataset]);

  useEffect(() => {
    fetchDatasets();
  }, [fetchDatasets]);

  // Polling for processing datasets
  useEffect(() => {
    let interval = null;
    if (selectedDataset && (selectedDataset.analysisStatus === 'PROCESSING' || pollingStatus)) {
      interval = setInterval(async () => {
        try {
          const res = await datasetService.getById(selectedDataset.id);
          if (res.data) {
            setSelectedDataset(res.data);
            if (res.data.analysisStatus !== 'PROCESSING') {
              setPollingStatus(false);
              fetchDatasets();
            }
          }
        } catch (e) {
          console.error('Polling error:', e);
        }
      }, 3000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [selectedDataset, pollingStatus, fetchDatasets]);

  const selectDataset = (dataset) => {
    setSelectedDataset(dataset);
    if (dataset) {
      localStorage.setItem('sociallens_selected_dataset_id', dataset.id);
    } else {
      localStorage.removeItem('sociallens_selected_dataset_id');
    }
  };

  const triggerAnalysis = async (datasetId, config) => {
    const res = await datasetService.triggerAnalysis(datasetId, config);
    setPollingStatus(true);
    // Optimistically update
    if (selectedDataset && selectedDataset.id === datasetId) {
      setSelectedDataset({ ...selectedDataset, analysisStatus: 'PROCESSING' });
    }
    return res;
  };

  return (
    <DatasetContext.Provider
      value={{
        datasets,
        selectedDataset,
        selectDataset,
        loading,
        fetchDatasets,
        triggerAnalysis,
      }}
    >
      {children}
    </DatasetContext.Provider>
  );
}

export function useDataset() {
  const context = useContext(DatasetContext);
  if (!context) {
    throw new Error('useDataset must be used within a DatasetProvider');
  }
  return context;
}

