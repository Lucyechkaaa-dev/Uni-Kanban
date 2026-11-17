import { useState, useLayoutEffect, useEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import BoardView from './components/BoardView';
import { api, setUnauthorizedHandler } from './api';
import { THEME, THEME_STORAGE_KEY, VIEW } from './constants';
import './App.css';

export default function App() {
  // the login cookie is invisible to the page, so at the start we do not know whether we are logged in: ask the backend
  const [currentView, setCurrentView] = useState(VIEW.LOADING);
  const [user, setUser] = useState(null);
  const [theme, setTheme] = useState(() =>
    localStorage.getItem(THEME_STORAGE_KEY) === THEME.DARK ? THEME.DARK : THEME.LIGHT
  );

  useLayoutEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  // the login expired or was ended: back to the login page
  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null);
      setCurrentView(VIEW.LOGIN);
    });
  }, []);

  // on every start and after a page reload: "who am I?" - a valid cookie gives the user back, otherwise 401
  useEffect(() => {
    let cancelled = false;
    api.me()
      .then((me) => {
        if (cancelled) return;
        setUser(me);
        setCurrentView(VIEW.BOARD);
      })
      .catch(() => {
        if (!cancelled) setCurrentView(VIEW.LOGIN);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const toggleTheme = () => {
    const nextTheme = theme === THEME.LIGHT ? THEME.DARK : THEME.LIGHT;
    setTheme(nextTheme);
    localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
  };

  const handleAuthSuccess = (loggedInUser) => {
    setUser(loggedInUser);
    setCurrentView(VIEW.BOARD);
  };

  const handleLogout = async () => {
    try {
      await api.logout();
    } catch {
      // even if the request failed, the user leaves this page; a dead login is dropped by the backend on its own
    }
    setUser(null);
    setCurrentView(VIEW.LOGIN);
  };

  return (
      <>
        {currentView === VIEW.LOADING && (
            <div className="auth-container">Loading…</div>
        )}

        {currentView === VIEW.LOGIN && (
            <Login
                onSwitchToRegister={() => setCurrentView(VIEW.REGISTER)}
                onLoginSuccess={handleAuthSuccess}
            />
        )}

        {currentView === VIEW.REGISTER && (
            <Register
                onSwitchToLogin={() => setCurrentView(VIEW.LOGIN)}
                onRegisterSuccess={handleAuthSuccess}
            />
        )}

        {currentView === VIEW.BOARD && user && (
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
