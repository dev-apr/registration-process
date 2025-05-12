import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { taskApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

export default function TaskList() {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [viewMode, setViewMode] = useState('my');
  // const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  useEffect(() => {
    const fetchTasks = async () => {
      if (!user?.username) return;

      setLoading(true);
      setError('');

      try {
        let response;

        switch (viewMode) {
          case 'unassigned':
            console.log('Fetching unassigned tasks, user:', user);
            try {
              response = await taskApi.getUnassignedTasks();
              console.log('Unassigned tasks response:', response);
            } catch (err) {
              console.error('Error fetching unassigned tasks:', err);
              response = { data: { tasks: [] } };
            }
            break;

          case 'all':
            console.log('Fetching all tasks (admin view)');
            try {
              response = await taskApi.getAllTasks();
              console.log('All tasks response:', response);
            } catch (err) {
              console.error('Error fetching all tasks:', err);
              response = { data: { tasks: [] } };
            }
            break;

          case 'my':
          default:
            console.log('Fetching tasks for user:', user.username);
            try {
              response = await taskApi.getTasksByUser(user.username);
              console.log('User tasks response:', response);
            } catch (err) {
              console.error('Error fetching user tasks:', err);
              response = { data: { tasks: [] } };
            }
            break;
        }

        // Log the entire response for debugging
        console.log('Full response:', response);

        // Handle error response
        if (response.data && response.data.error) {
          console.error('Error in response:', response.data.error);
          setError(`Server error: ${response.data.error}`);
          setTasks([]);
          return;
        }

        // Handle tasks array
        if (response.data && Array.isArray(response.data.tasks)) {
          setTasks(response.data.tasks);
          console.log('Tasks set:', response.data.tasks);
        } else if (response.data && response.data.tasks === null) {
          console.warn('Tasks is null in response');
          setTasks([]);
        } else if (response.data) {
          // Try to extract tasks from the response in a different way
          console.warn('Trying to extract tasks from response in a different format');
          const extractedTasks = response.data.items || response.data.content || [];
          setTasks(extractedTasks);
          console.log('Extracted tasks:', extractedTasks);
        } else {
          console.warn('No data found in response');
          setTasks([]);
        }
      } catch (err) {
        console.error('Error in task fetching process:', err);
        if (err.response) {
          console.error('Error response:', err.response);
          setError(`Failed to load tasks: ${err.response.status} ${err.response.statusText}`);
        } else if (err.request) {
          console.error('Error request:', err.request);
          setError('Network error: No response received from server.');
        } else {
          setError(`Error: ${err.message}`);
        }
        setTasks([]);
      } finally {
        setLoading(false);
      }
    };

    fetchTasks();
  }, [user, viewMode]);

  const handleClaimTask = async (taskId) => {
    try {
      await taskApi.claimTask(taskId);

      // Refresh task list after claiming
      if (viewMode === 'unassigned') {
        const response = await taskApi.getUnassignedTasks();
        setTasks(response.data.tasks || response.data.items || []);
      } else if (viewMode === 'all') {
        const response = await taskApi.getAllTasks();
        setTasks(response.data.tasks || response.data.items || []);
      }
    } catch (err) {
      console.error('Error claiming task:', err);
      setError('Failed to claim task. Please try again.');
    }
  };

  const setView = (mode) => {
    console.log('Setting view mode to:', mode);
    setViewMode(mode);
  };

  // const getViewTitle = () => {
  //   switch (viewMode) {
  //     case 'unassigned': return 'Unassigned Tasks';
  //     case 'all': return 'All Tasks (Admin View)';
  //     default: return 'My Tasks';
  //   }
  // };

  return (
    <div>
      {/* <h2>{getViewTitle()}</h2> */}

      {/* View selection buttons */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <button
          onClick={() => setView('my')}
          style={{
            backgroundColor: viewMode === 'my' ? '#007bff' : '#6c757d',
            color: 'white',
            border: 'none',
            padding: '8px 15px',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          My Tasks
        </button>

        {user?.roles?.includes('admin') && (
          <button
            onClick={() => setView('unassigned')}
            style={{
              backgroundColor: viewMode === 'unassigned' ? '#007bff' : '#6c757d',
              color: 'white',
              border: 'none',
              padding: '8px 15px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Unassigned Tasks
          </button>
        )}

        {/* Admin view button - only show for admin users */}
        {user?.roles?.includes('admin') && (
          <button
            onClick={() => setView('all')}
            style={{
              backgroundColor: viewMode === 'all' ? '#007bff' : '#6c757d',
              color: 'white',
              border: 'none',
              padding: '8px 15px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            All Tasks (Admin)
          </button>
        )}
      </div>

      {error && <div className="error message">{error}</div>}

      {loading ? (
        <div style={{ textAlign: 'center', padding: '20px' }}>
          <p>Loading tasks...</p>
        </div>
      ) : tasks.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '5px' }}>
          <p>No tasks found.</p>
        </div>
      ) : (
        <div className="task-list" style={{ marginTop: '20px' }}>
          {tasks.map(task => (
            <div
              key={task.id}
              className="task-item"
              style={{
                backgroundColor: '#f8f9fa',
                borderRadius: '5px',
                padding: '15px',
                marginBottom: '15px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.1)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}
            >
              <div>
                <h3 style={{ margin: '0 0 10px 0', color: '#343a40' }}>{task.name}</h3>
                <p style={{ margin: '5px 0', color: '#6c757d', fontSize: '14px' }}>
                  <strong>Created:</strong> {task.creationDate ? new Date(task.creationDate).toLocaleString() : 'Not available'}
                </p>
                <p style={{ margin: '5px 0', color: '#6c757d', fontSize: '14px' }}>
                  <strong>Process Instance:</strong> {task.processInstanceKey || 'Not available'}
                </p>

                <p style={{ margin: '5px 0', color: '#6c757d', fontSize: '14px' }}>
                  <strong>Assignee:</strong> {task.assignee ? task.assignee : "Unassigned"}
                </p>

              </div>
              <div>
                {viewMode === 'unassigned' ? (
                  <button
                    onClick={() => handleClaimTask(task.id)}
                    style={{
                      backgroundColor: '#28a745',
                      color: 'white',
                      border: 'none',
                      padding: '8px 15px',
                      borderRadius: '4px',
                      cursor: 'pointer'
                    }}
                  >
                    Claim Task
                  </button>
                ) : (
                  <Link to={`/task/${task.id}`} style={{ textDecoration: 'none' }}>
                    <button
                      style={{
                        backgroundColor: '#007bff',
                        color: 'white',
                        border: 'none',
                        padding: '8px 15px',
                        borderRadius: '4px',
                        cursor: 'pointer'
                      }}
                    >
                      View Details
                    </button>
                  </Link>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}