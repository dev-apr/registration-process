import { useAuth } from '../context/AuthContext';
import { Link } from 'react-router-dom';

export default function Home() {
  const { user } = useAuth();

  return (
    <div className="home-container">
      <h2>Welcome, {user?.username || 'User'}!</h2>
      
      <div className="dashboard-card">
        <h3>Your Dashboard</h3>
        <p>You are logged in as: {user?.isAdmin ? 'Admin' : 'User'}</p>
        
        <div className="dashboard-actions">
          <h4>Quick Actions</h4>
          <ul>
            <li>
              <Link to="/start-process">Start a new registration process</Link>
              <p>Begin a new registration workflow with your information</p>
            </li>
            <li>
              <Link to="/tasks">View your tasks</Link>
              <p>See and manage tasks assigned to you</p>
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
}