package com.gamercommunity.postLike.service;

import com.gamercommunity.global.exception.custom.EntityNotFoundException;
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


    //게시글 좋아요 토글(추가/취소)
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
            liked = false;
        } else {
            // 좋아요 추가
            try {
                postLikeRepository.save(PostLike.builder()
                        .post(post)
                        .user(user)
                        .build());
                postRepository.incrementLikeCount(postId);
                liked = true;
            } catch (DataIntegrityViolationException e) {
                // 유니크 제약조건 위반 (동시성 이슈 대응)
                throw new IllegalStateException("이미 좋아요를 눌렀습니다.");
            }
        }


        Integer likeCount = postRepository.getLikeCount(postId);

        return PostLikeResponse.builder()
                .id(postId)
                .likeCount(likeCount != null ? likeCount : 0)
                .liked(liked)
                .build();
    }


}
