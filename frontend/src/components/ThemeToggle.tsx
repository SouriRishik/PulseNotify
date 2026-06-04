import React, { useEffect, useState } from 'react';
import { Moon, Sun } from 'lucide-react';

const ThemeToggle: React.FC = () => {
  const [isLight, setIsLight] = useState(false);

  useEffect(() => {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light') {
      setIsLight(true);
      document.documentElement.setAttribute('data-theme', 'light');
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }, []);

  const toggleTheme = () => {
    if (isLight) {
      document.documentElement.removeAttribute('data-theme');
      localStorage.setItem('theme', 'dark');
      setIsLight(false);
    } else {
      document.documentElement.setAttribute('data-theme', 'light');
      localStorage.setItem('theme', 'light');
      setIsLight(true);
    }
  };

  return (
    <button
      onClick={toggleTheme}
      className="glass-panel"
      style={{
        position: 'fixed',
        top: '1.5rem',
        right: '1.5rem',
        width: '45px',
        height: '45px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        border: 'none',
        borderRadius: '50%',
        cursor: 'pointer',
        zIndex: 1000,
        color: 'var(--text-main)'
      }}
      aria-label="Toggle Theme"
    >
      {isLight ? <Moon size={22} /> : <Sun size={22} />}
    </button>
  );
};

export default ThemeToggle;
