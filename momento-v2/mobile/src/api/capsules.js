import api from './client';

/**
 * Fetch capsules within `radiusMeters` of the given coordinates.
 * Default radius matches the backend's configured PROXIMITY_RADIUS_METERS.
 */
export const getNearbyCapsules = (latitude, longitude, radiusMeters = 50) =>
  api.get('/capsules/nearby', { params: { latitude, longitude, radiusMeters } });

/**
 * Drop a new capsule at the current location.
 * `payload` shape: { latitude, longitude, textContent?, media? }
 */
export const createCapsule = payload => api.post('/capsules', payload);

/**
 * Attempt to unlock a capsule (proximity-gated).
 * `payload` shape: { latitude, longitude }
 */
export const unlockCapsule = (capsuleId, payload) =>
  api.post(`/capsules/${capsuleId}/unlock`, payload);

/** Fetch the current user's own profile stats. */
export const getProfile = () => api.get('/users/me');

/** Fetch the current user's discovery history. */
export const getHistory = () => api.get('/discoveries/me');

/** Fetch all capsules dropped by the current user. */
export const getMyCapsules = () => api.get('/capsules/mine');

/** Permanently delete one of the current user's capsules. */
export const deleteCapsule = capsuleId => api.delete(`/capsules/${capsuleId}`);
