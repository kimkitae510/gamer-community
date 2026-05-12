// src/category/Header/CategoryHeader.tsx
import React from "react";
import { useNavigate } from "react-router-dom";
import type { Genre } from "./types";

interface Props {
  parentCategoryName: string;
  selectedGenreId: number | null;
  allGenres: Genre[];
  clearFilter: () => Promise<void> | void;
  isLevel3OrAbove: boolean;
}

export default function CategoryHeader({ parentCategoryName, selectedGenreId, allGenres, clearFilter, isLevel3OrAbove }: Props) {
  const navigate = useNavigate();

  // 플랫폼별 로고 이미지
  const getPlatformLogo = (categoryName: string) => {
    const logos: { [key: string]: string } = {
      'PlayStation': '/images/플스-removebg-preview.png',
      'Xbox': '/images/엑스박스-removebg-preview.png',
      'Nintendo': '/images/닌텐도.jpg-removebg-preview.png',
    };
    return logos[categoryName] || null;
  };

  // 카테고리별 설명
  const getCategoryDescription = (categoryName: string) => {
    const descriptions: { [key: string]: string } = {
      'PlayStation': 'PlayStation 게임들을 만나보세요',
      'Xbox': 'Xbox 게임들을 만나보세요',
      'Nintendo': 'Nintendo 게임들을 만나보세요',
      'PC': 'PC 게임들을 만나보세요',
      'Mobile': 'Mobile 게임들을 만나보세요'
    };
    return descriptions[categoryName] || '다양한 게임들을 만나보세요';
  };

  const platformLogo = getPlatformLogo(parentCategoryName);

  return (
    <div className="mb-8">
      {/* 헤더 */}
      <div className="flex justify-between items-start">
        <div className="flex items-center gap-2">
          {/* 플랫폼 로고 */}
          {platformLogo && (
            <div className="w-16 h-16 flex items-center justify-center flex-shrink-0">
              <img 
                src={platformLogo} 
                alt={parentCategoryName}
                className={`object-contain ${
                  parentCategoryName === 'Xbox' ? 'w-28 h-28' 
                  : 'w-16 h-16'
                }`}
              />
            </div>
          )}
          
          <div>
            <h1 className="text-2xl font-bold text-neutral-900 mb-1.5">
              {parentCategoryName || "Game Board"}
            </h1>
            <p className="text-base text-neutral-600">
              {getCategoryDescription(parentCategoryName)}
            </p>
          </div>
        </div>
        {isLevel3OrAbove && (
          <button
            onClick={() => navigate(`/board/create`)}
            className="group px-5 py-2.5 bg-gradient-to-r from-primary-600 to-primary-700 text-white text-sm font-semibold rounded-lg hover:from-primary-700 hover:to-primary-800 transition-all shadow-md hover:shadow-lg flex items-center gap-2.5"
          >
            <div className="w-5 h-5 rounded-full bg-white/20 flex items-center justify-center group-hover:bg-white/30 transition-colors">
              <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M12 4v16m8-8H4" />
              </svg>
            </div>
            <span>게시판 생성</span>
          </button>
        )}
      </div>

      {/* 필터 표시 */}
      {selectedGenreId && (
        <div className="mt-4 inline-flex items-center gap-2 px-4 py-2 bg-white border border-neutral-200 rounded-lg shadow-sm">
          <span className="text-neutral-700 font-medium text-sm">
            {allGenres.find(g => g.id === selectedGenreId)?.name}
          </span>
          <button 
            onClick={clearFilter} 
            className="text-neutral-400 hover:text-neutral-600 transition-colors"
          >
            ✕
          </button>
        </div>
      )}
    </div>
  );
}
