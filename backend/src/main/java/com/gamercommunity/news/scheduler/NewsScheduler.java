package com.gamercommunity.news.scheduler;

import com.gamercommunity.news.service.NewsFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final NewsFetchService newsFetchService;

    // 매일 오전 6시에 뉴스 수집
    @Scheduled(cron = "0 0 6 * * *")
    public void scheduleDailyNewsFetch() {
        log.info("일일 뉴스 수집 시작");
        newsFetchService.fetchAllNews();
    }

}
