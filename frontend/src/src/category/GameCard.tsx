// src/category/Cards/GameCard.tsx
import React from "react";
import Select from "react-select";
import customSelectStyles from "./selectStyles";
import type { Category, Genre } from "./types";
import type { ChangeEvent } from "react";
import { useNavigate } from "react-router-dom";

interface Props {
  cat: Category;
  categoryName?: string;
  uploadingId: number | null;
  editingNameId: number | null;
  editingGenresId: number | null;
  editedName: string;
  setEditedName: (s: string) => void;
  editedGenres: Genre[];
  setEditedGenres: (g: Genre[]) => void;
  allGenres: Genre[];
  startEditName: (cat: Category) => void;
  saveNameEdit: (categoryId: number) => Promise<void>;
  cancelEditName: () => void;
  startEditGenres: (cat: Category) => void;
  saveGenresEdit: (categoryId: number) => Promise<void>;
  cancelEditGenres: () => void;
  handleFileChange: (categoryId: number, e: ChangeEvent<HTMLInputElement>) => Promise<void>;
  handleDelete: (categoryId: number) => Promise<void>;
  getStarRating: (rating: number) => string;
  isLevel3OrAbove: boolean;
}

export default function GameCard({
  cat,
  categoryName,
  uploadingId,
  editingNameId,
  editingGenresId,
  editedName,
  setEditedName,
  editedGenres,
  setEditedGenres,
  allGenres,
  startEditName,
  saveNameEdit,
  cancelEditName,
  startEditGenres,
  saveGenresEdit,
  cancelEditGenres,
  handleFileChange,
  handleDelete,
  getStarRating,
  isLevel3OrAbove
}: Props) {
  const navigate = useNavigate();

  const onCardClick = () => {
    if (!editingNameId && !editingGenresId) {
      navigate(`/${categoryName}/game/${cat.id}`);
    }
  };

  return (
    <div
      className="group bg-white rounded-lg border border-neutral-200 hover:shadow-lg cursor-pointer transition-all duration-200 overflow-hidden"
      onClick={onCardClick}
    >
      {/* 이미지 영역 */}
      <div className="relative aspect-[3/2] overflow-hidden bg-neutral-100">
        {uploadingId === cat.id && (
          <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-10">
            <div className="text-white text-center">
              <div className="animate-spin w-8 h-8 border-2 border-white border-t-transparent rounded-full mx-auto mb-2"></div>
              <span className="text-sm">업로드 중...</span>
            </div>
          </div>
        )}

        {cat.imageUrl ? (
          <img src={cat.imageUrl} alt={cat.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" />
        ) : (
          <div className="w-full h-full flex items-center justify-center">
            <div className="text-center text-neutral-400">
              <div className="text-4xl mb-2">🎮</div>
              <div className="text-xs">이미지 없음</div>
            </div>
          </div>
        )}

        {/* 편집 버튼 */}
        <div className="absolute top-2 left-2 right-2 flex justify-between opacity-0 group-hover:opacity-100 transition-opacity duration-200">
          {isLevel3OrAbove && (
            <>
              <label
                htmlFor={`file-input-${cat.id}`}
                className="bg-white text-neutral-700 w-8 h-8 rounded-md flex items-center justify-center hover:bg-neutral-50 cursor-pointer shadow-sm"
                title="이미지 변경"
                onClick={e => e.stopPropagation()}
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </label>
              <input
                onClick={e => e.stopPropagation()}
                id={`file-input-${cat.id}`}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={e => handleFileChange(cat.id, e)}
              />

              <button
                onClick={e => { e.stopPropagation(); handleDelete(cat.id); }}
                className="bg-red-500 text-white w-8 h-8 rounded-md flex items-center justify-center hover:bg-red-600 shadow-sm"
                title="삭제"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </>
          )}
        </div>
      </div>

      {/* 내용 영역 */}
      <div className="p-4 space-y-3">
        {/* 제목 편집 */}
        {editingNameId === cat.id ? (
          <div className="space-y-2">
            <input
              value={editedName}
              onChange={e => setEditedName(e.target.value)}
              className="w-full px-3 py-2 border border-neutral-300 rounded-md focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500 font-semibold text-sm"
              onClick={e => e.stopPropagation()}
            />
            <div className="flex gap-2">
              <button
                onClick={e => { e.stopPropagation(); saveNameEdit(cat.id); }}
                className="px-3 py-1.5 bg-primary-600 text-white rounded-md text-xs font-medium hover:bg-primary-700"
              >
                저장
              </button>
              <button
                onClick={e => { e.stopPropagation(); cancelEditName(); }}
                className="px-3 py-1.5 bg-neutral-200 text-neutral-700 rounded-md text-xs font-medium hover:bg-neutral-300"
              >
                취소
              </button>
            </div>
          </div>
        ) : (
          <div className="flex justify-between items-start">
            <h3 className="text-base font-bold text-neutral-900 truncate flex-1 leading-tight">{cat.name}</h3>
            {isLevel3OrAbove && (
              <button
                onClick={e => { e.stopPropagation(); startEditName(cat); }}
                className="ml-2 text-neutral-400 hover:text-primary-600 transition-colors opacity-0 group-hover:opacity-100"
                title="이름 편집"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                </svg>
              </button>
            )}
          </div>
        )}

        {/* 장르, 평점, 리뷰, 글 수 */}
        {editingGenresId === cat.id ? (
          <div className="space-y-2" onClick={e => e.stopPropagation()}>
            <Select
              isMulti
              styles={customSelectStyles}
              options={allGenres.map(g => ({ value: g.id, label: g.name }))}
              value={editedGenres.map(g => ({ value: g.id, label: g.name }))}
              onChange={(opts) => setEditedGenres(opts.map((o: any) => ({ id: o.value, name: o.label })))}
              placeholder="장르 선택"
              menuPortalTarget={document.body}
            />
            <div className="flex gap-2 mt-2">
              <button
                onClick={() => saveGenresEdit(cat.id)}
                className="px-3 py-1.5 bg-primary-600 text-white rounded-md text-xs font-medium hover:bg-primary-700"
              >
                저장
              </button>
              <button
                onClick={cancelEditGenres}
                className="px-3 py-1.5 bg-neutral-200 text-neutral-700 rounded-md text-xs font-medium hover:bg-neutral-300"
              >
                취소
              </button>
            </div>
          </div>
        ) : (
          <div className="text-sm text-neutral-600 space-y-2.5">
            {/* 평점 */}
            <div>
              <span className="font-medium text-neutral-500 text-sm">평점 : </span>
              <button
                className="text-red-600 font-bold hover:text-red-700 hover:underline transition-colors"
                onClick={e => { e.stopPropagation(); navigate(`/${categoryName}/game/${cat.id}/reviews`); }}
              >
                {cat.rating ? cat.rating.toFixed(1) : '0.0'} <span className="text-neutral-600 font-normal text-xs">({cat.ratingCount || 0}개의 리뷰)</span>
              </button>
            </div>
            
            {/* 장르 */}
            <div className="flex justify-between items-start">
              <div className="flex-1">
                <span className="font-medium text-neutral-500 text-sm">장르 : </span>
                {cat.genres && cat.genres.length > 0 ? (
                  cat.genres.map((genre, index) => (
                    <span key={genre.id} className="text-neutral-600">
                      {genre.name}{index < (cat.genres?.length || 0) - 1 ? ', ' : ''}
                    </span>
                  ))
                ) : (
                  <span className="text-neutral-400">없음</span>
                )}
              </div>
              {isLevel3OrAbove && (
                <button
                  onClick={e => { e.stopPropagation(); startEditGenres(cat); }}
                  className="ml-2 text-neutral-400 hover:text-primary-600 transition-colors opacity-0 group-hover:opacity-100"
                  title="장르 편집"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                  </svg>
                </button>
              )}
            </div>
            
            {/* 글 수 */}
            <div className="pt-2 border-t border-neutral-200">
              <span className="font-medium text-neutral-500 text-sm">글 : </span>
              <span className="font-bold text-neutral-900">{cat.postCount || 0}</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
