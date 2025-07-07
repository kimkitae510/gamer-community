package com.gamercommunity.comment.service;

import com.gamercommunity.comment.dto.CommentRequest;
import com.gamercommunity.comment.dto.CommentResponse;
import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.comment.repository.CommentRepository;
import com.gamercommunity.commentLike.repository.CommentLikeRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 댓글 작성
    @Transactional
    public Long saveComment(CommentRequest commentRequest, String loginId) {
        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("게시글", commentRequest.getPostId()));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        Comment parent = null;
        String content = commentRequest.getContent();

        // 대댓글 처리
        if (commentRequest.getParentId() != null) {
            Comment target = commentRepository.findById(commentRequest.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글", commentRequest.getParentId()));


            if (target.getParent() == null) {

                parent = target;
            } else {

                parent = target.getParent();
            }

            // @멘션 추가
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

        return comment.getId();
    }

    // 게시글의 댓글 트리 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPost(Long postId, String loginId) {
        List<Comment> allComments = commentRepository.findByPostIdWithAuthor(postId);

        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, CommentResponse> dtoMap = new HashMap<>();
        for (Comment c : allComments) {
            CommentResponse dto = CommentResponse.of(
                    c.getId(),
                    c.getContent(),
                    c.getAuthor().getNickname(),
                    c.getAuthor().getLoginId(),
                    c.getCreatedAt(),
                    c.getUpdatedAt(),
                    c.getLikeCount()
            );
            dtoMap.put(c.getId(), dto);
        }

        // 좋아요 정보 일괄 조회
        if (loginId != null) {
            try {
                List<Long> commentIds = allComments.stream()
                        .map(Comment::getId)
                        .collect(Collectors.toList());


                List<Long> likedCommentIdList = commentLikeRepository.findLikedCommentIds(commentIds, loginId);
                Set<Long> likedCommentIds = new HashSet<>(likedCommentIdList);


                dtoMap.values().forEach(dto ->
                        dto.setIsLiked(likedCommentIds.contains(dto.getId()))
                );
            } catch (Exception e) {
                System.err.println("좋아요 정보 조회 실패: " + e.getMessage());
            }
        }

        // 댓글 트리 2단계
        List<CommentResponse> result = new ArrayList<>();
        for (Comment c : allComments) {
            CommentResponse dto = dtoMap.get(c.getId());
            if (dto == null) continue;

            if (c.getParent() == null) {
                // 원본 댓글
                result.add(dto);
            } else {
                // 대댓글
                CommentResponse parentDto = dtoMap.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }

        return result;
    }


    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글", commentId));

        if (!comment.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다");
        }

        Long postId = comment.getPost().getId();

        commentLikeRepository.deleteByCommentId(commentId);
        postRepository.decrementCommentCount(postId);
        commentRepository.delete(comment);
    }

    // 댓글 수정
    @Transactional
    public void updateComment(Long commentId, String content, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글", commentId));

        if (!comment.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다");
        }

        comment.updateContent(content);
    }

}

