import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { processApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import axios from 'axios';

export default function StartProcess() {
  const [wage, setWage] = useState('');
  const [requestDate, setRequestDate] = useState(new Date().toISOString().split('T')[0]); // Default to today
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();

  // Check if user is authenticated
  useEffect(() => {
    if (!isAuthenticated && !loading) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate, loading]);

  const startProcess = async () => {
    // Validate input
    if (!wage || isNaN(wage) || parseInt(wage) <= 0) {
      setError('Please enter a valid wage amount (positive number)');
      return;
    }
    
    if (!requestDate) {
      setError('Please select a request date');
      return;
    }
    
    setLoading(true);
    setError('');
    setSuccess('');
    
    try {
      // Create process request object
      const processData = {
        wage: parseInt(wage),
        requestDate: requestDate
      };
      
      console.log('Sending process data:', processData);
      
      // Get the token directly from localStorage for this request
      const token = localStorage.getItem('auth_token');
      
      // Make a direct axios call with explicit CORS settings
      const response = await axios({
        method: 'post',
        url: 'http://localhost:8080/process/start',
        data: processData,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json'
        },
        withCredentials: false
      });
      
      console.log('Process started successfully:', response.data);
      
      setSuccess(`Process started successfully! Process Instance Key: ${response.data.processInstanceKey}`);
      setWage('');
      // Keep the date as is for convenience
      
    } catch (err) {
      console.error('Error starting process:', err);
      let errorMessage = 'Failed to start process. Please try again.';
      
      if (err.response) {
        console.error('Error response:', err.response);
        errorMessage = `Server error: ${err.response.status} ${err.response.statusText}`;
      } else if (err.request) {
        console.error('Error request:', err.request);
        errorMessage = 'Network error: No response received from server. Please check if the backend is running and CORS is properly configured.';
      } else {
        console.error('Error message:', err.message);
        errorMessage = `Error: ${err.message}`;
      }
      
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    startProcess();
  };

  return (
    <div>
      <h2>Start Registration Process</h2>
      
      {error && <div className="error message">{error}</div>}
      {success && <div className="success message">{success}</div>}
      
      <form onSubmit={handleSubmit} className="form-container">
        <div className="form-group">
          <label htmlFor="wage">Wage Amount:</label>
          <input
            type="number"
            id="wage"
            value={wage}
            onChange={(e) => setWage(e.target.value)}
            placeholder="Enter wage amount"
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="requestDate">Request Date:</label>
          <input
            type="date"
            id="requestDate"
            value={requestDate}
            onChange={(e) => setRequestDate(e.target.value)}
            required
          />
        </div>
        
        <button 
          type="submit" 
          disabled={loading}
          className="submit-button"
        >
          {loading ? 'Starting Process...' : 'Start Process'}
        </button>
      </form>
    </div>
  );
}