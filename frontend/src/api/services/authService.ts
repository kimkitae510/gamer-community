//< 인증 관련 API 서비스
import api from '../axios';
import type { TokenResponse, TokenRequest, UserInfo } from '../types';

export const authService = {
  //< Access Token 재발급
  reissue: async (data: TokenRequest): Promise<TokenResponse> => {
    const response = await api.post<TokenResponse>('/auth/reissue', data);
    return response.data;
  },

  //< 로그아웃
  logout: async (): Promise<string> => {
    const response = await api.post<string>('/auth/logout');
    return response.data;
  },

  //< 현재 사용자 정보 조회
  getMe: async (): Promise<UserInfo> => {
    const response = await api.get<UserInfo>('/auth/me');
    return response.data;
  },
};