package com.gamercommunity.popular.service;

import com.gamercommunity.global.exception.custom.EntityNotFoundException;
import com.gamercommunity.popular.entity.PopularScore;
import com.gamercommunity.popular.repository.PopularScoreRepository;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularScoreService {

    private final PopularScoreRepository popularScoreRepository;
    private final PostRepository postRepository;


    //댓글 생성
    @Transactional
    public void onCommentCreated(Long postId) {
        int updated = popularScoreRepository.incrementCommentScoreAtomic(postId);
        
        // PopularScore가 없으면 생성 후 다시 시도
        if (updated == 0) {
            createPopularScoreIfNotExists(postId);
            popularScoreRepository.incrementCommentScoreAtomic(postId);
        }
        
        log.info("댓글 생성: postId={}, +3점", postId);
    }

    //댓글 삭제
    @Transactional
    public void onCommentDeleted(Long postId) {
        int updated = popularScoreRepository.decrementCommentScoreAtomic(postId);
        
        // PopularScore가 없으면 생성 후 다시 시도
        if (updated == 0) {
            createPopularScoreIfNotExists(postId);
            popularScoreRepository.decrementCommentScoreAtomic(postId);
        }
        
        log.info("댓글 삭제: postId={}, -3점", postId);
    }

    //추천
    @Transactional
    public void onLikeCreated(Long postId) {
        int updated = popularScoreRepository.incrementLikeScoreAtomic(postId);
        
        // PopularScore가 없으면 생성 후 다시 시도
        if (updated == 0) {
            createPopularScoreIfNotExists(postId);
            popularScoreRepository.incrementLikeScoreAtomic(postId);
        }
        
        log.info("추천 생성: postId={}, +5점", postId);
    }

    //추천 취소
    @Transactional
    public void onLikeCancelled(Long postId) {
        int updated = popularScoreRepository.decrementLikeScoreAtomic(postId);
        
        // PopularScore가 없으면 생성 후 다시 시도
        if (updated == 0) {
            createPopularScoreIfNotExists(postId);
            popularScoreRepository.decrementLikeScoreAtomic(postId);
        }
        
        log.info("추천 취소: postId={}, -5점", postId);
    }

    //PopularScore가 없으면 생성
    private void createPopularScoreIfNotExists(Long postId) {
        // 이미 존재하는지 다시 확인
        if (popularScoreRepository.findByPostId(postId).isPresent()) {
            return;
        }
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글", postId));

        PopularScore popularScore = PopularScore.builder()
                .post(post)
                .score(0)
                .commentScore(0)
                .likeScore(0)
                .build();

        try {
            popularScoreRepository.save(popularScore);
            log.info("PopularScore 생성: postId={}", postId);
        } catch (Exception e) {
            log.debug("PopularScore 이미 존재: postId={}", postId);
        }
    }

    //특정 게시글의 인기점수 조회
    @Transactional(readOnly = true)
    public Integer getScore(Long postId) {
        return popularScoreRepository.findByPostId(postId)
                .map(PopularScore::getScore)
                .orElse(0);
    }
}
