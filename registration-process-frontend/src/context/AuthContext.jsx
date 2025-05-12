import { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';
import keycloak, { initKeycloak, getToken, updateToken } from '../services/keycloak';

// Create context
const AuthContext = createContext(null);

// Provider component
export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(null);

  useEffect(() => {
    const initAuth = async () => {
      try {
        console.log('Starting auth initialization');
        
        // First check if we have a token in localStorage
        const storedToken = localStorage.getItem('auth_token');
        const storedUser = localStorage.getItem('user_info');
        
        if (storedToken && storedUser) {
          console.log('Found stored token and user info');
          setToken(storedToken);
          setUser(JSON.parse(storedUser));
          setIsAuthenticated(true);
          
          // Configure axios default header
          axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
          setLoading(false);
          return;
        }
        
        // If no stored token, we'll just set loading to false and let the user log in manually
        console.log('No stored credentials found, user will need to log in manually');
      } catch (error) {
        console.error('Auth initialization error:', error);
      } finally {
        setLoading(false);
      }
    };
    
    initAuth();
  }, []);

  // Login with username and password
  const loginWithCredentials = async (username, password) => {
    try {
      console.log('Attempting login with username:', username);
      
      // Make the token request using the proxied URL
      const tokenResponse = await axios.post('/auth/realms/camunda-platform/protocol/openid-connect/token', 
        new URLSearchParams({
          'client_id': 'camunda-app',
          'client_secret': 'tI4bSYrju97cT1T1eEmoFyjug8hRMPft',
          'grant_type': 'password',
          'username': username,
          'password': password,
          'scope': 'openid'
        }), 
        {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
          }
        }
      );
      
      console.log('Token response received:', tokenResponse.status);
      
      const { access_token, refresh_token } = tokenResponse.data;
      
      // Get user info from token
      const userInfo = parseJwt(access_token);
      
      // Create user object
      const user = {
        username: userInfo.preferred_username || userInfo.sub,
        roles: userInfo.realm_access?.roles || [],
        isAdmin: userInfo.realm_access?.roles?.includes('admin') || false,
        isUser: userInfo.realm_access?.roles?.includes('user') || false
      };
      
      // Store token and user info
      localStorage.setItem('auth_token', access_token);
      localStorage.setItem('refresh_token', refresh_token);
      localStorage.setItem('user_info', JSON.stringify(user));
      
      // Update state
      setToken(access_token);
      setUser(user);
      setIsAuthenticated(true);
      
      // Configure axios default header
      axios.defaults.headers.common['Authorization'] = `Bearer ${access_token}`;
      
      return user;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  };

  // Logout function
  const handleLogout = () => {
    // Clear localStorage
    localStorage.removeItem('auth_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_info');
    
    // Clear state
    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
    
    // Remove default header
    delete axios.defaults.headers.common['Authorization'];
  };

  // Helper function to parse JWT
  const parseJwt = (token) => {
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      return null;
    }
  };

  // Get token for API calls
  const getToken = () => {
    return token;
  };

  // Context value
  const value = {
    isAuthenticated,
    user,
    loading,
    token,
    loginWithCredentials,
    logout: handleLogout,
    getToken
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook to use the auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;