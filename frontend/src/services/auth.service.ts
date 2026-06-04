import api from './api';

export const login = async (email: string, password: string) => {
  const response = await api.post('/auth/signin', { email, password });
  if (response.data.token) {
    localStorage.setItem('token', response.data.token);
    localStorage.setItem('user', JSON.stringify(response.data));
  }
  return response.data;
};

export const register = async (username: string, email: string, password: string) => {
  const response = await api.post('/auth/signup', { username, email, password });
  return response.data;
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};

export const getCurrentUser = () => {
  const userStr = localStorage.getItem('user');
  if (userStr) return JSON.parse(userStr);
  return null;
};
