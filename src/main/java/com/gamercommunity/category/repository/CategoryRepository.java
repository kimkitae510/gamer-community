package com.gamercommunity.category.repository;

import com.gamercommunity.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    //부모가 없는 카테고리 찾기 = 부모카테고리 찾기
    List<Category> findByParentIsNull();

    //게임기종별 게임목록 최신순
    List<Category> findByParentIdOrderByCreatedAtDesc(Long parentId);

    // 부모 카테고리별 정렬 (페이징 지원)
    Page<Category> findByParentId(Long parentId, Pageable pageable);

    // 장르별 카테고리 목록 조회 (페이징 지원)
    @Query("""
        SELECT DISTINCT c FROM Category c 
        JOIN CategoryGenre cg ON cg.category = c
        WHERE c.parent.id = :parentId 
        AND cg.genre.id = :genreId
    """)
    Page<Category> findByParentIdAndGenreId(
        @Param("parentId") Long parentId, 
        @Param("genreId") Long genreId,
        Pageable pageable
    );

    // 평점 재계산 및 업데이트 (삭제된 리뷰 제외)
    @Modifying
    @Query("""
        UPDATE Category c
        SET c.rating = (
            SELECT COALESCE(AVG(r.rating), 0.0)
            FROM Review r
            WHERE r.game.id = :categoryId
            AND r.status = 'ACTIVE'
        )
        WHERE c.id = :categoryId
    """)
    void recalculateRating(@Param("categoryId") Long categoryId);

    // 리뷰 개수 증가
    @Modifying
    @Query("UPDATE Category c SET c.reviewCount = c.reviewCount + 1 WHERE c.id = :categoryId")
    void incrementReviewCount(@Param("categoryId") Long categoryId);

    // 리뷰 개수 감소
    @Modifying
    @Query("UPDATE Category c SET c.reviewCount = CASE WHEN c.reviewCount > 0 THEN c.reviewCount - 1 ELSE 0 END WHERE c.id = :categoryId")
    void decrementReviewCount(@Param("categoryId") Long categoryId);

    // 게시글 개수 증가
    @Modifying
    @Query("UPDATE Category c SET c.postCount = c.postCount + 1 WHERE c.id = :categoryId")
    void incrementPostCount(@Param("categoryId") Long categoryId);

    // 게시글 개수 감소
    @Modifying
    @Query("UPDATE Category c SET c.postCount = CASE WHEN c.postCount > 0 THEN c.postCount - 1 ELSE 0 END WHERE c.id = :categoryId")
    void decrementPostCount(@Param("categoryId") Long categoryId);

    // 신설 게시판 조회 (최신순, 최대 10개)
    @Query("SELECT c FROM Category c WHERE c.isNew = true ORDER BY c.createdAt DESC")
    List<Category> findNewCategories();

    // isNew = true인 게시판 개수
    @Query("SELECT COUNT(c) FROM Category c WHERE c.isNew = true")
    long countNewCategories();

    // isNew = true인 게시판 중 가장 오래된 것
    @Query("SELECT c FROM Category c WHERE c.isNew = true ORDER BY c.createdAt ASC")
    List<Category> findOldestNewCategory();

}
