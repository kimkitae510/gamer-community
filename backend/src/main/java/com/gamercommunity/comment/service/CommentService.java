package com.gamercommunity.comment.service;

import com.gamercommunity.comment.dto.CommentCreateRequest;
import com.gamercommunity.comment.dto.CommentResponse;
import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.comment.repository.CommentRepository;
import com.gamercommunity.commentLike.repository.CommentLikeRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.popular.service.PopularScoreService;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PopularScoreService popularScoreService;

    // 댓글 작성 — save + @Modifying 여러 개 → 트랜잭션 필요
    @Transactional
    public Long saveComment(CommentCreateRequest commentRequest, String loginId) {
        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("게시글", commentRequest.getPostId()));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        Comment parent = null;
        String content = commentRequest.getContent();

        if (commentRequest.getParentId() != null) {
            Comment target = commentRepository.findById(commentRequest.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글", commentRequest.getParentId()));

            if (target.getStatus().isDeleted()) {
                throw new IllegalStateException("삭제된 댓글에는 답글을 작성할 수 없습니다.");
            }

            parent = (target.getParent() == null) ? target : target.getParent();
            content = "@" + target.getAuthor().getNickname() + " " + content;
        }

        Comment comment = Comment.builder()
                .content(content)
                .post(post)
                .author(user)
                .parent(parent)
                .build();

        if (parent != null) {
            parent.addChild(comment);
        }

        commentRepository.save(comment);
        postRepository.incrementCommentCount(post.getId());
        popularScoreService.onCommentCreated(post.getId());

        return comment.getId();
    }

    // 댓글 트리 조회 — JOIN FETCH 한 방 + 단순 SELECT, 트랜잭션 불필요
    public List<CommentResponse> getCommentsByPost(Long postId, String loginId) {
        List<Comment> allComments = commentRepository.findByPostIdWithAuthor(postId);

        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, CommentResponse> dtoMap = new HashMap<>();
        for (Comment c : allComments) {
            String displayContent = c.getStatus().isDeleted() ? "삭제된 댓글입니다" : c.getContent();
            String displayAuthor = c.getStatus().isDeleted() ? "알 수 없음" : c.getAuthor().getNickname();
            String displayAuthorId = c.getStatus().isDeleted() ? "" : c.getAuthor().getLoginId();

            dtoMap.put(c.getId(), CommentResponse.of(
                    c.getId(), displayContent, displayAuthor, displayAuthorId,
                    c.getCreatedAt(), c.getUpdatedAt(), c.getLikeCount()
            ));
        }

        if (loginId != null) {
            try {
                List<Long> activeIds = allComments.stream()
                        .filter(c -> c.getStatus().isActive())
                        .map(Comment::getId)
                        .collect(Collectors.toList());

                if (!activeIds.isEmpty()) {
                    Set<Long> likedIds = new HashSet<>(commentLikeRepository.findLikedCommentIds(activeIds, loginId));
                    dtoMap.values().forEach(dto -> dto.setIsLiked(likedIds.contains(dto.getId())));
                }
            } catch (Exception e) {
                log.warn("좋아요 정보 조회 실패: {}", e.getMessage());
            }
        }

        List<CommentResponse> result = new ArrayList<>();
        for (Comment c : allComments) {
            CommentResponse dto = dtoMap.get(c.getId());
            if (dto == null) continue;

            if (c.getParent() == null) {
                result.add(dto);
            } else {
                CommentResponse parentDto = dtoMap.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getReplies().add(dto);
                }
            }
        }

        return result;
    }

    // 댓글 삭제 — @Modifying 여러 개 → 트랜잭션 필요
    @Transactional
    public void deleteComment(Long commentId, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글", commentId));

        if (!comment.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }

        Long postId = comment.getPost().getId();

        commentRepository.softDeleteById(commentId);
        postRepository.decrementCommentCount(postId);
        popularScoreService.onCommentDeleted(postId);
    }

    // 댓글 수정 — @Modifying 단건 UPDATE → 트랜잭션 필요 (@Modifying 요구)
    @Transactional
    public void updateComment(Long commentId, String content, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글", commentId));

        if (!comment.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        int updated = commentRepository.updateContent(commentId, content);
        if (updated == 0) {
            throw new EntityNotFoundException("댓글 수정에 실패했습니다. id=" + commentId);
        }
    }
}
