import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  
  const { isAuthenticated, loginWithCredentials, loading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // If already authenticated, redirect to home
    if (isAuthenticated) {
      navigate('/');
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!username || !password) {
      setError('Please enter both username and password');
      return;
    }
    
    setIsLoggingIn(true);
    setError('');
    
    try {
      console.log('Login form submitted for user:', username);
      await loginWithCredentials(username, password);
      console.log('Login successful');
      // Navigation will happen automatically via the useEffect when isAuthenticated changes
    } catch (err) {
      console.error('Login error:', err);
      
      // More detailed error logging
      if (err.response) {
        console.error('Error response data:', err.response.data);
        console.error('Error response status:', err.response.status);
        console.error('Error response headers:', err.response.headers);
        
        // Extract the specific error message
        const errorDescription = err.response.data?.error_description;
        const errorType = err.response.data?.error;
        
        if (errorType === 'invalid_grant' && errorDescription?.includes('Invalid user credentials')) {
          setError('Invalid username or password. Please check your credentials and try again.');
        } else if (errorType === 'invalid_client') {
          setError('Authentication failed: Invalid client configuration. Please contact the administrator.');
        } else {
          setError(errorDescription || 
                  err.response.data?.message || 
                  `Server error (${err.response.status}). Please try again later.`);
        }
      } else if (err.request) {
        console.error('Error request:', err.request);
        setError('Unable to connect to the authentication server. Please check your network connection and try again.');
      } else {
        console.error('Error message:', err.message);
        setError(err.message || 'An unexpected error occurred. Please try again.');
      }
    } finally {
      setIsLoggingIn(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="login-container">
      
      <div className="login-card">
        <h3>Login</h3>
        
        {error && <div className="error message">{error}</div>}
        
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your username"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
            />
          </div>
          
          <button 
            type="submit" 
            className="login-button" 
            disabled={isLoggingIn}
          >
            {isLoggingIn ? 'Logging in...' : 'Log In'}
          </button>
        </form>
      </div>
    </div>
  );
}