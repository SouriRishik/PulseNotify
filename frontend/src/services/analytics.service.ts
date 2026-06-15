import api from './api';

export interface DashboardMetrics {
  totalNotifications: number;
  successRate: number;
  channelBreakdown: Record<string, number>;
  statusBreakdown: Record<string, number>;
}

export const getDashboardMetrics = async (): Promise<DashboardMetrics> => {
  const response = await api.get('/analytics/dashboard');
  return response.data;
};
