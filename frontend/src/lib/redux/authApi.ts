import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import {User} from "../models/User.ts";
import { storage } from '../utils/StorageService.ts';

export const API_BASE_URL = 'http://localhost:5298'; // port may be different

// interface ApiError {
//   message: string;
//   status?: number;
// }

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  phoneNumber: string | null;
  firstName: string;
  lastName: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  expiration: string;
  user: User;
}

export const authApi = createApi({
  reducerPath: 'authApi',
  baseQuery: fetchBaseQuery({
    baseUrl: `${API_BASE_URL}/api`,
    prepareHeaders: (headers) => {
      const token = storage.getAccessToken();
      if (token) headers.set('Authorization', `Bearer ${token}`);
      return headers;
    }
  }),
  endpoints: (builder) => ({
    login: builder.mutation<LoginResponse, LoginRequest>({
      query: (credentials) => ({
        url: 'auth/login',
        method: 'POST',
        body: credentials,
      }),
    }),
    register: builder.mutation<void, RegisterRequest>({
      query: (userData) => ({
        url: 'auth/register',
        method: 'POST',
        body: userData,
      }),
    }),
    logout: builder.mutation<void, { refreshToken: string }>({
      query: ({ refreshToken }) => ({
        url: 'auth/logout',
        method: 'POST',
        body: { refreshToken },
      }),
    }),
    verify: builder.query<User, void>({
      query: () => ({
        url: 'auth/verify',
        method: 'POST',
      }),
    }),
    refreshToken: builder.mutation<LoginResponse, { refreshToken: string }>({
      query: ({ refreshToken }) => ({
        url: 'auth/refresh',
        method: 'POST',
        body: { refreshToken },
      }),
    }),
    createAdmin: builder.mutation<void, RegisterRequest>({
      query: (userData) => ({
        url: 'auth/createAdmin',
        method: 'POST',
        body: userData,
      }),
    }),
  }),
});
export const {
  useLoginMutation,
  useRegisterMutation,
  useLogoutMutation,
  useVerifyQuery,
  useRefreshTokenMutation,
  useCreateAdminMutation,
} = authApi;
