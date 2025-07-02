package com.gamercommunity.comment.service;

import com.gamercommunity.comment.dto.CommentRequest;
import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.comment.repository.CommentRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

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

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String loginId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글", commentId));

        if (!comment.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다");
        }

        Long postId = comment.getPost().getId();

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

