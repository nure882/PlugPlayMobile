import {createApi, fetchBaseQuery} from '@reduxjs/toolkit/query/react';
import {storage} from "../utils/StorageService.ts";

export const API_BASE_URL = 'http://localhost:5298';

export const baseApi = createApi({
  reducerPath: 'api',
  baseQuery: fetchBaseQuery({
    baseUrl: `${API_BASE_URL}/api`,
    prepareHeaders: (headers) => {
      const token = storage.getAccessToken();
      if (token) {
        headers.set('Authorization', `Bearer ${token}`);
      }

      if (!headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json');
      }

      return headers;
    },
  }),
  endpoints: () => ({}),
});
