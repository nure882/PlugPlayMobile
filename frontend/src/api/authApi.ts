import {baseApi} from './baseApi.ts';
import {User} from "../models/User.ts";

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

export const authApi = baseApi.injectEndpoints({
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
      query: ({refreshToken}) => ({
        url: 'auth/logout',
        method: 'POST',
        body: {refreshToken},
      }),
    }),
    verify: builder.query<User, void>({
      query: () => ({
        url: 'auth/verify',
        method: 'POST',
      }),
    }),
    refreshToken: builder.mutation<LoginResponse, { refreshToken: string }>({
      query: ({refreshToken}) => ({
        url: 'auth/refresh',
        method: 'POST',
        body: {refreshToken},
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
} = authApi;
