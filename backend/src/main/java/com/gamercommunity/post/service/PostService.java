package com.gamercommunity.post.service;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.comment.repository.CommentRepository;
import com.gamercommunity.commentLike.repository.CommentLikeRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.global.exception.custom.InvalidRequestException;
import com.gamercommunity.popular.entity.PopularScore;
import com.gamercommunity.popular.repository.PopularScoreRepository;
import com.gamercommunity.post.dto.PostCreateRequest;
import com.gamercommunity.post.dto.PostResponse;
import com.gamercommunity.post.dto.PostUpdateRequest;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.entity.PostSort;
import com.gamercommunity.post.entity.Tag;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.postLike.repository.PostLikeRepository;
import com.gamercommunity.stats.service.CategoryStatsService;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PopularScoreRepository popularScoreRepository;
    private final CategoryStatsService categoryStatsService;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    // 게시글 작성
    @Transactional
    public long createPost(PostCreateRequest request, String loginId) {
        User author = findUserByLoginId(loginId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리", request.getCategoryId()));

        if (!category.isWritable()) {
            throw new InvalidRequestException("해당 카테고리에는 게시글을 작성할 수 없습니다.");
        }

        Post post = Post.builder()
                .author(author)
                .category(category)
                .views(0)
                .likeCount(0)
                .commentCount(0)
                .title(request.getTitle())
                .content(request.getContent())
                .tag(request.getTag())
                .build();

        postRepository.save(post);

        popularScoreRepository.save(PopularScore.builder()
                .post(post)
                .score(0)
                .commentScore(0)
                .likeScore(0)
                .viewScore(0)
                .lastViewScoreCheckpoint(0)
                .isTrending(false)
                .build());

        categoryRepository.incrementPostCount(request.getCategoryId());
        categoryStatsService.onPostCreated(category);

        return post.getId();
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, String loginId) {
        Post post = findActivePostById(postId);
        validateAuthor(post, loginId);

        Long categoryId = post.getCategory().getId();

        commentLikeRepository.deleteByPostId(postId);
        commentRepository.softDeleteAllByPostId(postId);
        postLikeRepository.deleteByPostId(postId);
        post.softDelete();
        categoryRepository.decrementPostCount(categoryId);
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String loginId) {
        Post post = findActivePostById(postId);
        validateAuthor(post, loginId);

        int updated = postRepository.updateTitleAndContent(postId, request.getTitle(), request.getContent());
        if (updated == 0) {
            throw new EntityNotFoundException("게시글 수정에 실패했습니다. id=" + postId);
        }

        // 수정된 값을 반영해서 응답 (DB 재조회 없이 메모리에서 구성)
        return PostResponse.builder()
                .id(post.getId())
                .title(request.getTitle())
                .content(request.getContent())
                .author(post.getAuthor().getNickname())
                .authorId(post.getAuthor().getLoginId())
                .categoryId(post.getCategory().getId())
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .views(post.getViews())
                .createdAt(post.getCreatedAt().toString())
                .updatedAt(post.getUpdatedAt().toString())
                .tag(post.getTag() != null ? post.getTag().toString() : "일반")
                .build();
    }

    // 게시글 단건 조회
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글", postId));

        if (post.getStatus().isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다. id=" + postId);
        }

        return PostResponse.from(post);
    }

    // 카테고리별 게시글 목록
    public List<PostResponse> getPostsByCategoryAndSort(Long categoryId, PostSort postSort, Tag tag) {
        validateCategoryExists(categoryId);

        return postRepository.findByCategoryWithFilters(categoryId, tag, postSort)
                .stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    // 카테고리별 게시글 목록 (페이징)
    public Page<PostResponse> getPostsByCategoryWithPaging(Long categoryId, PostSort postSort, Tag tag, Pageable pageable) {
        validateCategoryExists(categoryId);

        return postRepository.findByCategoryWithPaging(categoryId, tag, postSort, pageable)
                .map(PostResponse::from);
    }

    // === Private Helper Methods ===

    private User findUserByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));
    }

    private Post findActivePostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글", postId));

        if (post.getStatus().isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다. id=" + postId);
        }

        return post;
    }

    private void validateAuthor(Post post, String loginId) {
        if (!post.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 게시글만 수정/삭제할 수 있습니다.");
        }
    }

    private void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("카테고리", categoryId);
        }
    }
}
