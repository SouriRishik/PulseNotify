import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { BarChart3, LayoutTemplate, Send, LogOut } from 'lucide-react';
import { logout } from '../services/auth.service';

interface SidebarProps {
  children: React.ReactNode;
}

const Layout: React.FC<SidebarProps> = ({ children }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: <BarChart3 size={20} /> },
    { path: '/templates', label: 'Templates', icon: <LayoutTemplate size={20} /> },
    { path: '/send', label: 'Send Notification', icon: <Send size={20} /> },
  ];

  return (
    <div style={{ display: 'flex', width: '100%', minHeight: '100vh' }}>
      {/* Sidebar */}
      <div className="glass-panel" style={{ width: '250px', margin: '1rem', display: 'flex', flexDirection: 'column', position: 'sticky', top: '1rem', height: 'calc(100vh - 2rem)' }}>
        <div style={{ padding: '2rem', display: 'flex', alignItems: 'center' }}>
           <h2 style={{ color: 'var(--primary)', margin: 0 }}>PulseNotify</h2>
        </div>
        <div style={{ flex: 1, padding: '1rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
           {navItems.map((item) => (
             <div 
               key={item.path}
               onClick={() => navigate(item.path)}
               style={{ 
                 display: 'flex', 
                 alignItems: 'center', 
                 padding: '1rem', 
                 cursor: 'pointer', 
                 background: location.pathname === item.path ? 'rgba(255,255,255,0.05)' : 'transparent', 
                 borderRadius: '8px', 
                 color: location.pathname === item.path ? 'var(--primary)' : 'var(--text-muted)',
                 transition: 'all 0.2s ease'
               }}
             >
               <span style={{ marginRight: '10px' }}>{item.icon}</span> {item.label}
             </div>
           ))}
        </div>
        <div style={{ padding: '1rem' }}>
           <div onClick={handleLogout} style={{ display: 'flex', alignItems: 'center', padding: '1rem', cursor: 'pointer', color: 'var(--text-muted)' }}>
             <LogOut size={20} style={{ marginRight: '10px' }} /> Logout
           </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="main-content" style={{ flex: 1, padding: '2rem 2rem 2rem 1rem' }}>
        {children}
      </div>
    </div>
  );
};

export default Layout;
