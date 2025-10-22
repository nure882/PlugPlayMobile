import React, { createContext, useContext, useEffect, useState } from 'react';
import { User } from '../models/User.ts';
import { storage } from '../utils/StorageService.ts';
import { useVerifyQuery, useLogoutMutation } from '../redux/authApi.ts';

interface AuthContextType {
  user: User | null;
  setUser: (u: User | null) => void;
  logout: () => Promise<void>;
  isVerifying: boolean;
  isLoggingOut: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);

  const hasToken = !!storage.getAccessToken() && storage.hasValidToken();

  const { data, isFetching, isError } = useVerifyQuery(undefined, { skip: !hasToken });
  const [logoutApi, { isLoading: isLoggingOut }] = useLogoutMutation();

  useEffect(() => {
    if (data) {
      setUser(data);
    } else if (!isFetching && !data) {
      // No verified user
      if (!hasToken) storage.clearTokens();
      setUser(null);
    }

    if (isError) {
      storage.clearTokens();
      setUser(null);
    }
  }, [data, isFetching, isError, hasToken]);

  const logout = async () => {
    try {
      const refreshToken = storage.getRefreshToken();
      if (refreshToken) {
        await logoutApi({ refreshToken }).unwrap();
      }
    } catch (e) {
      // ignore server errors on logout
    } finally {
      storage.clearTokens();
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, setUser, logout, isVerifying: isFetching, isLoggingOut }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
