import api from './api';

export interface SendNotificationRequest {
  channelName: string; // e.g. "EMAIL", "SMS"
  recipientAddress: string;
  templateId?: string;
  payload?: Record<string, any>;
  priority?: string;
  idempotencyKey: string;
}

export const sendNotification = async (request: SendNotificationRequest) => {
  const response = await api.post('/notifications/send', request);
  return response.data;
};
