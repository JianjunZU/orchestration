import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// Process Definitions
export const processApi = {
  list: () => api.get('/process-definitions'),
  getById: (id) => api.get(`/process-definitions/${id}`),
  getByKey: (key) => api.get(`/process-definitions/key/${key}`),
  save: (data) => api.post('/process-definitions', data),
  deploy: (id) => api.post(`/process-definitions/${id}/deploy`),
  delete: (id) => api.delete(`/process-definitions/${id}`)
}

// Process Instances
export const instanceApi = {
  start: (data) => api.post('/process-instances/start', data),
  listRunning: (processKey) => api.get('/process-instances/running', { params: { processKey } }),
  listHistory: (processKey) => api.get('/process-instances/history', { params: { processKey } }),
  detail: (id) => api.get(`/process-instances/${id}`),
  stop: (id, reason) => api.delete(`/process-instances/${id}`, { params: { reason } }),
  sendSignal: (data) => api.post('/process-instances/signal', data),
  sendMessage: (data) => api.post('/process-instances/message', data)
}

// Triggers
export const triggerApi = {
  list: () => api.get('/triggers'),
  getById: (id) => api.get(`/triggers/${id}`),
  save: (data) => api.post('/triggers', data),
  delete: (id) => api.delete(`/triggers/${id}`),
  toggle: (id) => api.put(`/triggers/${id}/toggle`)
}

// Element Registry
export const registryApi = {
  getExecutorTypes: () => api.get('/registry/executor-types'),
  getLogicTypes: () => api.get('/registry/logic-types'),
  getTriggerTypes: () => api.get('/registry/trigger-types')
}

export default api
