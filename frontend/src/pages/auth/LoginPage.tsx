import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import useAuthStore from "../../store/auth";
import { userService, authService } from "../../api/services";

export default function LoginPage() {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const setTokens = useAuthStore(state => state.setTokens);
  const setUser = useAuthStore(state => state.setUser);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogin = async () => {
    if (!loginId.trim() || !password.trim()) {
      alert("아이디와 비밀번호를 입력해주세요.");
      return;
    }

    setLoading(true);
    try {
      const tokenResponse = await userService.login({ loginId, password });
      setTokens(tokenResponse.accessToken, tokenResponse.refreshToken);

      const userInfo = await authService.getMe();
      setUser(userInfo);

      // 로그인 페이지로 올 때 저장된 이전 경로가 있으면 그곳으로, 없으면 홈으로
      const from = (location.state as any)?.from?.pathname || "/";
      navigate(from, { replace: true });
    } catch (err: any) {
      console.error("로그인 실패:", err);
      alert(err.response?.data?.message || "로그인에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !loading) {
      handleLogin();
    }
  };

  return (
    <div className="min-h-screen bg-neutral-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-primary-600 to-primary-700 rounded-2xl mb-4 shadow-lg">
            <span className="text-white font-bold text-3xl">G</span>
          </div>
          <h1 className="text-3xl font-bold text-neutral-900 mb-2">로그인</h1>
          <p className="text-neutral-600">Gamer Community에 오신 것을 환영합니다</p>
        </div>

        <div className="bg-white rounded-2xl border border-neutral-200 p-8">
          <div className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-2">
                아이디
              </label>
              <input
                className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all"
                placeholder="아이디를 입력하세요"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                onKeyPress={handleKeyPress}
                disabled={loading}
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-2">
                비밀번호
              </label>
              <input
                className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all"
                placeholder="비밀번호를 입력하세요"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyPress={handleKeyPress}
                disabled={loading}
              />
            </div>

            <button
              className={`w-full py-3 rounded-xl font-semibold shadow-md transition-all ${
                loading
                  ? "bg-neutral-300 text-neutral-500 cursor-not-allowed"
                  : "bg-gradient-to-r from-primary-600 to-primary-700 text-white hover:from-primary-700 hover:to-primary-800 hover:shadow-lg"
              }`}
              onClick={handleLogin}
              disabled={loading}
            >
              {loading ? "로그인 중..." : "로그인"}
            </button>
          </div>

          <div className="mt-6 pt-6 border-t border-neutral-200 text-center">
            <p className="text-sm text-neutral-600">
              아직 계정이 없으신가요?{" "}
              <button
                onClick={() => navigate("/join")}
                className="text-primary-600 hover:text-primary-700 font-semibold"
              >
                회원가입
              </button>
            </p>
          </div>
        </div>

        <div className="mt-6 text-center">
          <button
            onClick={() => navigate("/")}
            className="text-neutral-600 hover:text-neutral-900 text-sm font-medium"
          >
            ← 홈으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
}
