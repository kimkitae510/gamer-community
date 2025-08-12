import React from "react";
import Select from "react-select";
import customSelectStyles from "./selectStyles";
import type { Genre } from "./types";

interface Props {
  allGenres: Genre[];
  selectedGenreId: number | null;
  handleGenreChange: (opt: any) => void;
  sortOrder: 'latest' | 'oldest' | 'popular' | 'rating' | null;
  setSortOrder: (order: 'latest' | 'oldest' | 'popular' | 'rating' | null) => void;
  newGenreName: string;
  setNewGenreName: (s: string) => void;
  addGenre: () => Promise<void>;
  deleteSelectedGenre: () => Promise<void>;
  subCategoriesCount: number;
}

export default function GenreSidebar({
  allGenres,
  selectedGenreId,
  handleGenreChange,
  sortOrder,
  setSortOrder,
  newGenreName,
  setNewGenreName,
  addGenre,
  deleteSelectedGenre,
  subCategoriesCount,
}: Props) {
  return (
    <aside className="space-y-0">
      <div className="mb-6">
        <h2 className="text-xl font-bold text-neutral-900 mb-1.5">모든 게임 찾아보기</h2>
        <p className="text-sm text-neutral-600">게임 {subCategoriesCount.toLocaleString()}개</p>
      </div>

      <div className="space-y-4">
        <div className="pt-4 border-t border-neutral-200">
          <h3 className="text-lg font-semibold text-neutral-900 mb-3 uppercase tracking-wide">필터</h3>
        </div>

        <div>
          <label className="block text-sm text-neutral-700 mb-2.5 font-medium">장르</label>
          <Select
            styles={customSelectStyles}
            options={allGenres.map(g => ({ value: g.id, label: g.name }))}
            value={selectedGenreId ? {
              value: selectedGenreId,
              label: allGenres.find(g => g.id === selectedGenreId)?.name,
            } : null}
            onChange={handleGenreChange}
            isClearable
            placeholder="모든 장르"
          />
        </div>

        <div>
          <label className="block text-sm text-neutral-700 mb-2.5 font-medium">정렬</label>
          <div className="grid grid-cols-2 gap-2">
            {([
              { value: 'latest', label: '최신순' },
              { value: 'oldest', label: '오래된순' },
              { value: 'popular', label: '인기순' },
              { value: 'rating', label: '평점순' },
            ] as const).map(({ value, label }) => (
              <button
                key={value}
                onClick={() => setSortOrder(sortOrder === value ? null : value)}
                className={`px-3 py-2 text-sm font-medium rounded-md transition-colors ${
                  sortOrder === value
                    ? 'bg-primary-600 text-white'
                    : 'bg-neutral-100 text-neutral-700 hover:bg-neutral-200'
                }`}
              >
                {label}
              </button>
            ))}
          </div>
        </div>
      </div>
    </aside>
  );
}
