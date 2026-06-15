import api from './api';

export interface Template {
  id: string;
  name: string;
  subject?: string;
  body: string;
  isActive: boolean;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export const getAllTemplates = async (): Promise<Template[]> => {
  const response = await api.get('/templates');
  return response.data;
};

export const createTemplate = async (data: Partial<Template>): Promise<Template> => {
  const response = await api.post('/templates', data);
  return response.data;
};

export const updateTemplate = async (id: string, data: Partial<Template>): Promise<Template> => {
  const response = await api.put(`/templates/${id}`, data);
  return response.data;
};

export const deleteTemplate = async (id: string): Promise<void> => {
  await api.delete(`/templates/${id}`);
};

export const toggleTemplateStatus = async (id: string, active: boolean): Promise<Template> => {
  const response = await api.patch(`/templates/${id}/status?active=${active}`);
  return response.data;
};
