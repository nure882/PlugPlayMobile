import {baseApi} from "./baseApi.ts";

export const userInfoApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getUserByToken: builder.query<{ FirstName: string; LastName: string }, string>({
      query: (token) => ({
        url: `userinfo/${token}`,
        method: 'GET',
      }),
    }),
  }),
});

export const {
  useGetUserByTokenQuery,
} = userInfoApi;