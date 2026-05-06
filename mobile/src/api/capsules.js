import api from './client';

export const getNearbyCapsules = (latitude, longitude, radiusMeters = 50) =>
  api.get('/capsules/nearby', { params: { latitude, longitude, radiusMeters } });

export const createCapsule = payload => api.post('/capsules', payload);
export const unlockCapsule = (capsuleId, payload) => api.post(`/capsules/${capsuleId}/unlock`, payload);
export const getProfile = () => api.get('/users/me');
export const getHistory = () => api.get('/discoveries/me');
export const getMyCapsules = () => api.get('/capsules/mine');
export const deleteCapsule = capsuleId => api.delete(`/capsules/${capsuleId}`);
