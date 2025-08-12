package com.gamercommunity.popular.repository;

import com.gamercommunity.popular.entity.PopularScore;
import com.gamercommunity.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PopularScoreRepository extends JpaRepository<PopularScore, Long> {
    
    Optional<PopularScore> findByPost(Post post);
    
    Optional<PopularScore> findByPostId(Long postId);
    
    // 댓글 점수 원자적 증가 (+3점)
    @Modifying
    @Query("UPDATE PopularScore ps SET ps.score = ps.score + 3, ps.commentScore = ps.commentScore + 3, ps.updatedAt = CURRENT_TIMESTAMP WHERE ps.post.id = :postId")
    int incrementCommentScoreAtomic(@Param("postId") Long postId);
    
    // 댓글 점수 원자적 감소 (-3점)
    @Modifying
    @Query("UPDATE PopularScore ps SET ps.score = ps.score - 3, ps.commentScore = ps.commentScore - 3, ps.updatedAt = CURRENT_TIMESTAMP WHERE ps.post.id = :postId")
    int decrementCommentScoreAtomic(@Param("postId") Long postId);
    
    // 추천 점수 원자적 증가 (+5점)
    @Modifying
    @Query("UPDATE PopularScore ps SET ps.score = ps.score + 5, ps.likeScore = ps.likeScore + 5, ps.updatedAt = CURRENT_TIMESTAMP WHERE ps.post.id = :postId")
    int incrementLikeScoreAtomic(@Param("postId") Long postId);
    
    // 추천 점수 원자적 감소 (-5점)
    @Modifying
    @Query("UPDATE PopularScore ps SET ps.score = ps.score - 5, ps.likeScore = ps.likeScore - 5, ps.updatedAt = CURRENT_TIMESTAMP WHERE ps.post.id = :postId")
    int decrementLikeScoreAtomic(@Param("postId") Long postId);

    // 조회수 체크포인트 기반 점수 업데이트
    // 조회수가 100 단위 구간을 넘을 때만 점수 갱신, 중복 계산 방지
    @Modifying
    @Query(value =
            "UPDATE popular_score ps " +
            "JOIN post p ON ps.post_id = p.id " +
            "SET " +
            "    ps.score = ps.score + (p.views DIV 100 - ps.last_view_score_checkpoint), " +
            "    ps.view_score = ps.view_score + (p.views DIV 100 - ps.last_view_score_checkpoint), " +
            "    ps.last_view_score_checkpoint = p.views DIV 100, " +
            "    ps.updated_at = NOW() " +
            "WHERE ps.post_id = :postId " +
            "  AND p.views DIV 100 > ps.last_view_score_checkpoint",
            nativeQuery = true)
    int updateViewScoreAtCheckpoint(@Param("postId") Long postId);

    // 실시간 인기글 플래그 일괄 업데이트 (24시간 이내 & 100점 이상 = true)
    @Modifying
    @Query(value =
            "UPDATE popular_score ps " +
            "JOIN post p ON ps.post_id = p.id " +
            "SET ps.is_trending = true, ps.updated_at = NOW() " +
            "WHERE p.created_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) " +
            "  AND ps.score >= 100 " +
            "  AND ps.is_trending = false",
            nativeQuery = true)
    int updateTrendingToTrue();

    // 실시간 인기글 조회 - 최신순
    @Query("SELECT ps FROM PopularScore ps " +
           "JOIN FETCH ps.post p " +
           "JOIN FETCH p.author " +
           "JOIN FETCH p.category " +
           "WHERE ps.isTrending = true " +
           "AND p.status = com.gamercommunity.global.enums.ContentStatus.ACTIVE " +
           "ORDER BY p.createdAt DESC")
    List<PopularScore> findTrendingPosts();

    // 실시간 인기글 상위 N개 조회 - 최신순
    @Query("SELECT ps FROM PopularScore ps " +
           "JOIN FETCH ps.post p " +
           "JOIN FETCH p.author " +
           "JOIN FETCH p.category " +
           "WHERE ps.isTrending = true " +
           "AND p.status = com.gamercommunity.global.enums.ContentStatus.ACTIVE " +
           "ORDER BY p.createdAt DESC")
    List<PopularScore> findTopNTrendingPosts(org.springframework.data.domain.Pageable pageable);
}
