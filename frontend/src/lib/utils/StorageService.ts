class StorageService {
    private tokenKey: string;
    private refreshTokenKey: string;

    constructor() {
        this.tokenKey = 'auth_token';
        this.refreshTokenKey = 'refresh_token';
    }
   
    setTokens(token: string, refreshToken: string) {
        try {
            localStorage.setItem(this.tokenKey, token);
            localStorage.setItem(this.refreshTokenKey, refreshToken);
        } catch (error) {
            console.error('Error saving tokens:', error);
        }
    }

    getAccessToken() {
        return localStorage.getItem(this.tokenKey);
    }

    getRefreshToken() {
        return localStorage.getItem(this.refreshTokenKey);
    }

    clearTokens() {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.refreshTokenKey);
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
}

export const storage = new StorageService();
