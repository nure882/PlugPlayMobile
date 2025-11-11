import {CartItem} from "../models/CartItem";
import {useState, useEffect} from "react";

class StorageService {
  private tokenKey: string;
  private refreshTokenKey: string;
  private guestCartKey: string;

  constructor() {
    this.tokenKey = 'auth_token';
    this.refreshTokenKey = 'refresh_token';
    this.guestCartKey = 'guest_cart';
  }

  setTokens(token: string, refreshToken: string) {
    try {
      localStorage.setItem(this.tokenKey, token);
      localStorage.setItem(this.refreshTokenKey, refreshToken);
    } catch (error) {
      console.error('Error saving tokens:', error);
    }
    window.dispatchEvent(new Event('tokenChanged'));
  }

  getAccessToken() {
    return localStorage.getItem(this.tokenKey);
  }

  useAccessToken() {
    const [token, setToken] = useState<string | null>(storage.getAccessToken());

    useEffect(() => {
      const handleChange = () => {
        setToken(storage.getAccessToken());
      };

      // react to storage changes (cross-tab) and same-tab custom events
      window.addEventListener('storage', handleChange);
      window.addEventListener('tokenChanged', handleChange);

      return () => {
        window.removeEventListener('storage', handleChange);
        window.removeEventListener('tokenChanged', handleChange);
      };
    }, []);

    return token;
  }

  getRefreshToken() {
    return localStorage.getItem(this.refreshTokenKey);
  }

  clearTokens() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.refreshTokenKey);
    window.dispatchEvent(new Event('tokenChanged'));
  }

  hasValidToken() {
    try {
      const token = this.getAccessToken();
      if (!token) {
        return false;
      }

      const parts = token.split('.');
      if (parts.length !== 3) {
        return false;
      }

      const payload = JSON.parse(atob(parts[1]));

      return payload.exp * 1000 > Date.now();
    } catch (error) {
      console.error('Error validating token:', error);

      return false;
    }
  }

  getGuestCart(): CartItem[] {
    try {
      const raw = localStorage.getItem(this.guestCartKey);
      return raw ? JSON.parse(raw) : [];
    } catch (error) {
      console.error('Error reading guest cart:', error);
      return [];
    }
  }

  setGuestCart(cart: CartItem[]): void {
    try {
      localStorage.setItem(this.guestCartKey, JSON.stringify(cart));
    } catch (error) {
      console.error('Error saving guest cart:', error);
    }
  }


  clearGuestCart(): void {
    try {
      localStorage.removeItem(this.guestCartKey);
    } catch (error) {
      console.error('Error clearing guest cart:', error);
    }
    window.dispatchEvent(new StorageEvent('storage', {key: this.guestCartKey, newValue: null}));
  }

}

export const storage = new StorageService();
