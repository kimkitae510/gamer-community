// src/pages/categories/CategoryCreatePage.tsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";
import { categoryService } from "../api/services";
import type { Category } from "../api/types";
import type { Genre } from "./types";

export default function CategoryCreatePage() {
  const navigate = useNavigate();

  const [parents, setParents] = useState<Category[]>([]);
  const [selectedParent, setSelectedParent] = useState<number | "">("");
  const [categoryName, setCategoryName] = useState<string>("");
  const [genres, setGenres] = useState<Genre[]>([]);
  const [selectedGenres, setSelectedGenres] = useState<number[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  // 상위 카테고리 불러오기
  useEffect(() => {
    categoryService.getParents()
      .then(setParents)
      .catch(err => {
        console.error(err);
        alert("상위 카테고리를 불러오는 데 실패했습니다.");
      });

    // 장르 목록 불러오기
    api.get<Genre[]>("/genres")
      .then(res => setGenres(res.data))
      .catch(err => {
        console.error(err);
        alert("장르 목록을 불러오는 데 실패했습니다.");
      });
  }, []);

  const toggleGenre = (genreId: number) => {
    setSelectedGenres(prev =>
      prev.includes(genreId) ? prev.filter(id => id !== genreId) : [...prev, genreId]
    );
  };

  const handleSubmit = async () => {
    if (!selectedParent) {
      alert("상위 카테고리를 선택해주세요.");
      return;
    }
    if (!categoryName.trim()) {
      alert("카테고리 이름을 입력해주세요.");
      return;
    }
    if (selectedGenres.length === 0) {
      alert("최소 하나의 장르를 선택해주세요.");
      return;
    }

    setLoading(true);
    try {
      await api.post(`/categories/parents/${selectedParent}/children`, {
        name: categoryName,
        genreId: selectedGenres,
        writable: true,
        parentId: selectedParent
      });

      alert("카테고리가 생성되었습니다.");
      navigate(-1); // 이전 페이지로 이동
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || err.message || "카테고리 생성에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white py-8">
      <div className="container-custom max-w-3xl">
        {/* 뒤로가기 버튼 */}
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 mb-6 text-neutral-700 hover:text-primary-600 transition-colors"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          <span className="font-medium">뒤로 가기</span>
        </button>

        {/* 메인 카드 */}
        <div className="bg-white rounded-lg border border-neutral-200 shadow-sm overflow-hidden">
          {/* 헤더 */}
          <div className="px-8 py-6 border-b border-neutral-200">
            <h1 className="text-2xl font-bold text-neutral-900 mb-2">게시판 생성</h1>
            <p className="text-neutral-600">새로운 게임 카테고리를 만들어보세요</p>
          </div>

          {/* 폼 영역 */}
          <div className="p-8 space-y-6">
            {/* 상위 카테고리 선택 */}
            <div>
              <label className="block text-sm font-semibold text-neutral-900 mb-2">
                상위 카테고리
              </label>
              <select
                className="w-full px-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent bg-white text-neutral-900"
                value={selectedParent}
                onChange={(e) => setSelectedParent(Number(e.target.value))}
              >
                <option value="">선택해주세요</option>
                {parents.map(p => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
            </div>

            {/* 카테고리 이름 입력 */}
            <div>
              <label className="block text-sm font-semibold text-neutral-900 mb-2">
                게시판 이름
              </label>
              <input
                type="text"
                value={categoryName}
                onChange={(e) => setCategoryName(e.target.value)}
                className="w-full px-4 py-2.5 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent placeholder:text-neutral-400"
                placeholder="예: 젤다의 전설, 몬스터 헌터 등"
              />
            </div>

            {/* 장르 선택 */}
            <div>
              <label className="block text-sm font-semibold text-neutral-900 mb-2">
                장르 선택 <span className="text-sm font-normal text-neutral-500">(최소 1개 이상)</span>
              </label>
              <div className="flex flex-wrap gap-2">
                {genres.map((genre) => (
                  <button
                    key={genre.id}
                    type="button"
                    onClick={() => toggleGenre(genre.id)}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${
                      selectedGenres.includes(genre.id)
                        ? "bg-primary-600 text-white shadow-md"
                        : "bg-white text-neutral-700 border border-neutral-300 hover:border-primary-600 hover:bg-primary-50"
                    }`}
                  >
                    {selectedGenres.includes(genre.id) && (
                      <span className="mr-1">✓</span>
                    )}
                    {genre.name}
                  </button>
                ))}
              </div>
              {selectedGenres.length > 0 && (
                <div className="mt-3 text-sm text-neutral-600">
                  선택된 장르: <span className="font-semibold text-primary-600">{selectedGenres.length}개</span>
                </div>
              )}
            </div>

            {/* 제출 버튼 */}
            <div className="pt-4">
              <button
                onClick={handleSubmit}
                disabled={loading}
                className={`w-full py-3 rounded-lg font-semibold text-base transition-all shadow-sm ${
                  loading
                    ? "bg-neutral-300 text-neutral-500 cursor-not-allowed"
                    : "bg-primary-600 text-white hover:bg-primary-700 hover:shadow-md"
                }`}
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <div className="animate-spin w-5 h-5 border-2 border-white border-t-transparent rounded-full"></div>
                    생성 중...
                  </span>
                ) : (
                  "게시판 생성하기"
                )}
              </button>
            </div>
          </div>

          {/* 안내 메시지 */}
          <div className="bg-neutral-50 px-8 py-6 border-t border-neutral-200">
            <h3 className="text-sm font-semibold text-neutral-900 mb-3">
              안내사항
            </h3>
            <ul className="space-y-2 text-sm text-neutral-600">
              <li className="flex items-start gap-2">
                <span className="text-primary-600 mt-0.5">•</span>
                <span>게시판은 상위 카테고리 아래에 생성됩니다</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-primary-600 mt-0.5">•</span>
                <span>장르는 나중에도 수정할 수 있습니다</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-primary-600 mt-0.5">•</span>
                <span>생성된 게시판은 모든 사용자가 볼 수 있습니다</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
}
