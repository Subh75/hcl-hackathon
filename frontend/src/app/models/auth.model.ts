export interface LoginRequest {
  customerId: number;
  password: string;
}

export interface LoginResponse {
  token: string;
  refreshToken: string;
  role: string;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface RefreshResponse {
  token: string;
  refreshToken: string;
}

export interface RegisterRequest {
  name: string;
  password: string;
}

export interface RegisterResponse {
  customerId: number;
  name: string;
  role: string;
}
