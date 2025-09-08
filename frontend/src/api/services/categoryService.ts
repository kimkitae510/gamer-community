import api from '../axios';
import type { Category, CategoryRequest } from '../types';

export const categoryService = {
  //< 최상위 카테고리 목록 조회
  getParents: async (): Promise<Category[]> => {
    const response = await api.get<Category[]>('/categories/parents');
    return response.data;
  },

  //< 하위 카테고리 단건 조회
  getChildById: async (childId: number): Promise<Category> => {
    const response = await api.get<Category>(`/categories/children/${childId}`);
    return response.data;
  },

  //< 하위 카테고리 이미지 업로드
  uploadChildImage: async (childId: number, imageFile: File): Promise<string> => {
    const formData = new FormData();
    formData.append('image', imageFile);
    const response = await api.put<string>(`/categories/children/${childId}/image/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  //< 카테고리 장르 수정
  updateGenres: async (categoryId: number, data: CategoryRequest): Promise<Category> => {
    const response = await api.put<Category>(`/categories/${categoryId}/genres/update`, data);
    return response.data;
  },

  //< 카테고리 이름 수정
  updateName: async (categoryId: number, data: CategoryRequest): Promise<Category> => {
    const response = await api.put<Category>(`/categories/${categoryId}/name/update`, data);
    return response.data;
  },

  //< 하위 카테고리 삭제
  deleteChild: async (categoryId: number): Promise<string> => {
    const response = await api.delete<string>(`/categories/${categoryId}`);
    return response.data;
  },

  //< 최신 카테고리 목록 조회
  getNewCategories: async (): Promise<Category[]> => {
    const response = await api.get<Category[]>('/categories/new');
    return response.data;
  },

  //< 상위 카테고리별 하위 카테고리 페이징 조회
  getCategoriesWithPaging: async (
    parentId: number,
    page: number = 0,
    sortBy: string = 'latest',
    size: number = 12
  ): Promise<{ content: Category[]; totalPages: number; totalElements: number; size: number; number: number }> => {
    const response = await api.get(`/categories/parents/${parentId}/sorted`, {
      params: { page, sortBy, size },
    });
    return response.data;
  },

  //< 장르 필터 + 페이징 카테고리 조회
  getCategoriesByGenreWithPaging: async (
    parentId: number,
    genreId: number,
    page: number = 0,
    sortBy: string = 'latest',
    size: number = 12
  ): Promise<{ content: Category[]; totalPages: number; totalElements: number; size: number; number: number }> => {
    const response = await api.get(`/categories/parents/${parentId}/genres/${genreId}/sorted`, {
      params: { page, sortBy, size },
    });
    return response.data;
  },
};
