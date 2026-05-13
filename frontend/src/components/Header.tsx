import { Link, useNavigate, useLocation } from "react-router-dom";
import { useState } from "react";
import useAuthStore from "../store/auth";
import CategoryTabs from "../category/CategoryTabs";
import { authService } from "../api/services";

export default function Header() {
  const { accessToken, logout: storeLogout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const logout = async () => {
    try { await authService.logout(); } catch {}
    finally {
      localStorage.removeItem('accessToken');
      storeLogout();
      setMobileMenuOpen(false);
      navigate("/");
    }
  };

  const handleLoginClick = () => {
    setMobileMenuOpen(false);
    navigate("/login", { state: { from: location } });
  };

  return (
    <header className="bg-white border-b border-neutral-200">
      <div className="max-w-[1920px] mx-auto px-4 md:px-8">
        <div className="flex items-center justify-between h-14">
          <div className="flex items-center">
            <Link to="/" className="flex items-center gap-2 flex-shrink-0 group pr-4 md:pr-8">
              <svg width="22" height="22" viewBox="0 0 22 22" fill="none" xmlns="http://www.w3.org/2000/svg">
                <text x="1" y="18" fontFamily="system-ui, -apple-system, sans-serif" fontWeight="900" fontSize="20" fill="rgb(29,78,216)" letterSpacing="-1">G</text>
              </svg>
              <span className="text-neutral-800 font-semibold text-sm group-hover:text-primary-600 transition-colors hidden sm:inline" style={{ letterSpacing: '-0.01em' }}>GamerCommunity</span>
            </Link>

            <div className="hidden md:flex items-center">
              <div className="w-px h-5 bg-neutral-200 mr-8 flex-shrink-0" />
              <CategoryTabs />
            </div>
          </div>

          <nav className="hidden md:flex items-center gap-6 flex-shrink-0">
            {accessToken ? (
              <button onClick={logout} className="px-4 py-2 text-neutral-600 text-sm font-medium hover:text-neutral-900 transition-colors">로그아웃</button>
            ) : (
              <>
                <button onClick={handleLoginClick} className="px-4 py-2 text-neutral-600 text-sm font-medium hover:text-neutral-900 transition-colors">로그인</button>
                <Link to="/join" className="px-4 py-2 text-neutral-600 text-sm font-medium hover:text-neutral-900 transition-colors">회원가입</Link>
              </>
            )}
          </nav>

          <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="md:hidden p-2 text-neutral-600 hover:text-neutral-900">
            {mobileMenuOpen ? (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
            ) : (
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>
            )}
          </button>
        </div>
      </div>

      {mobileMenuOpen && (
        <div className="md:hidden border-t border-neutral-200 bg-white">
          <div className="px-4 py-3 overflow-x-auto">
            <CategoryTabs />
          </div>
          <div className="border-t border-neutral-100 px-4 py-3 flex gap-4">
            {accessToken ? (
              <button onClick={logout} className="w-full py-2.5 text-neutral-600 text-sm font-medium border border-neutral-200 rounded-lg">로그아웃</button>
            ) : (
              <>
                <button onClick={handleLoginClick} className="flex-1 py-2.5 text-sm font-medium text-neutral-600 border border-neutral-200 rounded-lg">로그인</button>
                <Link to="/join" onClick={() => setMobileMenuOpen(false)} className="flex-1 py-2.5 text-sm font-medium text-white bg-primary-600 rounded-lg text-center">회원가입</Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
