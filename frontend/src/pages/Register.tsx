import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../services/auth.service';
import { Activity } from 'lucide-react';

const Register: React.FC = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await register(username, email, password);
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', width: '100%' }}>
      <div className="glass-panel animate-fade-in" style={{ padding: '3rem', width: '100%', maxWidth: '400px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '2rem' }}>
          <Activity color="var(--primary)" size={40} style={{ marginRight: '10px' }} />
          <h1 style={{ margin: 0, color: 'var(--primary)' }}>PulseNotify</h1>
        </div>
        
        <h2 style={{ textAlign: 'center', marginBottom: '1.5rem' }}>Create Account</h2>
        
        {error && <div style={{ color: '#ef4444', marginBottom: '1rem', textAlign: 'center' }}>{error}</div>}
        {success && <div style={{ color: '#10b981', marginBottom: '1rem', textAlign: 'center' }}>Account created! Redirecting to login...</div>}
        
        <form onSubmit={handleRegister}>
          <div className="form-group">
            <label className="form-label">Username</label>
            <input 
              type="text" 
              className="input-glass" 
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input 
              type="email" 
              className="input-glass" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input 
              type="password" 
              className="input-glass" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn-primary" style={{ width: '100%', marginTop: '1rem' }} disabled={success}>
            Sign Up
          </button>
        </form>
        <div style={{ marginTop: '1.5rem', textAlign: 'center' }}>
          <Link to="/login" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>
            Already have an account? <span style={{ color: 'var(--primary)' }}>Sign In</span>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
