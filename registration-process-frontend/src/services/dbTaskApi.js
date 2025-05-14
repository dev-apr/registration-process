import api from './api';

// Database-backed task assignment API calls
export const dbTaskApi = {
  // Claim a task using database-backed round-robin assignment
  claimTaskWithDbRoundRobin: (taskId) => api.post(`/tasks-db/${taskId}/claim-db`),
  
  // Claim a task for a specific role using database-backed round-robin assignment
  claimTaskForRole: (taskId, role) => api.post(`/tasks-db/${taskId}/claim-role/${role}`),
};

export default dbTaskApi;