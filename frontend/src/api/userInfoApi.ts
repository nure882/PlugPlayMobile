import {baseApi} from "./baseApi.ts";
import {User} from "../models/User.ts";

export const userInfoApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getUserByToken: builder.query<{ user: User }, string>({
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


interface UpdateProfileRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  email?: string;
}

interface UpdateProfileResponse {
  success: boolean;
  message?: string;
}

export const updateUserProfile = async (payload: UpdateProfileRequest): Promise<UpdateProfileResponse> => {
  await new Promise(resolve => setTimeout(resolve, 300));
  console.log('updateUserProfile placeholder called with', payload);
  return { success: true };
};
