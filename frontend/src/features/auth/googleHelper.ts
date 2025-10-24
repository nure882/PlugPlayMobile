import {API_BASE_URL} from "../../api/baseApi.ts";
import {storage} from "../../utils/StorageService.ts";

export const handleGoogleSuccess = async (credentialResponse: any) => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/auth/google`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          idToken: credentialResponse.credential
        })
      });

    const data = await response.json();
    // console.log(data);
    storage.setTokens(data.token, data.refreshToken);

    return data.user;
    // console.log('Login successful:', data.user);
  } catch (error) {
    console.error('Login failed:', error);
  }
};

export const handleGoogleError = () => {
  console.error('Google Sign-In failed');
};