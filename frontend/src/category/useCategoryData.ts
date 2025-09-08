import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { categoryService } from "../api/services";
import api from "../api/axios";
import type { Category, Genre } from "./types";

export function useCategoryData(categoryName?: string) {
  const navigate = useNavigate();

  const [subCategories, setSubCategories] = useState<Category[]>([]);
  const [allGenres, setAllGenres] = useState<Genre[]>([]);
  const [parentCategoryName, setParentCategoryName] = useState("");
  const [parentId, setParentId] = useState<number | null>(null);
  const [parentChildCount, setParentChildCount] = useState<number>(0);
  const [loading, setLoading] = useState(true);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);

  const fetchAllGenres = async () => {
    try {
      const res = await api.get<Genre[]>("/genres");
      setAllGenres(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchSubCategories = async (pid: number, sortBy: string = 'latest', reset: boolean = true) => {
    if (!pid) return;
    try {
      reset ? setLoading(true) : setLoadingMore(true);
      if (reset) { setPage(0); setSubCategories([]); }

      const pageNum = reset ? 0 : page + 1;
      const data = await categoryService.getCategoriesWithPaging(pid, pageNum, sortBy, 12);

      setSubCategories(prev => reset ? data.content : [...prev, ...data.content]);
      setPage(pageNum);
      setTotalPages(data.totalPages);
      setHasMore(pageNum < data.totalPages - 1);
    } catch (err) {
      console.error(err);
    } finally {
      reset ? setLoading(false) : setLoadingMore(false);
    }
  };

  const fetchSubCategoriesByGenre = async (pid: number, genreId: number, sortBy: string = 'latest', reset: boolean = true) => {
    if (!pid) return;
    try {
      reset ? setLoading(true) : setLoadingMore(true);
      if (reset) { setPage(0); setSubCategories([]); }

      const pageNum = reset ? 0 : page + 1;
      const data = await categoryService.getCategoriesByGenreWithPaging(pid, genreId, pageNum, sortBy, 12);

      setSubCategories(prev => reset ? data.content : [...prev, ...data.content]);
      setPage(pageNum);
      setTotalPages(data.totalPages);
      setHasMore(pageNum < data.totalPages - 1);
    } catch (err) {
      console.error(err);
    } finally {
      reset ? setLoading(false) : setLoadingMore(false);
    }
  };

  const loadMore = async (pid: number, genreId: number | null, sortBy: string = 'latest') => {
    if (loadingMore || !hasMore) return;
    if (genreId) {
      await fetchSubCategoriesByGenre(pid, genreId, sortBy, false);
    } else {
      await fetchSubCategories(pid, sortBy, false);
    }
  };

  const fetchParentAndChildren = async () => {
    if (!categoryName) return;
    try {
      const parentsRes = await api.get<Category[]>('/categories/parents');
      const parent = parentsRes.data.find(
        (cat) => cat.name.toLowerCase() === categoryName.toLowerCase()
      );

      if (!parent) {
        alert("존재하지 않는 카테고리입니다.");
        navigate(-1);
        return;
      }

      setParentCategoryName(parent.name);
      setParentId(parent.id);
      setParentChildCount(parent.childCount ?? 0);
      await fetchSubCategories(parent.id);
    } catch (error) {
      console.error(error);
      alert("카테고리를 불러오는데 실패했습니다.");
      navigate(-1);
    }
  };

  useEffect(() => { fetchAllGenres(); }, []);
  useEffect(() => { fetchParentAndChildren(); }, [categoryName]);

  return {
    subCategories,
    setSubCategories,
    allGenres,
    setAllGenres,
    parentCategoryName,
    parentId,
    parentChildCount,
    loading,
    setLoading,
    fetchParentAndChildren,
    fetchSubCategories,
    fetchSubCategoriesByGenre,
    fetchAllGenres,
    page,
    totalPages,
    hasMore,
    loadingMore,
    loadMore,
  };
}
