package com.gamercommunity.postLike.service;

import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.popular.service.PopularScoreService;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.postLike.dto.PostLikeResponse;
import com.gamercommunity.postLike.entity.PostLike;
import com.gamercommunity.postLike.repository.PostLikeRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PopularScoreService popularScoreService;

    // 게시글 좋아요 토글 (추가/취소)
    @Transactional
    public PostLikeResponse toggleLike(Long postId, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글", postId));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        boolean liked;
        if (existingLike.isPresent()) {
            // 좋아요 취소
            postRepository.decrementLikeCount(postId);
            postLikeRepository.delete(existingLike.get());
            popularScoreService.onLikeCancelled(postId);
            liked = false;
        } else {
            // 좋아요 추가
            try {
                postLikeRepository.save(PostLike.builder()
                        .post(post)
                        .user(user)
                        .build());
                postRepository.incrementLikeCount(postId);
                popularScoreService.onLikeCreated(postId);
                liked = true;
            } catch (DataIntegrityViolationException e) {
                // 유니크 제약조건 위반 (동시성 이슈 대응)
                throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
            }
        }

        // 엔티티에서 직접 조회 (추가 SELECT 불필요)
        return PostLikeResponse.builder()
                .id(postId)
                .likeCount(post.getLikeCount() + (liked ? 1 : -1))
                .isLiked(liked)
                .build();
    }

    // 좋아요 상태 조회
    public PostLikeResponse getLikeStatus(Long postId, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글", postId));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자", "loginId=" + loginId));

        boolean isLiked = postLikeRepository.findByUserAndPost(user, post).isPresent();

        return PostLikeResponse.builder()
                .id(postId)
                .likeCount(post.getLikeCount())
                .isLiked(isLiked)
                .build();
    }
}
