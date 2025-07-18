package com.gamercommunity.popular.repository;

import com.gamercommunity.popular.entity.PopularScore;
import com.gamercommunity.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PopularScoreRepository extends JpaRepository<PopularScore, Long> {
    
    Optional<PopularScore> findByPost(Post post);
    
    Optional<PopularScore> findByPostId(Long postId);
    
    // 댓글 점수 원자적 증가 (+3점)
    @Modifying
    @Query("UPDATE PopularScore ps SET ps.score = ps.score + 3, ps.commentScore = ps.commentScore + 3 WHERE ps.post.id = :postId")
    int incrementCommentScoreAtomic(@Param("postId") Long postId);
    
    // 댓글 점수 원자적 감소 (-3점)
    @Modifying
    @Query("UPDATE PopularScore ps SET ps.score = ps.score - 3, ps.commentScore = ps.commentScore - 3 WHERE ps.post.id = :postId")
    int decrementCommentScoreAtomic(@Param("postId") Long postId);
    
    // 추천 점수 원자적 증가 (+5점)
    @Modifying
    @Query("UPDATE PopularScore ps SET ps.score = ps.score + 5, ps.likeScore = ps.likeScore + 5 WHERE ps.post.id = :postId")
    int incrementLikeScoreAtomic(@Param("postId") Long postId);
    
    // 추천 점수 원자적 감소 (-5점)
    @Modifying
    @Query("UPDATE PopularScore ps SET ps.score = ps.score - 5, ps.likeScore = ps.likeScore - 5 WHERE ps.post.id = :postId")
    int decrementLikeScoreAtomic(@Param("postId") Long postId);
}
