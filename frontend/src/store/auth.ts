import { create } from "zustand";
import { persist } from "zustand/middleware";

interface User {
  id: number;
  loginId: string;
  nickname: string;
  email: string;
  role: string;
}

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  setTokens: (accessToken: string, refreshToken: string) => void;
  setUser: (user: User) => void;
  clearTokens: () => void;
  clearUser: () => void;
  logout: () => void;
}

//< Zustand + persist로 인증 상태 관리 (localStorage에 자동 저장)
const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      setTokens: (accessToken, refreshToken) => set({ accessToken, refreshToken }),
      setUser: (user) => set({ user }),
      clearTokens: () => set({ accessToken: null, refreshToken: null }),
      clearUser: () => set({ user: null }),
      logout: () => {
        set({ accessToken: null, refreshToken: null, user: null });
        localStorage.removeItem("auth-storage"); //< persist 저장소도 명시적 삭제
      },
    }),
    {
      name: "auth-storage", //< localStorage 키 이름
    }
  )
);

export default useAuthStore;
