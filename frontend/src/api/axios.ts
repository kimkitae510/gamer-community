import axios, { AxiosHeaders } from "axios";

import useAuthStore from "../store/auth";

const api = axios.create({
  baseURL: "http://localhost:8081/api",  // ✅ 수정
  withCredentials: true,  // ✅ 추가 (쿠키 전송)
  headers: { "Content-Type": "application/json" },
});

// 요청 인터셉터: Access Token 자동 추가
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;

  if (!config.headers) {
    config.headers = new AxiosHeaders();
  }

  if (token) {
    (config.headers as AxiosHeaders).set("Authorization", `Bearer ${token}`);
  }

  return config;
});

// 응답 인터셉터: 401 → 토큰 재발급 후 요청 재시도
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const { refreshToken, accessToken } = useAuthStore.getState();
        
        // refreshToken이 없으면 로그인 페이지로 리다이렉트하지 않고 에러 반환
        if (!refreshToken) {
          return Promise.reject(error);
        }

        const res = await api.post("/auth/reissue", {
          accessToken,
          refreshToken,
        });

        // 토큰 저장
        useAuthStore.getState().setTokens(
          res.data.accessToken,
          res.data.refreshToken
        );

        // 기존 요청 헤더에 새 Access Token 적용
        originalRequest.headers["Authorization"] = `Bearer ${res.data.accessToken}`;

        // 요청 재시도
        return api(originalRequest);
      } catch (e) {
        useAuthStore.getState().logout();
        // 일부 API는 로그인 없이도 접근 가능하므로 리다이렉트 제거
        // window.location.href = "/login";
        return Promise.reject(e);
      }
    }

    return Promise.reject(error);
  }
);

export default api;