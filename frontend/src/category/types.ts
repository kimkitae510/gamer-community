// src/category/types.ts
export interface Genre {
  id: number;
  name: string;
}

export interface Category {
  id: number;
  name: string;
  imageUrl?: string;
  rating?: number;
  ratingCount?: number;
  genres?: Genre[];
  postCount?: number;
  childCount?: number;
  createdAt?: string;
}

