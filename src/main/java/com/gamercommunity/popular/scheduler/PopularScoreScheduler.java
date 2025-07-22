package com.gamercommunity.popular.scheduler;

import com.gamercommunity.popular.repository.PopularScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularScoreScheduler {

    private final PopularScoreRepository popularScoreRepository;

    //5분간격 실시간 인기글 플래그 업데이트
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void updateTrendingFlags() {
        log.info("실시간 인기글 플래그 업데이트 시작");

        try {
            int count = popularScoreRepository.updateTrendingToTrue();
            log.info("실시간 인기글 플래그 업데이트 완료: {}건", count);
        } catch (Exception e) {
            log.error("실시간 인기글 플래그 업데이트 실패", e);
        }
    }
}
