package com.gamercommunity.category.repository;

import com.gamercommunity.category.entity.CategoryGenre;
import com.gamercommunity.genre.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryGenreRepository extends JpaRepository<CategoryGenre, Long> {

    // 카테고리의 장르 삭제 (장르 수정 시 사용)
    void deleteByCategoryId(Long categoryId);

    // 카테고리의 장르 조회
    @Query("SELECT cg.genre FROM CategoryGenre cg WHERE cg.category.id = :categoryId")
    List<Genre> findGenresByCategoryId(@Param("categoryId") Long categoryId);

    // 여러 카테고리의 장르 조회
    @Query("SELECT cg FROM CategoryGenre cg JOIN FETCH cg.genre WHERE cg.category.id IN :categoryIds")
    List<CategoryGenre> findByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds);
}
