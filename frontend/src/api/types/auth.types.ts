// 인증 관련 타입 정의
export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface JoinRequest {
  loginId: string;
  password: string;
  passwordCheck: string;
  nickname: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface TokenRequest {
  accessToken: string;
  refreshToken: string;
}

export interface UserInfo {
  loginId: string;
  username: string;
  role: string;
  grade: 'LEVEL1' | 'LEVEL2' | 'LEVEL3';
}
