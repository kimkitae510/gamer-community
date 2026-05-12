package com.gamercommunity.post.repository;

import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,Long>, PostRepositoryCustom {

    // 게시글 상세 조회
    @Query("SELECT p FROM Post p " +
           "LEFT JOIN FETCH p.author " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.id = :postId")
    Optional<Post> findByIdWithDetails(@Param("postId") Long postId);

    // 조회수 증가
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    // 좋아요 수 증가
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    // 좋아요 수 감소
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :postId")
    void decrementLikeCount(@Param("postId") Long postId);

    // 댓글 수 증가
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    // 댓글 수 감소
    @Modifying
    @Query("UPDATE Post p SET p.commentCount = CASE WHEN p.commentCount > 0 THEN p.commentCount - 1 ELSE 0 END WHERE p.id = :postId")
    void decrementCommentCount(@Param("postId") Long postId);

    // 게시글 제목/내용 수정 (직접 UPDATE)
    @Modifying
    @Query("UPDATE Post p SET p.title = :title, p.content = :content, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :postId AND p.status = com.gamercommunity.global.enums.ContentStatus.ACTIVE")
    int updateTitleAndContent(@Param("postId") Long postId, @Param("title") String title, @Param("content") String content);

    // 트랜잭션 없는 단건 조회 (성능 테스트용)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdNoTx(@Param("id") Long id);

    // AI 자동 답변 대상: 태그가 '질문'이고, 댓글이 없고, 생성된 지 10분 이상 지난 활성 게시글
    @Query("SELECT p FROM Post p " +
           "WHERE p.tag = :tag " +
           "AND p.commentCount = 0 " +
           "AND p.status = com.gamercommunity.global.enums.ContentStatus.ACTIVE " +
           "AND p.createdAt <= :threshold")
    List<Post> findUnansweredQuestionPosts(@Param("tag") Tag tag,
                                           @Param("threshold") LocalDateTime threshold);
}
