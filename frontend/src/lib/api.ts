interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber: string;
}

interface RegisterResponse {
  success: boolean;
  message?: string;
  user?: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
  };
}

const API_BASE_URL = '';

export const registerUser = async (userData: RegisterRequest): Promise<RegisterResponse> => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/Auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    return data;
  } catch (error) {
    if (error instanceof Error) {
      throw new Error(error.message);
    }
    throw new Error('Network error occurred');
  }
};

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
