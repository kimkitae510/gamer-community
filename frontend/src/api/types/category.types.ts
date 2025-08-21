// 카테고리 관련 타입 정의
export interface Category {
  id: number;
  name: string;
  imageUrl?: string;
  parentId?: number | null;
  writable: boolean;
  rating?: number;
  reviewCount?: number;
  ratingCount?: number;
  postCount?: number;
  genres?: Genre[];
  createdAt?: string;
}

export interface CategoryWithChildren extends Category {
  children: Category[];
}

export interface CategoryRequest {
  name: string;
  parentId?: number;
  genreIds?: number[];
}

export interface Genre {
  id: number;
  name: string;
}
