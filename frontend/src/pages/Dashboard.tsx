import React, { useEffect, useState } from 'react';
import { getDashboardMetrics, type DashboardMetrics } from '../services/analytics.service';
import { Bell, CheckCircle } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import Layout from '../components/Layout';

const Dashboard: React.FC = () => {
  const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const data = await getDashboardMetrics();
        setMetrics(data);
      } catch (err) {
        console.error('Failed to fetch metrics', err);
      }
    };
    fetchMetrics();
  }, []);

  const chartData = metrics && metrics.channelBreakdown ? 
    Object.entries(metrics.channelBreakdown).map(([name, value]) => ({ name, value })) : [];

  return (
    <Layout>
      <div className="animate-fade-in">
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
                <h1 style={{ fontSize: '3rem', margin: 0 }}>{metrics.successRate.toFixed(1)}%</h1>
              </div>

              <div className="glass-panel" style={{ padding: '2rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem' }}>
                  <CheckCircle color="var(--secondary)" size={24} style={{ marginRight: '10px' }} />
                  <h3 style={{ margin: 0 }}>Open Rate</h3>
                </div>
                <h1 style={{ fontSize: '3rem', margin: 0 }}>{metrics.openRate.toFixed(1)}%</h1>
              </div>
            </div>

            <div className="glass-panel" style={{ padding: '2rem', height: '400px' }}>
              <h3 style={{ marginBottom: '2rem' }}>Channel Breakdown</h3>
              {chartData.length === 0 ? (
                 <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%', color: 'var(--text-muted)' }}>
                   No notifications sent yet.
                 </div>
              ) : (
                 <ResponsiveContainer width="100%" height="100%">
                   <BarChart data={chartData}>
                     <XAxis dataKey="name" stroke="var(--text-muted)" />
                     <YAxis stroke="var(--text-muted)" allowDecimals={false} />
                     <Tooltip cursor={{ fill: 'rgba(255,255,255,0.05)' }} contentStyle={{ background: 'var(--bg-card)', border: '1px solid var(--border)', borderRadius: '8px' }} />
                     <Bar dataKey="value" fill="var(--primary)" radius={[4, 4, 0, 0]} />
                   </BarChart>
                 </ResponsiveContainer>
              )}
            </div>
          </>
        )}
      </div>
    </Layout>
  );
};

export default Dashboard;

