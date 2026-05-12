//< 유저 관련 API 서비스
import api from '../axios';
import type { LoginRequest, JoinRequest, TokenResponse } from '../types';

export const userService = {
  //< 로그인 ID 중복 체크
  checkLoginId: async (loginId: string): Promise<boolean> => {
    const response = await api.get<boolean>('/users/check-id', {
      params: { loginId },
    });
    return response.data;
  },

  //< 닉네임 중복 체크
  checkNickname: async (username: string): Promise<boolean> => {
    const response = await api.get<boolean>('/users/check-nickname', {
      params: { username },
    });
    return response.data;
  },

  //< 로그인
  login: async (data: LoginRequest): Promise<TokenResponse> => {
    const response = await api.post<TokenResponse>('/users/login', data);
    return response.data;
  },

  //< 회원가입
  join: async (data: JoinRequest): Promise<string> => {
    const response = await api.post<string>('/users/join', data);
    return response.data;
  },
};