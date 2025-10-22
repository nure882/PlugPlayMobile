import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';

export const API_BASE_URL = 'http://localhost:5298'; // port may be different

export const baseApi = createApi({
  reducerPath: 'api',
  baseQuery: fetchBaseQuery({ baseUrl: `${API_BASE_URL}/api` }),
  endpoints: () => ({}),
});