import axios from 'axios';
import { getToken, updateToken } from './keycloak';

// Create an axios instance with default config
const api = axios.create({
  baseURL: 'http://localhost:8080', // Direct backend URL
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  withCredentials: false // Don't send credentials for CORS
});

// Add a request interceptor to include the auth token
api.interceptors.request.use(
  async (config) => {
    try {
      // First try to get token from localStorage (for manual login flow)
      let token = localStorage.getItem('auth_token');
      
      if (!token) {
        // If no token in localStorage, try to update from Keycloak
        try {
          token = await updateToken(60);
        } catch (tokenError) {
          console.warn('Could not update token via Keycloak:', tokenError);
        }
      }
      
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        console.log('Added auth token to request');
      } else {
        console.warn('No auth token available for request');
      }
      
      // Don't add CORS headers on the client side
      
    } catch (error) {
      console.error('Failed to set auth token', error);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor to handle common errors
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API Error:', error);
    
    // Handle authentication errors
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      console.error('Authentication error:', error.response.data);
    }
    
    return Promise.reject(error);
  }
);

// Process-related API calls
export const processApi = {
  startProcess: (processData) => {
    console.log('Starting process with data:', processData);
    return api.post('/process/start', processData);
  },
  getProcessInstance: (processInstanceKey) => api.get(`/process/process-instance/${processInstanceKey}`),
  addVariablesByTaskId: (taskId, variables) => api.post(`/process/add-variables/${taskId}`, variables),
  addVariablesByProcessInstanceId: (processInstanceKey, variables) => 
    api.post(`/process/add-variables-by-process-instance/${processInstanceKey}`, variables),
};

// Task-related API calls
export const taskApi = {
  getTasksByUser: (username) => api.get(`/tasks/user/${username}`),
  getTaskById: (taskId) => api.get(`/tasks/${taskId}`),
  getTaskVariables: (taskId) => api.get(`/tasks/${taskId}/variables`),
  getUnassignedTasks: () => api.get('/tasks/unassigned'),
  getAllTasks: () => api.get('/tasks/all'),
  claimTask: (taskId) => api.post(`/tasks/${taskId}/claim`),
  unclaimTask: (taskId) => api.post(`/tasks/${taskId}/unclaim`),
  completeTask: (taskId, variables) => api.post(`/tasks/${taskId}/complete`, variables),
};

export default api;