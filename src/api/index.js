import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

async function requestWithRetry(requestFn, retries = 2, delay = 1000) {
  try {
    return await requestFn()
  } catch (error) {
    if (retries > 0 && !axios.isCancel(error)) {
      await new Promise(resolve => setTimeout(resolve, delay))
      return requestWithRetry(requestFn, retries - 1, delay * 2)
    }
    throw error
  }
}

api.interceptors.response.use(
  response => response.data,
  error => {
    if (error.code === 'ECONNABORTED') {
      console.error('请求超时:', error.config?.url)
    } else if (error.response) {
      const status = error.response.status
      if (status === 401) {
        console.error('未授权访问')
      } else if (status === 403) {
        console.error('访问被拒绝')
      } else if (status >= 500) {
        console.error('服务器错误:', status)
      }
    } else if (error.request) {
      console.error('网络不可用，请检查连接')
    }
    return Promise.reject(error)
  }
)

export const logApi = {
  getOverview: () => requestWithRetry(() => api.get('/overview')),
  getPvUvStats: (date) => requestWithRetry(() => api.get('/pvuv', { params: { date } })),
  getTopApis: (date, limit) => requestWithRetry(() => api.get('/topN', { params: { date, limit } })),
  getErrors: (limit) => requestWithRetry(() => api.get('/errors', { params: { limit } })),
  getSystemHealth: () => requestWithRetry(() => api.get('/health')),
  getServerMetrics: () => requestWithRetry(() => api.get('/servers')),
  getLogLevelStats: () => requestWithRetry(() => api.get('/log-levels')),
  getResponseTime: () => requestWithRetry(() => api.get('/responseTime')),
  getHdfsPv: () => requestWithRetry(() => api.get('/hdfs/pv')),
  getHdfsUv: () => requestWithRetry(() => api.get('/hdfs/uv')),
  getHdfsErrors: () => requestWithRetry(() => api.get('/hdfs/errors')),
  getHdfsRt: () => requestWithRetry(() => api.get('/hdfs/rt')),
  getHdfsTop: () => requestWithRetry(() => api.get('/hdfs/top')),
  getHdfsDashboard: () => requestWithRetry(() => api.get('/hdfs/dashboard')),
  listHdfsDirs: (path) => requestWithRetry(() => api.get('/hdfs/dirs', { params: { path } }))
}

export default api
