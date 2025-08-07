package com.gamercommunity.category.repository;

import com.gamercommunity.category.entity.Category;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // 비관적 락 - game row X lock (테스트용: 성능 비교)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Category c WHERE c.id = :categoryId")
    Optional<Category> findByIdForUpdate(@Param("categoryId") Long categoryId);

    // 리뷰 등록 시: count+1, sum+rating, avg=새sum/새count
    @Modifying
    @Query("""
        UPDATE Category c
        SET c.reviewCount = c.reviewCount + 1,
            c.ratingSum   = c.ratingSum + :rating,
            c.rating      = c.ratingSum / c.reviewCount
        WHERE c.id = :categoryId
    """)
    void addRating(@Param("categoryId") Long categoryId, @Param("rating") int rating);

    // 리뷰 삭제 시: count-1, sum-rating, avg=새sum/새count (count=1이면 avg=0)
    @Modifying
    @Query("""
        UPDATE Category c
        SET c.reviewCount = c.reviewCount - 1,
            c.ratingSum   = c.ratingSum - :rating,
            c.rating      = CASE
                                WHEN c.reviewCount <= 0 THEN 0.0
                                ELSE c.ratingSum / c.reviewCount
                            END
        WHERE c.id = :categoryId
    """)
    void subtractRating(@Param("categoryId") Long categoryId, @Param("rating") int rating);

    // 리뷰 수정 시: 기존 평점 빼고 새 평점 더하기, avg=새sum/count
    @Modifying
    @Query("""
        UPDATE Category c
        SET c.ratingSum = c.ratingSum - :oldRating + :newRating,
            c.rating    = c.ratingSum / c.reviewCount
        WHERE c.id = :categoryId
    """)
    void updateRatingDiff(@Param("categoryId") Long categoryId,
                          @Param("oldRating") int oldRating,
                          @Param("newRating") int newRating);

    // UPDATE 안에 SELECT 서브쿼리 → Current Read → 동시 INSERT와 데드락 발생 가능
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

    // AVG만 단독 SELECT (MVCC 스냅샷 읽기 → 데드락 없음)
    @Query("""
        SELECT COALESCE(AVG(r.rating), 0.0)
        FROM Review r
        WHERE r.game.id = :categoryId
        AND r.status = 'ACTIVE'
    """)
    Double calculateAvgRating(@Param("categoryId") Long categoryId);

    // 미리 계산된 AVG값으로 UPDATE (review 테이블 건드리지 않음)
    @Modifying
    @Query("UPDATE Category c SET c.rating = :rating WHERE c.id = :categoryId")
    void updateRating(@Param("categoryId") Long categoryId, @Param("rating") Double rating);

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

    // 자식 카테고리 개수 증가
    @Modifying
    @Query("UPDATE Category c SET c.childCount = c.childCount + 1 WHERE c.id = :categoryId")
    void incrementChildCount(@Param("categoryId") Long categoryId);

    // 자식 카테고리 개수 감소
    @Modifying
    @Query("UPDATE Category c SET c.childCount = CASE WHEN c.childCount > 0 THEN c.childCount - 1 ELSE 0 END WHERE c.id = :categoryId")
    void decrementChildCount(@Param("categoryId") Long categoryId);

    // 신설 게시판 조회 (최신순, 최대 10개)
    @Query("SELECT c FROM Category c WHERE c.isNew = true ORDER BY c.createdAt DESC")
    List<Category> findNewCategories();

    // isNew = true인 게시판 개수
    @Query("SELECT COUNT(c) FROM Category c WHERE c.isNew = true")
    long countNewCategories();

    // isNew = true인 게시판 중 가장 오래된 것
    @Query("SELECT c FROM Category c WHERE c.isNew = true ORDER BY c.createdAt ASC")
    List<Category> findOldestNewCategory();

    // 카테고리 이름 수정 (직접 UPDATE)
    @Modifying
    @Query("UPDATE Category c SET c.name = :name WHERE c.id = :categoryId")
    int updateName(@Param("categoryId") Long categoryId, @Param("name") String name);

    // 카테고리 이미지 URL 수정 (직접 UPDATE)
    @Modifying
    @Query("UPDATE Category c SET c.imageUrl = :imageUrl WHERE c.id = :categoryId")
    int updateImageUrl(@Param("categoryId") Long categoryId, @Param("imageUrl") String imageUrl);
}
