import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import { Suspense, lazy } from 'react';
import './App.css';
import { AuthProvider, useAuth } from './context/AuthContext';

// Lazy load components for better performance
const Home = lazy(() => import('./components/Home'));
const StartProcess = lazy(() => import('./components/StartProcess'));
const TaskList = lazy(() => import('./components/TaskList'));
const TaskDetail = lazy(() => import('./components/TaskDetail'));
const Login = lazy(() => import('./components/Login'));

// Protected route component
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div>Loading authentication...</div>;
  }
  
  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }
  
  return children;
};

// Main App component
const AppContent = () => {
  const { isAuthenticated, user, logout, loading } = useAuth();
  
  if (loading) {
    return <div className="loading">Loading application...</div>;
  }
  
  return (
    <div className="app-container">
      <header>
        <h1>Registration Process Application</h1>
        <nav>
          <ul>
            <li><Link to="/">Home</Link></li>
            {isAuthenticated && (
              <>
                <li><Link to="/start-process">Start Process</Link></li>
                <li><Link to="/tasks">Tasks</Link></li>
              </>
            )}
          </ul>
        </nav>
        <div className="user-info">
          {isAuthenticated && user ? (
            <>
              <span>Logged in as: {user.username} ({user.isAdmin ? 'Admin' : 'User'})</span>
              <button onClick={logout}>Logout</button>
            </>
          ) : (
            <span>Not logged in</span>
          )}
        </div>
      </header>
      
      <main>
        <Suspense fallback={<div>Loading...</div>}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/" element={
              isAuthenticated ? <Home /> : <Navigate to="/login" />
            } />
            <Route path="/start-process" element={
              <ProtectedRoute>
                <StartProcess />
              </ProtectedRoute>
            } />
            <Route path="/tasks" element={
              <ProtectedRoute>
                <TaskList />
              </ProtectedRoute>
            } />
            <Route path="/task/:taskId" element={
              <ProtectedRoute>
                <TaskDetail />
              </ProtectedRoute>
            } />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </Suspense>
      </main>
      
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <Router>
        <AppContent />
      </Router>
    </AuthProvider>
  );
}