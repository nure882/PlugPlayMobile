import React, {createContext, useContext, useEffect, useState} from 'react';
import {User} from '../models/User.ts';
import {storage} from '../utils/StorageService.ts';
import {useVerifyQuery, useLogoutMutation} from '../api/authApi.ts';
// import {s} from "../utils/useful.ts";

interface AuthContextType {
  user: User | null;
  setUser: (u: User | null) => void;
  logout: () => Promise<void>;
  isVerifying: boolean;
  isLoggingOut: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
  const [user, setUser] = useState<User | null>(null);

  const hasToken = !!storage.getAccessToken() // && storage.hasValidToken();
  // s(`access token: ${storage.getAccessToken()}`);
  // s(`refresh token: ${storage.getRefreshToken()}`);
  // s(`hasToken: ${hasToken}`);
  // s(`user: ${user}`);

  const {data, isFetching, isError} = useVerifyQuery(undefined, {skip: !hasToken});
  const [logoutApi, {isLoading: isLoggingOut}] = useLogoutMutation();

  useEffect(() => {
    if (data) {
      setUser(data);
    } else if (!isFetching && !data) {
      // No verified user
      if (!hasToken) {
        // storage.clearTokens();
        // s("hui")
      }
      // setUser(null);
    }

    if (isError) {
      storage.clearTokens();
      // s("nia");
      setUser(null);
    }
  }, [data, isFetching, isError, hasToken]);

  const logout = async () => {
    try {
      const refreshToken = storage.getRefreshToken();
      if (refreshToken) {
        await logoutApi({refreshToken}).unwrap();
      }
    } catch (e) {
      // ignore server errors on logout
    } finally {
      storage.clearTokens();
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{user, setUser, logout, isVerifying: isFetching, isLoggingOut}}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
};
