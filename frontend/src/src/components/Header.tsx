import { Link, useNavigate, useLocation } from "react-router-dom";
import useAuthStore from "../store/auth";
import CategoryTabs from "../category/CategoryTabs";
import { authService } from "../api/services";

//< 상단 헤더 (로고 + 카테고리 탭 + 로그인/로그아웃)
export default function Header() {
  const { accessToken, logout: storeLogout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();

  //< 로그아웃 처리 (백엔드 요청 + 프론트 상태 초기화)
  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error("로그아웃 실패:", error);
    } finally {
      localStorage.removeItem('accessToken');
      storeLogout();
      navigate("/");
    }
  };

  //< 로그인 페이지 이동 (현재 위치 기억)
  const handleLoginClick = () => {
    navigate("/login", { state: { from: location } });
  };

  return (
    <header className="bg-white border-b border-neutral-200">
      <div className="max-w-[1920px] mx-auto px-8">
        <div className="flex items-center justify-between h-14">
          {/*< 왼쪽: 로고 + 카테고리 탭 */}
          <div className="flex items-center">
            <Link to="/" className="flex items-center gap-2 flex-shrink-0 group pr-8">
              <svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg">
                <text x="1" y="18" fontFamily="system-ui, -apple-system, sans-serif" fontWeight="900" fontSize="20" fill="rgb(29,78,216)" letterSpacing="-1">G</text>
              </svg>
              <span className="text-neutral-800 font-semibold text-sm group-hover:text-primary-600 transition-colors" style={{ letterSpacing: '-0.01em' }}>GamerCommunity</span>
            </Link>

            <div className="w-px h-5 bg-neutral-200 mr-8 flex-shrink-0" />

            <CategoryTabs />
          </div>

          {/*< 오른쪽: 로그인/회원가입 */}
          <nav className="flex items-center gap-6 flex-shrink-0">
            {accessToken ? (
              <button
                onClick={logout}
                className="px-4 py-2 text-neutral-600 text-sm font-medium hover:text-neutral-900 transition-colors"
              >
                로그아웃
              </button>
            ) : (
              <>
                <button
                  onClick={handleLoginClick}
                  className="px-4 py-2 text-neutral-600 text-sm font-medium hover:text-neutral-900 transition-colors"
                >
                  로그인
                </button>
                <Link
                  to="/join"
                  className="px-4 py-2 text-neutral-600 text-sm font-medium hover:text-neutral-900 transition-colors"
                >
                  회원가입
                </Link>
              </>
            )}
          </nav>
        </div>
      </div>
    </header>
  );
}