import { useState, useLayoutEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import BoardView from './components/BoardView';
import './App.css';

export default function App() {
  const [currentView, setCurrentView] = useState('login');
  const [theme, setTheme] = useState(() =>
    localStorage.getItem('kanban-theme') === 'dark' ? 'dark' : 'light'
  );

  useLayoutEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    const nextTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(nextTheme);
    localStorage.setItem('kanban-theme', nextTheme);
  };

  return (
      <>
        {currentView === 'login' && (
            <Login
                onSwitchToRegister={() => setCurrentView('register')}
                onLoginSuccess={() => setCurrentView('board')}
            />
        )}

        {currentView === 'register' && (
            <Register
                onSwitchToLogin={() => setCurrentView('login')}
                onRegisterSuccess={() => setCurrentView('board')}
            />
        )}

        {currentView === 'board' && (
            <BoardView
                theme={theme}
                toggleTheme={toggleTheme}
                onLogout={() => setCurrentView('login')}
            />
        )}
      </>
  );
}
