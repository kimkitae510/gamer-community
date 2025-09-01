package com.gamercommunity.post.service;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.comment.repository.CommentRepository;
import com.gamercommunity.commentLike.repository.CommentLikeRepository;
import com.gamercommunity.global.enums.ContentStatus;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.popular.entity.PopularScore;
import com.gamercommunity.popular.repository.PopularScoreRepository;
import com.gamercommunity.post.dto.PostCreateRequest;
import com.gamercommunity.post.dto.PostResponse;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.entity.Tag;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.postLike.repository.PostLikeRepository;
import com.gamercommunity.stats.service.CategoryStatsService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock private UserRepository userRepository;
    @Mock private PostRepository postRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private PopularScoreRepository popularScoreRepository;
    @Mock private CategoryStatsService categoryStatsService;
    @Mock private CommentRepository commentRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private CommentLikeRepository commentLikeRepository;

    @Test
    @DisplayName("게시글 단건 조회 - 존재하지 않는 ID면 EntityNotFoundException")
    void getPost_notFound() {
        // given
        given(postRepository.findByIdWithDetails(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPost(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("게시글 단건 조회 - 삭제된 게시글이면 EntityNotFoundException")
    void getPost_deleted() {
        // given
        Post deletedPost = createPost(1L, "testUser");
        deletedPost.softDelete();
        given(postRepository.findByIdWithDetails(1L)).willReturn(Optional.of(deletedPost));

        // when & then
        assertThatThrownBy(() -> postService.getPost(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("삭제된");
    }

    @Test
    @DisplayName("게시글 단건 조회 - 정상 조회")
    void getPost_success() {
        // given
        Post post = createPost(1L, "testUser");
        given(postRepository.findByIdWithDetails(1L)).willReturn(Optional.of(post));

        // when
        PostResponse response = postService.getPost(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    @DisplayName("게시글 삭제 - 본인이 아닌 사용자가 삭제 시도하면 AccessDeniedException")
    void deletePost_notAuthor() {
        // given
        Post post = createPost(1L, "author1");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.deletePost(1L, "otherUser"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("게시글 삭제 - 정상 삭제 시 댓글/좋아요 함께 처리")
    void deletePost_success() {
        // given
        Post post = createPost(1L, "testUser");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.deletePost(1L, "testUser");

        // then
        assertThat(post.getStatus()).isEqualTo(ContentStatus.DELETED);
        then(commentLikeRepository).should().deleteByPostId(1L);
        then(commentRepository).should().softDeleteAllByPostId(1L);
        then(postLikeRepository).should().deleteByPostId(1L);
        then(categoryRepository).should().decrementPostCount(any());
    }

    // === 헬퍼 ===

    private Post createPost(Long id, String loginId) {
        User author = User.builder().loginId(loginId).nickname("닉네임").build();
        Category category = Category.builder().name("PS5").writable(true).build();

        Post post = Post.builder()
                .author(author)
                .category(category)
                .title("테스트 제목")
                .content("테스트 내용")
                .tag(Tag.일반)
                .views(0)
                .likeCount(0)
                .commentCount(0)
                .build();

        // id 세팅 (리플렉션)
        try {
            var idField = Post.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(post, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return post;
    }
}
