import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('sociallens_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('sociallens_token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function verifyToken() {
      if (token) {
        try {
          const res = await authService.getCurrentUser();
          if (res.data) {
            setUser(res.data);
            localStorage.setItem('sociallens_user', JSON.stringify(res.data));
          }
        } catch (err) {
          console.warn('Session verification failed, logging out:', err);
          logout();
        }
      }
      setLoading(false);
    }
    verifyToken();
  }, [token]);

  const login = async (username, password) => {
    const res = await authService.login({ username, password });
    if (res.data && res.data.accessToken) {
      const { accessToken, userId, username: uname, email, role } = res.data;
      const userInfo = { id: userId, username: uname, email, role };
      localStorage.setItem('sociallens_token', accessToken);
      localStorage.setItem('sociallens_user', JSON.stringify(userInfo));
      setToken(accessToken);
      setUser(userInfo);
      return userInfo;
    }
    throw new Error('Login response invalid');
  };

  const register = async (username, email, password, role = 'ANALYST') => {
    const res = await authService.register({ username, email, password, role });
    if (res.data && res.data.accessToken) {
      const { accessToken, userId, username: uname, email: uemail, role: urole } = res.data;
      const userInfo = { id: userId, username: uname, email: uemail, role: urole };
      localStorage.setItem('sociallens_token', accessToken);
      localStorage.setItem('sociallens_user', JSON.stringify(userInfo));
      setToken(accessToken);
      setUser(userInfo);
      return userInfo;
    }
    throw new Error('Registration response invalid');
  };

  const logout = () => {
    localStorage.removeItem('sociallens_token');
    localStorage.removeItem('sociallens_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
