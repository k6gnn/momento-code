import axios from 'axios';
import { getApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

const api = axios.create({
  baseURL: process.env.EXPO_PUBLIC_API_BASE_URL,
  timeout: 10000
});

api.interceptors.request.use(async config => {
  // Use getApp() to ensure we reference the same Firebase instance
  // that AuthContext initialised, not a second default app.
  const user = getAuth(getApp()).currentUser;
  if (user) {
    const token = await user.getIdToken();
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
