export interface User {
  id: string;
  email: string;
  full_name: string;
  role: 'USER' | 'ADMIN' | 'SUPERADMIN';
  is_active: boolean;
  is_verified: boolean;
  avatar_url?: string;
}

export interface TokenPair {
  access_token: string;
  refresh_token: string;
  token_type: string;
  expires_in: number;
}

export interface AuthResponse {
  success: boolean;
  message?: string;
  data: {
    user: User;
    tokens: TokenPair;
  };
}
