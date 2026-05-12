import React, { useEffect, useState } from "react";
import type { ChangeEvent } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import api from "../api/axios";

import GenreSidebar from "./GenreSidebar";
import CategoryHeader from "./CategoryHeader";
import GameCardGrid from "./GameCardGrid";
import TopCategoriesSidebar from "../components/sidebar/TopCategoriesSidebar";
import { useCategoryData } from "./useCategoryData";
import { useSidebarData } from "../hooks/useSidebarData";
import type { Category, Genre } from "./types";
import { categoryService, authService } from "../api/services";
import type { TopCategory } from "../api/types";

export default function CategoryBoardPage() {
  const { categoryName } = useParams<{ categoryName: string }>();
  const navigate = useNavigate();

  const {
    subCategories, setSubCategories,
    allGenres, setAllGenres,
    parentCategoryName, parentId,
    parentChildCount,
    loading,
    fetchSubCategories,
    fetchSubCategoriesByGenre,
    hasMore,
    loadingMore,
    loadMore,
  } = useCategoryData(categoryName);

  // 페이지 전용 상태
  const [uploadingId, setUploadingId] = useState<number | null>(null);
  const [editingNameId, setEditingNameId] = useState<number | null>(null);
  const [editingGenresId, setEditingGenresId] = useState<number | null>(null);
  const [editedName, setEditedName] = useState<string>("");
  const [editedGenres, setEditedGenres] = useState<Genre[]>([]);
  const [selectedGenreId, setSelectedGenreId] = useState<number | null>(null);
  const [sortOrder, setSortOrder] = useState<'latest' | 'oldest' | 'popular' | 'rating'>('latest');
  const [newGenreName, setNewGenreName] = useState<string>("");
  const [userGrade, setUserGrade] = useState<string | null>(null);

  // 사이드바 데이터 (공통 훅)
  const { topCategories, selectedPeriod, handlePeriodChange } = useSidebarData();
  const [newCategories, setNewCategories] = useState<Category[]>([]);

  // 신규 게시판 로드
  useEffect(() => {
    categoryService.getNewCategories().then(setNewCategories).catch(console.error);
  }, []);

  // 사용자 레벨 가져오기
  useEffect(() => {
    const fetchUserGrade = async () => {
      try {
        const user = await authService.getMe();
        setUserGrade(user.grade);
      } catch {
        setUserGrade(null);
      }
    };
    fetchUserGrade();
  }, []);

  // 레벨3 이상인지 체크
  const isLevel3OrAbove = userGrade === 'LEVEL3';

  // 정렬 변경 시 새로 로드
  useEffect(() => {
    if (!parentId) return;
    
    if (selectedGenreId) {
      fetchSubCategoriesByGenre(parentId, selectedGenreId, sortOrder, true);
    } else {
      fetchSubCategories(parentId, sortOrder, true);
    }
  }, [sortOrder]);

  // ===== 핸들러 =====
  const handleGenreChange = async (option: any) => {
    const genreId = option ? option.value : null;
    setSelectedGenreId(genreId);

    if (!parentId) return;

    if (!genreId) {
      await fetchSubCategories(parentId, sortOrder, true);
    } else {
      await fetchSubCategoriesByGenre(parentId, genreId, sortOrder, true);
    }
  };

  const handleLoadMore = () => {
    if (!parentId) return;
    loadMore(parentId, selectedGenreId, sortOrder);
  };

  const handleFileChange = async (categoryId: number, e: ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files?.length) return;
    const file = e.target.files[0];
    setUploadingId(categoryId);

    try {
      const imageUrl = await categoryService.uploadChildImage(categoryId, file);
      setSubCategories(prev => prev.map(c => c.id === categoryId ? { ...c, imageUrl } : c));
    } catch {
      alert("이미지 업로드 실패");
    } finally {
      setUploadingId(null);
      e.target.value = "";
    }
  };

  const handleDelete = async (categoryId: number) => {
    if (!window.confirm("정말 삭제하시겠습니까?")) return;
    try {
      await categoryService.deleteChild(categoryId);
      setSubCategories(prev => prev.filter(c => c.id !== categoryId));
      alert("삭제 완료");
    } catch {
      alert("삭제 실패");
    }
  };

  const startEditName = (cat: Category) => { setEditingNameId(cat.id); setEditedName(cat.name); };
  const saveNameEdit = async (categoryId: number) => {
    try {
      await categoryService.updateName(categoryId, { name: editedName } as any);
      setSubCategories(prev => prev.map(c => c.id === categoryId ? { ...c, name: editedName } : c));
      setEditingNameId(null);
      alert("이름 수정 완료");
    } catch {
      alert("이름 수정 실패");
    }
  };
  const cancelEditName = () => setEditingNameId(null);

  const startEditGenres = (cat: Category) => { setEditingGenresId(cat.id); setEditedGenres(cat.genres ?? []); };
  const saveGenresEdit = async (categoryId: number) => {
    try {
      const genreId = editedGenres.map(g => g.id);
      await categoryService.updateGenres(categoryId, { genreId } as any);
      setSubCategories(prev => prev.map(c => c.id === categoryId ? { ...c, genres: editedGenres } : c));
      setEditingGenresId(null);
      alert("장르 수정 완료");
    } catch {
      alert("장르 수정 실패");
    }
  };
  const cancelEditGenres = () => setEditingGenresId(null);

  const addGenre = async () => {
    if (!newGenreName.trim()) return;
    try {
      const res = await api.post<Genre>("/genres", null, { params: { name: newGenreName.trim() } });
      setAllGenres(prev => [...prev, res.data]);
      setNewGenreName("");
    } catch {
      alert("장르 추가 실패");
    }
  };

  const deleteSelectedGenre = async () => {
    if (!selectedGenreId) return;
    if (!window.confirm("선택한 장르를 삭제하시겠습니까?")) return;
    try {
      await api.delete(`/genres/${selectedGenreId}`);
      setAllGenres(prev => prev.filter(g => g.id !== selectedGenreId));
      setSelectedGenreId(null);
    } catch {
      alert("장르 삭제 실패");
    }
  };

  const getStarRating = (rating: number) => {
    const stars: string[] = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;
    for (let i = 0; i < fullStars; i++) stars.push('⭐');
    if (hasHalfStar) stars.push('🌟');
    while (stars.length < 5) stars.push('☆');
    return stars.join('');
  };

  const clearFilter = async () => {
    setSelectedGenreId(null);
    setSortOrder('latest');
    if (parentId) {
      await fetchSubCategories(parentId, 'latest', true);
    }
  };

  useEffect(() => {
    setEditingNameId(null);
    setEditingGenresId(null);
    setEditedName("");
    setEditedGenres([]);
    setSelectedGenreId(null);
    setSortOrder('latest');
  }, [categoryName]);

  // ================= Xbox 스타일 깔끔한 레이아웃 =================
  return (
    <div className="min-h-screen bg-neutral-50">
      <div className="max-w-[1800px] mx-auto px-8">
        <div className="flex gap-8">
          {/* 왼쪽: 필터 사이드바 */}
          <aside className="w-[280px] flex-shrink-0 hidden lg:block py-8">
            <div className="sticky top-28 bg-white rounded-xl p-6 shadow-sm border border-neutral-100">
            <GenreSidebar
              allGenres={allGenres}
              selectedGenreId={selectedGenreId}
              handleGenreChange={handleGenreChange}
              sortOrder={sortOrder}
              setSortOrder={setSortOrder}
              newGenreName={newGenreName}
              setNewGenreName={setNewGenreName}
              addGenre={addGenre}
              deleteSelectedGenre={deleteSelectedGenre}
              subCategoriesCount={parentChildCount}
            />
            </div>
          </aside>

          {/* 중앙: 게임 카드 영역 */}
          <main className="flex-1 min-w-0">
            <div className="py-8 min-h-[800px]">
          <div className="mb-6">
            <CategoryHeader
            parentCategoryName={parentCategoryName}
            selectedGenreId={selectedGenreId}
            allGenres={allGenres}
            clearFilter={clearFilter}
            isLevel3OrAbove={isLevel3OrAbove}
          />
          </div>

          <GameCardGrid
            subCategories={subCategories}
            categoryName={categoryName}
            loading={loading}
            uploadingId={uploadingId}
            editingNameId={editingNameId}
            editingGenresId={editingGenresId}
            editedName={editedName}
            setEditedName={setEditedName}
            editedGenres={editedGenres}
            setEditedGenres={setEditedGenres}
            allGenres={allGenres}
            startEditName={startEditName}
            saveNameEdit={saveNameEdit}
            cancelEditName={cancelEditName}
            startEditGenres={startEditGenres}
            saveGenresEdit={saveGenresEdit}
            cancelEditGenres={cancelEditGenres}
            handleFileChange={handleFileChange}
            handleDelete={handleDelete}
            getStarRating={getStarRating}
            isLevel3OrAbove={isLevel3OrAbove}
          />

          {/* 더보기 버튼 - 차분한 느낌 */}
          {hasMore && (
            <div className="mt-8 text-center">
              <button
                onClick={handleLoadMore}
                disabled={loadingMore}
                className="px-6 py-2.5 text-sm text-neutral-600 font-medium bg-white border border-neutral-200 rounded-lg hover:border-neutral-300 hover:bg-neutral-50 hover:text-neutral-900 transition-all shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loadingMore ? (
                  <span className="flex items-center gap-2">
                    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    로딩 중
                  </span>
                ) : (
                  "더보기"
                )}
              </button>
            </div>
          )}
            </div>
          </main>

          {/* 오른쪽: 인기 게시판 & 신규 게시판 사이드바 */}
          <aside className="w-[320px] flex-shrink-0 hidden xl:block py-8">
            <div className="sticky top-28 space-y-6">
              {/* 인기 게시판 */}
              <TopCategoriesSidebar
                topCategories={topCategories}
                selectedPeriod={selectedPeriod}
                onPeriodChange={handlePeriodChange}
              />

              {/* 신규 게시판 */}
              <div className="bg-white rounded-lg border border-neutral-200 p-5">
                <div className="mb-4">
                  <h3 className="text-base font-bold text-neutral-900">신규 게시판</h3>
                </div>
                {newCategories.length === 0 ? (
                  <p className="text-sm text-neutral-500 text-center py-4">신규 게시판이 없습니다</p>
                ) : (
                  <div className="space-y-3">
                    {newCategories.slice(0, 5).map((category) => (
                      <Link
                        key={category.id}
                        to={`/${category.name}/game/${category.id}`}
                        className="block hover:bg-neutral-50 p-2.5 rounded transition-colors"
                      >
                        <div className="flex gap-3 items-center">
                          {/* 게임 이미지 */}
                          <div className="flex-shrink-0">
                            <img
                              src={category.imageUrl || ''}
                              alt={category.name}
                              className="w-12 h-12 rounded object-cover"
                              onError={(e) => {
                                e.currentTarget.onerror = null;
                                e.currentTarget.style.display = 'none';
                              }}
                            />
                          </div>
                          
                          {/* 정보 */}
                          <div className="flex-1 min-w-0">
                            <h4 className="text-sm font-bold text-neutral-900 truncate mb-1 hover:text-primary-600">
                              {category.name}
                            </h4>
                            <div className="flex items-center gap-2 text-xs text-neutral-600">
                              <div className="flex items-center gap-1">
                                <span>평점</span>
                                <span className="font-bold text-red-600">{category.rating?.toFixed(1) || '0.0'}</span>
                              </div>
                              <span className="text-neutral-400">·</span>
                              <div className="flex items-center gap-1">
                                <span>글</span>
                                <span className="font-bold text-neutral-900">{category.postCount || 0}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  );
}
