import axios from 'axios';
import {storage} from "./StorageService.ts";
import {API_BASE_URL} from '../redux/authApi.ts';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.request.use(
  (config) => {
    const token = storage.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(token => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        }).catch(err => {
          return Promise.reject(err);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = storage.getRefreshToken();
        
        const response = await api.post('/auth/refresh', { refreshToken });
        
        const { token, refreshToken: newRefreshToken } = response.data;

        storage.setTokens(token, newRefreshToken || refreshToken);
        api.defaults.headers.Authorization = `Bearer ${token}`;
        processQueue(null, token);

        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        storage.clearTokens();
        window.location.href = '/signin';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;