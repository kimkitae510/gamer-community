package com.gamercommunity.popular.service;

import com.gamercommunity.popular.entity.PopularScore;
import com.gamercommunity.popular.repository.PopularScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PopularScoreService {

    private final PopularScoreRepository popularScoreRepository;


    //댓글 생성 시 호출 (+3점) -
    @Transactional
    public void onCommentCreated(Long postId) {
        popularScoreRepository.incrementCommentScoreAtomic(postId);
        log.info("댓글 생성: postId={}, +3점", postId);
    }


    //댓글 삭제 시 호출 (-3점)
    @Transactional
    public void onCommentDeleted(Long postId) {
        popularScoreRepository.decrementCommentScoreAtomic(postId);
        log.info("댓글 삭제: postId={}, -3점", postId);
    }

    //추천 생성 시 호출 (+5점)
    @Transactional
    public void onLikeCreated(Long postId) {
        popularScoreRepository.incrementLikeScoreAtomic(postId);
        log.info("추천 생성: postId={}, +5점", postId);
    }


    //추천 취소 시 호출 (-5점) - 원자적 업데이트
    @Transactional
    public void onLikeCancelled(Long postId) {
        popularScoreRepository.decrementLikeScoreAtomic(postId);
        log.info("추천 취소: postId={}, -5점", postId);
    }

    //특정 게시글의 인기점수 조회
    @Transactional(readOnly = true)
    public Integer getScore(Long postId) {
        return popularScoreRepository.findByPostId(postId)
                .map(PopularScore::getScore)
                .orElse(0);
    }
}
