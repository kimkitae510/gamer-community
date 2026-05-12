// src/category/Cards/GameCardGrid.tsx
import React from "react";
import GameCard from "./GameCard";
import type { Category, Genre } from "./types";

interface Props {
  subCategories: Category[];
  categoryName?: string;
  loading: boolean;
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
  handleFileChange: (categoryId: number, e: React.ChangeEvent<HTMLInputElement>) => Promise<void>;
  handleDelete: (categoryId: number) => Promise<void>;
  getStarRating: (r: number) => string;
  isLevel3OrAbove: boolean;
}

export default function GameCardGrid(props: Props) {
  const {
    subCategories, categoryName, loading,
    uploadingId, editingNameId, editingGenresId,
    editedName, setEditedName, editedGenres, setEditedGenres,
    allGenres,
    startEditName, saveNameEdit, cancelEditName,
    startEditGenres, saveGenresEdit, cancelEditGenres,
    handleFileChange, handleDelete, getStarRating,
    isLevel3OrAbove
  } = props;

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <div className="text-center">
          <div className="animate-spin w-12 h-12 border-4 border-primary-200 border-t-primary-600 rounded-full mx-auto mb-4"></div>
          <p className="text-neutral-600 font-medium">게임들을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (subCategories.length === 0) {
    return (
      <div className="bg-neutral-50 rounded-lg border border-neutral-200 p-16 text-center">
        <div className="text-6xl mb-6">🎮</div>
        <h3 className="text-xl font-bold text-neutral-700 mb-3">아직 게임이 없어요</h3>
        <p className="text-neutral-500">새로운 게임 보드를 만들어보세요!</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
      {subCategories.map(cat => (
        <GameCard
          key={cat.id}
          cat={cat}
          categoryName={categoryName}
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
      ))}
    </div>
  );
}
