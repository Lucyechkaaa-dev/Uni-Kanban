import { useState, useLayoutEffect, useEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import BoardView from './components/BoardView';
import { api, setUnauthorizedHandler } from './api';
import './App.css';

export default function App() {
   const [currentView, setCurrentView] = useState('loading');
  const [user, setUser] = useState(null);
  const [theme, setTheme] = useState(() =>
    localStorage.getItem('kanban-theme') === 'dark' ? 'dark' : 'light'
  );

  useLayoutEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

    useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null);
      setCurrentView('login');
    });
  }, []);

   useEffect(() => {
    let cancelled = false;
    api.me()
      .then((me) => {
        if (cancelled) return;
        setUser(me);
        setCurrentView('board');
      })
      .catch(() => {
        if (!cancelled) setCurrentView('login');
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(nextTheme);
    localStorage.setItem('kanban-theme', nextTheme);
  };

  const handleAuthSuccess = (loggedInUser) => {
    setUser(loggedInUser);
    setCurrentView('board');
  };

  const handleLogout = async () => {
    try {
      await api.logout();
    } catch {
     }
    setUser(null);
    setCurrentView('login');
  };

  return (
      <>
        {currentView === 'loading' && (
            <div className="auth-container">Loading…</div>
        )}

        {currentView === 'login' && (
            <Login
                onSwitchToRegister={() => setCurrentView('register')}
                onLoginSuccess={handleAuthSuccess}
            />
        )}

        {currentView === 'register' && (
            <Register
                onSwitchToLogin={() => setCurrentView('login')}
                onRegisterSuccess={handleAuthSuccess}
            />
        )}

        {currentView === 'board' && user && (
            <BoardView
                user={user}
                theme={theme}
                toggleTheme={toggleTheme}
                onLogout={handleLogout}
            />
        )}
      </>
  );
}
