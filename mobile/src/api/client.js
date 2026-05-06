import axios from 'axios';
import { getApps, getApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const api = axios.create({
  baseURL: process.env.EXPO_PUBLIC_API_BASE_URL,
  timeout: 15000,
});

api.interceptors.request.use(
  async config => {
    // Guard: Firebase must already be initialized before any request.
    if (getApps().length === 0) return config;

    const currentUser = getAuth(getApp()).currentUser;
    if (currentUser) {
      try {
        const token = await currentUser.getIdToken(false);
        config.headers.Authorization = `Bearer ${token}`;
      } catch (err) {
        console.warn('[api] getIdToken failed:', err.message);
      }
    }

    // If the body is FormData, delete the Content-Type header so Axios
    // does NOT set 'application/json' and instead lets the runtime set
    // 'multipart/form-data; boundary=...' automatically.
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }

    return config;
  },
  error => Promise.reject(error),
);

api.interceptors.response.use(
  response => response,
  error => {
    if (!error.response) {
      error.message = 'Unable to reach the server. Check your internet connection and try again.';
    }
    return Promise.reject(error);
  },
);

export default api;