import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { taskApi, processApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function TaskDetail() {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  const [task, setTask] = useState(null);
  const [variables, setVariables] = useState({});
  const [processVariables, setProcessVariables] = useState({});
  const [taskVariables, setTaskVariables] = useState([]);
  const [taskVariablesLoading, setTaskVariablesLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [processLoading, setProcessLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const fetchTaskDetails = async () => {
      setLoading(true);
      setError('');

      try {
        const response = await taskApi.getTaskById(taskId);
        setTask(response.data);

        // Initialize variables based on task form fields if available
        if (response.data.variables) {
          const initialVariables = {};
          Object.keys(response.data.variables).forEach(key => {
            initialVariables[key] = response.data.variables[key].value;
          });
          setVariables(initialVariables);
        }

        // If we have a process instance key, fetch process variables
        if (response.data.processInstanceKey) {
          fetchProcessVariables(response.data.processInstanceKey);
        }
        fetchTaskVariables();
      } catch (err) {
        console.error('Error fetching task details:', err);
        setError('Failed to load task details. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    const fetchTaskVariables = async () => {
      setTaskVariablesLoading(true);
      try {
        const response = await taskApi.getTaskVariables(taskId);
        console.log('Task variables data:', response.data);
        setTaskVariables(response.data || []);
      } catch (err) {
        console.error('Error fetching task variables:', err);
        // Don't set error state here to avoid blocking the UI
      } finally {
        setTaskVariablesLoading(false);
      }
    };

    const fetchProcessVariables = async (processInstanceKey) => {
      if (!processInstanceKey) return;

      setProcessLoading(true);
      try {
        const response = await processApi.getProcessInstance(processInstanceKey);
        console.log('Process instance data:', response.data);

        // Handle the new response format
        if (response.data) {
          if (response.data.variables) {
            // Direct variables object
            setProcessVariables(response.data.variables);
          } else if (response.data.processInstance && response.data.variables) {
            // New DTO format with separate variables field
            setProcessVariables(response.data.variables);
          }
        }
      } catch (err) {
        console.error('Error fetching process variables:', err);
        // Don't set error state here to avoid blocking the UI
      } finally {
        setProcessLoading(false);
      }
    };

    fetchTaskDetails();
  }, [taskId]);

  const handleUnclaimTask = async () => {
    try {
      await taskApi.unclaimTask(taskId);
      setSuccess('Task unclaimed successfully');
      setTimeout(() => navigate('/tasks'), 1500);
    } catch (err) {
      console.error('Error unclaiming task:', err);
      setError('Failed to unclaim task. Please try again.');
    }
  };

  const handleCompleteTask = async () => {
    try {
      await taskApi.completeTask(taskId, variables);
      setSuccess('Task completed successfully');
      setTimeout(() => navigate('/tasks'), 1500);
    } catch (err) {
      console.error('Error completing task:', err);
      setError('Failed to complete task. Please try again.');
    }
  };

  const handleInputChange = (key, value) => {
    setVariables(prev => ({
      ...prev,
      [key]: value
    }));
  };
  
  // Helper function to find variable value from different sources
  const findVariableValue = (name, defaultValue = 'Not available') => {
    // First check in task variables from the dedicated endpoint
    const taskVar = taskVariables.find(v => v.name === name);
    if (taskVar && taskVar.value !== undefined) {
      return taskVar.value;
    }
    
    // Then check in process variables
    if (processVariables[name] !== undefined) {
      const procVar = processVariables[name];
      return typeof procVar === 'object' && procVar.value !== undefined ? procVar.value : procVar;
    }
    
    // Finally check in task variables from the task object
    if (task.variables) {
      const taskObjVar = Object.entries(task.variables).find(([key]) => key === name);
      if (taskObjVar) {
        const [_, variable] = taskObjVar;
        return typeof variable === 'object' && variable.value !== undefined ? variable.value : variable;
      }
    }
    
    return defaultValue;
  };

  if (loading) {
    return <p>Loading task details...</p>;
  }

  if (error) {
    return <div className="error message">{error}</div>;
  }

  if (!task) {
    return <p>Task not found</p>;
  }

  return (
    <div className="task-detail">
      <h2>{task.name}</h2>

      {success && <div className="success message">{success}</div>}

      <div>
        <p><strong>Created:</strong> {new Date(task.creationDate).toLocaleString()}</p>
        <p><strong>Process Instance:</strong> {task.processInstanceKey}</p>
        <p><strong>Assignee:</strong> {task.assignee || 'Unassigned'}</p>
      </div>

      
        
        {/* Read-only Task Variables Section */}
        <div style={{ marginTop: '20px' }}>
          <h4>Additional Task Information</h4>
          
          {taskVariablesLoading ? (
            <p>Loading additional task information...</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
              {/* Wage Field */}
              <div className="form-group">
                <label htmlFor="wage" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  Wage:
                </label>
                <input
                  type="text"
                  id="wage"
                  readOnly
                  value={(() => {
                    const wageValue = findVariableValue('wage') || 
                                     findVariableValue('salary') || 
                                     findVariableValue('income');
                    
                    if (wageValue && !isNaN(parseFloat(wageValue))) {
                      // Format as currency if it's a number
                      return new Intl.NumberFormat('en-US', { 
                        style: 'currency', 
                        currency: 'USD',
                        minimumFractionDigits: 2
                      }).format(parseFloat(wageValue));
                    }
                    return wageValue;
                  })()}
                  style={{
                    width: '100%',
                    padding: '8px',
                    border: '1px solid #ddd',
                    borderRadius: '4px',
                    backgroundColor: '#f9f9f9'
                  }}
                />
              </div>
              
              {/* Request Date Field */}
              <div className="form-group">
                <label htmlFor="requestDate" style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
                  Request Date:
                </label>
                <input
                  type="text"
                  id="requestDate"
                  readOnly
                  value={(() => {
                    // Try different possible variable names for the request date
                    const dateValue = findVariableValue('requestDate', null) || 
                                     findVariableValue('request_date', null) || 
                                     findVariableValue('date', null);
                    
                    if (dateValue) {
                      try {
                        // Try to format the date if it's a valid date string
                        return new Date(dateValue).toLocaleDateString();
                      } catch (e) {
                        return dateValue;
                      }
                    }
                    return 'Not available';
                  })()}
                  style={{
                    width: '100%',
                    padding: '8px',
                    border: '1px solid #ddd',
                    borderRadius: '4px',
                    backgroundColor: '#f9f9f9'
                  }}
                />
              </div>
            </div>
          )}
        </div>

      <div className="task-actions" style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
        {/* Only show complete button if user is assigned to this task or is admin */}
        {(task.assignee === user?.username || user?.roles?.includes('admin')) && (
          <button
            onClick={handleCompleteTask}
            style={{
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              padding: '10px 15px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Complete Task
          </button>
        )}

        {/* Only show unclaim button if task is assigned and user is admin or the assignee */}
        {task.assignee && (task.assignee === user?.username || user?.roles?.includes('admin')) && (
          <button
            onClick={handleUnclaimTask}
            style={{
              backgroundColor: '#dc3545',
              color: 'white',
              border: 'none',
              padding: '10px 15px',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Unclaim Task
          </button>
        )}

        <button
          onClick={() => navigate('/tasks')}
          style={{
            backgroundColor: '#6c757d',
            color: 'white',
            border: 'none',
            padding: '10px 15px',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Back to Tasks
        </button>
      </div>
    </div>
  );
}