package com.gamercommunity.news.service;

import com.gamercommunity.news.entity.Platform;
import com.gamercommunity.global.exception.custom.NewsCollectionException;
import com.gamercommunity.news.crawler.NewsCrawler;
import com.gamercommunity.news.entity.News;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private final NewsService newsService;
    private final List<NewsCrawler> crawlers;

    //특정 플랫폼의 뉴스 수집
    public void fetchNewsByPlatform(Platform platform) {
        log.info("=== {} 뉴스 수집 시작 ===", platform);

        int totalCollected = 0;
        int totalSaved = 0;

        for (NewsCrawler crawler : crawlers) {
            // 크롤러가 해당 플랫폼을 지원하는지 확인
            if (!crawler.getSupportedPlatforms().contains(platform)) {
                continue;
            }

            try {
                log.info("{} 크롤러로 {} 뉴스 수집 중...", crawler.getSourceName(), platform);
                
                List<News> newsList = crawler.crawl(platform);
                totalCollected += newsList.size();

                int savedCount = 0;
                for (News news : newsList) {
                    try {
                        newsService.saveNews(news);
                        savedCount++;
                    } catch (Exception e) {
                        log.error("뉴스 저장 실패: {} - {}", news.getTitle(), e.getMessage());
                    }
                }

                totalSaved += savedCount;
                log.info("{} 크롤러: {}개 수집, {}개 저장", 
                         crawler.getSourceName(), newsList.size(), savedCount);

            } catch (NewsCollectionException e) {
                log.error("{} 크롤러 실패: {}", crawler.getSourceName(), e.getMessage());
            }
        }

        log.info("=== {} 뉴스 수집 완료 === (수집: {}개, 저장: {}개)", 
                 platform, totalCollected, totalSaved);
    }

    //모든 플랫폼의 뉴스 수집
    public void fetchAllNews() {
        log.info("=== 전체 게임 뉴스 수집 시작 ===");

        Platform[] platforms = Platform.values();
        int totalSuccess = 0;
        int totalFailed = 0;

        for (Platform platform : platforms) {
            try {
                fetchNewsByPlatform(platform);
                totalSuccess++;
            } catch (Exception e) {
                log.error("{} 뉴스 수집 실패: {}", platform, e.getMessage());
                totalFailed++;
            }
        }

        log.info("=== 전체 게임 뉴스 수집 완료 === (성공: {}, 실패: {})", 
                 totalSuccess, totalFailed);
    }
}
