package com.gamercommunity.post.service;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.category.repository.CategoryRepository;
import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.popular.entity.PopularScore;
import com.gamercommunity.popular.repository.PopularScoreRepository;
import com.gamercommunity.popular.service.PopularScoreService;
import com.gamercommunity.post.dto.PostRequest;
import com.gamercommunity.post.dto.PostResponse;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.entity.PostSort;
import com.gamercommunity.post.entity.Tag;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.stats.service.CategoryStatsService;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PostService {

    private  final UserRepository userRepository;
    private  final PostRepository postRepository;
    private  final CategoryRepository categoryRepository;
    private  final PopularScoreRepository popularScoreRepository;
    private  final PopularScoreService popularScoreService;
    private  final CategoryStatsService categoryStatsService;



    // 게시글 작성
    @Transactional
    public long createPost(PostRequest postRequest, String loginId) {
        User author = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        Category category = categoryRepository.findById(postRequest.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("카테고리", postRequest.getCategoryId()));

        Post post = Post.builder()
                .author(author)
                .category(category)
                .views(0)
                .likeCount(0)
                .commentCount(0)
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .tag(postRequest.getTag())
                .build();

        postRepository.save(post);

        // 게시글 생성 시 인기점수 0으로 초기화
        popularScoreRepository.save(PopularScore.builder()
                .post(post)
                .score(0)
                .commentScore(0)
                .likeScore(0)
                .viewScore(0)
                .lastViewScoreCheckpoint(0)
                .isTrending(false)
                .build());

        categoryRepository.incrementPostCount(postRequest.getCategoryId());

        // 일간/주간/월간 통계 업데이트
        categoryStatsService.onPostCreated(category);

        return post.getId();
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        if (!post.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 게시글만 삭제할 수 있습니다.");
        }
        
        Long categoryId = post.getCategory().getId();

        post.softDelete();

        categoryRepository.decrementPostCount(categoryId);
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long postId, PostRequest postRequest, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        if (!post.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 게시글만 수정할 수 있습니다.");
        }

        post.update(postRequest.getTitle(), postRequest.getContent());

        return PostResponse.from(post);
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponse getPost(Long postId) {

        postRepository.incrementViewCount(postId);

        // 조회수 체크포인트 기반 인기점수 갱신
        popularScoreService.onPostViewed(postId);
        
        Post post = postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다. id=" + postId));

        if (post.getStatus().isDeleted()) {
            throw new EntityNotFoundException("삭제된 게시글입니다. id=" + postId);
        }

        return PostResponse.from(post);
    }

    // 카테고리별 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByCategoryAndSort(Long categoryId, PostSort postSort, Tag tag) {

        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("카테고리", categoryId));


        List<Post> posts = postRepository.findByCategoryWithFilters(categoryId, tag, postSort);

        return posts.stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }
}
