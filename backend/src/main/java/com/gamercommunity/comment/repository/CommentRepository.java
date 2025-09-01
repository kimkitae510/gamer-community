package com.gamercommunity.comment.repository;

import com.gamercommunity.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 좋아요 수 증가
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") Long commentId);

    // 좋아요 수 감소
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = CASE WHEN c.likeCount > 0 THEN c.likeCount - 1 ELSE 0 END WHERE c.id = :commentId")
    void decrementLikeCount(@Param("commentId") Long commentId);

    // 게시글의 모든 댓글 조회 (작성자 fetch join)
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId")
    List<Comment> findByPostIdWithAuthor(@Param("postId") Long postId);

    // 게시글 삭제 시 해당 게시글의 모든 댓글 소프트 딜리트
    @Modifying
    @Query("UPDATE Comment c SET c.status = com.gamercommunity.global.enums.ContentStatus.DELETED WHERE c.post.id = :postId AND c.status = com.gamercommunity.global.enums.ContentStatus.ACTIVE")
    int softDeleteAllByPostId(@Param("postId") Long postId);

    // 게시글의 활성 댓글 ID 목록 조회 (좋아요 일괄 삭제용)
    @Query("SELECT c.id FROM Comment c WHERE c.post.id = :postId AND c.status = com.gamercommunity.global.enums.ContentStatus.ACTIVE")
    List<Long> findActiveCommentIdsByPostId(@Param("postId") Long postId);

    // 댓글 내용 수정 (직접 UPDATE)
    @Modifying
    @Query("UPDATE Comment c SET c.content = :content WHERE c.id = :commentId AND c.status = com.gamercommunity.global.enums.ContentStatus.ACTIVE")
    int updateContent(@Param("commentId") Long commentId, @Param("content") String content);

    // 댓글 단건 소프트 딜리트 (직접 UPDATE)
    @Modifying
    @Query("UPDATE Comment c SET c.status = com.gamercommunity.global.enums.ContentStatus.DELETED WHERE c.id = :commentId AND c.status = com.gamercommunity.global.enums.ContentStatus.ACTIVE")
    int softDeleteById(@Param("commentId") Long commentId);
}
