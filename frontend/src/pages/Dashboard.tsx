import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { logout } from '../services/auth.service';
import { useNavigate } from 'react-router-dom';
import { Bell, CheckCircle, BarChart3, LogOut } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

const Dashboard: React.FC = () => {
  const [metrics, setMetrics] = useState<any>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const res = await api.get('/analytics/dashboard');
        setMetrics(res.data);
      } catch (err) {
        console.error('Failed to fetch metrics', err);
      }
    };
    fetchMetrics();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const chartData = metrics ? Object.entries(metrics.channelBreakdown).map(([name, value]) => ({ name, value })) : [];

  return (
    <div style={{ display: 'flex', width: '100%' }}>
      {/* Sidebar */}
      <div className="glass-panel" style={{ width: '250px', height: '95vh', margin: '2.5vh', display: 'flex', flexDirection: 'column' }}>
        <div style={{ padding: '2rem', display: 'flex', alignItems: 'center' }}>
           <h2 style={{ color: 'var(--primary)', margin: 0 }}>PulseNotify</h2>
        </div>
        <div style={{ flex: 1, padding: '1rem' }}>
           <div style={{ display: 'flex', alignItems: 'center', padding: '1rem', cursor: 'pointer', background: 'rgba(255,255,255,0.05)', borderRadius: '8px', color: 'var(--primary)' }}>
             <BarChart3 size={20} style={{ marginRight: '10px' }} /> Dashboard
           </div>
        </div>
        <div style={{ padding: '1rem' }}>
           <div onClick={handleLogout} style={{ display: 'flex', alignItems: 'center', padding: '1rem', cursor: 'pointer', color: 'var(--text-muted)' }}>
             <LogOut size={20} style={{ marginRight: '10px' }} /> Logout
           </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="main-content animate-fade-in">
        <h1 style={{ marginBottom: '2rem' }}>Dashboard Overview</h1>
        
        {!metrics ? (
          <div>Loading metrics...</div>
        ) : (
          <>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '2rem', marginBottom: '2rem' }}>
              <div className="glass-panel" style={{ padding: '2rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                  <Bell color="var(--primary)" size={24} style={{ marginRight: '10px' }} />
                  <h3 style={{ margin: 0 }}>Total Sent</h3>
                </div>
                <h1 style={{ fontSize: '3rem', margin: 0 }}>{metrics.totalNotifications}</h1>
              </div>

              <div className="glass-panel" style={{ padding: '2rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                  <CheckCircle color="#10b981" size={24} style={{ marginRight: '10px' }} />
                  <h3 style={{ margin: 0 }}>Success Rate</h3>
                </div>
                <h1 style={{ fontSize: '3rem', margin: 0 }}>{metrics.successRate}%</h1>
              </div>
            </div>

            <div className="glass-panel" style={{ padding: '2rem', height: '400px' }}>
              <h3 style={{ marginBottom: '2rem' }}>Channel Breakdown</h3>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                  <XAxis dataKey="name" stroke="var(--text-muted)" />
                  <YAxis stroke="var(--text-muted)" />
                  <Tooltip contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '8px' }} />
                  <Bar dataKey="value" fill="var(--primary)" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
