import api from '../axios';
import type { Post, PostRequest, PostListParams } from '../types';

export const postService = {
  //< 게시글 작성
  create: async (data: PostRequest): Promise<number> => {
    const response = await api.post<number>('/posts', data);
    return response.data;
  },

  //< 게시글 삭제
  delete: async (postId: number): Promise<string> => {
    const response = await api.delete<string>(`/posts/${postId}`);
    return response.data;
  },

  //< 게시글 수정
  update: async (postId: number, data: PostRequest): Promise<Post> => {
    const response = await api.patch<Post>(`/posts/${postId}`, data);
    return response.data;
  },

  //< 게시글 단건 조회
  getById: async (postId: number): Promise<Post> => {
    const response = await api.get<Post>(`/posts/${postId}`);
    return response.data;
  },

  //< 카테고리별 게시글 페이징 조회
  getByCategoryPaged: async (params: PostListParams & { page?: number }): Promise<{
    content: Post[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
  }> => {
    const { categoryId, sort = 'LATEST', tag, page = 0 } = params;
    const response = await api.get(`/posts/category/${categoryId}`, {
      params: { sort, tag: tag || undefined, page },
    });
    return response.data;
  },
};
