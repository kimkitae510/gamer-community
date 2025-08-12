import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { userService } from "../../api/services";

export default function JoinPage() {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [passwordCheck, setPasswordCheck] = useState("");
  const [nickname, setNickname] = useState("");
  const [loading, setLoading] = useState(false);
  const [loginIdChecked, setLoginIdChecked] = useState(false);
  const [nicknameChecked, setNicknameChecked] = useState(false);
  const navigate = useNavigate();

  const checkLoginIdDuplicate = async () => {
    if (!loginId.trim()) {
      alert("아이디를 입력해주세요.");
      return;
    }

    try {
      const isDuplicate = await userService.checkLoginId(loginId);
      if (isDuplicate) {
        alert("이미 사용 중인 아이디입니다.");
        setLoginIdChecked(false);
      } else {
        alert("사용 가능한 아이디입니다.");
        setLoginIdChecked(true);
      }
    } catch (err) {
      alert("중복 확인에 실패했습니다.");
      setLoginIdChecked(false);
    }
  };

  const checkNicknameDuplicate = async () => {
    if (!nickname.trim()) {
      alert("닉네임을 입력해주세요.");
      return;
    }

    try {
      const isDuplicate = await userService.checkNickname(nickname);
      if (isDuplicate) {
        alert("이미 사용 중인 닉네임입니다.");
        setNicknameChecked(false);
      } else {
        alert("사용 가능한 닉네임입니다.");
        setNicknameChecked(true);
      }
    } catch (err) {
      alert("중복 확인에 실패했습니다.");
      setNicknameChecked(false);
    }
  };

  const handleJoin = async () => {
    if (!loginId.trim() || !password.trim() || !passwordCheck.trim() || !nickname.trim()) {
      alert("모든 항목을 입력해주세요.");
      return;
    }

    const loginIdRegex = /^[a-zA-Z0-9]{8,16}$/;
    if (!loginIdRegex.test(loginId)) {
      alert("아이디는 8자 이상 16자 이하, 영어와 숫자만 가능합니다.");
      return;
    }

    if (!loginIdChecked) {
      alert("아이디 중복 확인을 해주세요.");
      return;
    }

    if (!nicknameChecked) {
      alert("닉네임 중복 확인을 해주세요.");
      return;
    }

    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,16}$/;
    if (!passwordRegex.test(password)) {
      alert("비밀번호는 8자 이상 16자 이하, 영어, 숫자, 특수문자를 모두 포함해야 합니다.");
      return;
    }

    if (password !== passwordCheck) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    setLoading(true);
    try {
      await userService.join({ loginId, password, passwordCheck, nickname });
      alert("회원가입이 완료되었습니다!");
      navigate("/login");
    } catch (err: any) {
      console.error("회원가입 실패:", err);
      const errorMessage = err.response?.data?.message || "회원가입에 실패했습니다.";
      alert(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-neutral-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-primary-600 to-primary-700 rounded-2xl mb-4 shadow-lg">
            <span className="text-white font-bold text-3xl">G</span>
          </div>
          <h1 className="text-3xl font-bold text-neutral-900 mb-2">회원가입</h1>
          <p className="text-neutral-600">Gamer Community에 가입하세요</p>
        </div>

        <div className="bg-white rounded-2xl border border-neutral-200 p-8">
          <div className="space-y-5">
            {/* 아이디 */}
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-2">
                아이디 {loginIdChecked && <span className="text-green-600">✓</span>}
              </label>
              <div className="flex gap-2">
                <input
                  className="flex-1 px-4 py-3 border border-neutral-300 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all"
                  placeholder="8-16자, 영어+숫자"
                  value={loginId}
                  onChange={(e) => {
                    setLoginId(e.target.value);
                    setLoginIdChecked(false);
                  }}
                  disabled={loading}
                />
                <button
                  className="px-4 py-3 bg-neutral-100 text-neutral-700 rounded-xl hover:bg-neutral-200 transition-colors font-medium whitespace-nowrap"
                  onClick={checkLoginIdDuplicate}
                  disabled={loading}
                >
                  중복확인
                </button>
              </div>
              <p className="mt-1.5 text-xs text-neutral-500">영문자와 숫자를 조합하여 8-16자</p>
            </div>

            {/* 비밀번호 */}
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-2">
                비밀번호
              </label>
              <input
                type="password"
                className="w-full px-4 py-3 border border-neutral-300 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all"
                placeholder="8-16자, 영어+숫자+특수문자"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
              />
              <p className="mt-1.5 text-xs text-neutral-500">영문, 숫자, 특수문자 포함 8-16자</p>
            </div>

            {/* 비밀번호 확인 */}
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-2">
                비밀번호 확인 
                {password && passwordCheck && password === passwordCheck && (
                  <span className="text-green-600 ml-1">✓</span>
                )}
              </label>
              <input
                type="password"
                className={`w-full px-4 py-3 border rounded-xl focus:ring-2 focus:outline-none transition-all ${
                  password && passwordCheck && password !== passwordCheck
                    ? 'border-red-300 focus:border-red-500 focus:ring-red-100'
                    : 'border-neutral-300 focus:border-primary-500 focus:ring-primary-100'
                }`}
                placeholder="비밀번호 재입력"
                value={passwordCheck}
                onChange={(e) => setPasswordCheck(e.target.value)}
                disabled={loading}
              />
              {password && passwordCheck && password !== passwordCheck && (
                <p className="mt-1.5 text-xs text-red-600">비밀번호가 일치하지 않습니다</p>
              )}
            </div>

            {/* 닉네임 */}
            <div>
              <label className="block text-sm font-medium text-neutral-700 mb-2">
                닉네임 {nicknameChecked && <span className="text-green-600">✓</span>}
              </label>
              <div className="flex gap-2">
                <input
                  className="flex-1 px-4 py-3 border border-neutral-300 rounded-xl focus:border-primary-500 focus:ring-2 focus:ring-primary-100 focus:outline-none transition-all"
                  placeholder="닉네임 입력"
                  value={nickname}
                  onChange={(e) => {
                    setNickname(e.target.value);
                    setNicknameChecked(false);
                  }}
                  disabled={loading}
                />
                <button
                  className="px-4 py-3 bg-neutral-100 text-neutral-700 rounded-xl hover:bg-neutral-200 transition-colors font-medium whitespace-nowrap"
                  onClick={checkNicknameDuplicate}
                  disabled={loading}
                >
                  중복확인
                </button>
              </div>
            </div>

            {/* 회원가입 버튼 */}
            <button
              className={`w-full py-3 rounded-xl font-semibold shadow-md transition-all mt-6 ${
                loading
                  ? "bg-neutral-300 text-neutral-500 cursor-not-allowed"
                  : "bg-gradient-to-r from-primary-600 to-primary-700 text-white hover:from-primary-700 hover:to-primary-800 hover:shadow-lg"
              }`}
              onClick={handleJoin}
              disabled={loading}
            >
              {loading ? "가입 처리 중..." : "회원가입"}
            </button>
          </div>

          <div className="mt-6 pt-6 border-t border-neutral-200 text-center">
            <p className="text-sm text-neutral-600">
              이미 계정이 있으신가요?{" "}
              <button
                onClick={() => navigate("/login")}
                className="text-primary-600 hover:text-primary-700 font-semibold"
              >
                로그인
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
