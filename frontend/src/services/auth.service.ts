import api from './api';

export const login = async (email: string, password: string) => {
  const response = await api.post('/auth/login', { email, password });
  if (response.data.token) {
    localStorage.setItem('token', response.data.token);
    localStorage.setItem('user', JSON.stringify(response.data));
  }
  return response.data;
};

export const register = async (firstName: string, lastName: string, email: string, password: string) => {
  const response = await api.post('/auth/register', { firstName, lastName, email, password });
  return response.data;
};

export const verifyEmail = async (email: string, token: string) => {
  const response = await api.post(`/auth/verify?email=${encodeURIComponent(email)}&token=${encodeURIComponent(token)}`);
  return response.data;
};

export const resendOtp = async (email: string) => {
  const response = await api.post(`/auth/resend-otp?email=${encodeURIComponent(email)}`);
  return response.data;
};

export const forgotPassword = async (email: string) => {
  const response = await api.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`);
  return response.data;
};

export const resetPassword = async (email: string, token: string, newPassword: string) => {
  const response = await api.post('/auth/reset-password', { email, token, newPassword });
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
