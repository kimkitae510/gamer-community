package com.gamercommunity.comment.service;

import com.gamercommunity.comment.dto.CommentCreateRequest;
import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.comment.repository.CommentRepository;
import com.gamercommunity.commentLike.repository.CommentLikeRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.popular.service.PopularScoreService;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentLikeRepository commentLikeRepository;
    @Mock private PopularScoreService popularScoreService;

    @Test
    @DisplayName("댓글 작성 - 존재하지 않는 게시글이면 EntityNotFoundException")
    void saveComment_postNotFound() {
        // given
        CommentCreateRequest request = createRequest(999L, "내용", null);
        given(postRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.saveComment(request, "testUser"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("댓글 삭제 - 본인이 아닌 사용자가 삭제 시도하면 AccessDeniedException")
    void deleteComment_notAuthor() {
        // given
        User author = User.builder().loginId("author1").nickname("작성자").build();
        Post post = Post.builder().author(author).title("제목").content("내용").build();

        Comment comment = Comment.builder()
                .content("댓글")
                .post(post)
                .author(author)
                .build();

        setId(comment, 1L);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(1L, "otherUser"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("댓글 수정 - 존재하지 않는 댓글이면 EntityNotFoundException")
    void updateComment_notFound() {
        // given
        given(commentRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(999L, "수정 내용", "testUser"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 헬퍼 ===

    private CommentCreateRequest createRequest(Long postId, String content, Long parentId) {
        try {
            var constructor = CommentCreateRequest.class.getDeclaredConstructor(Long.class, String.class, Long.class);
            constructor.setAccessible(true);
            return constructor.newInstance(postId, content, parentId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setId(Object entity, Long id) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
