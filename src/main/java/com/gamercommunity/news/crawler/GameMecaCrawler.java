package com.gamercommunity.news.crawler;

import com.gamercommunity.news.entity.Platform;
import com.gamercommunity.global.exception.custom.NewsCollectionException;
import com.gamercommunity.news.entity.News;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Component
public class GameMecaCrawler implements NewsCrawler {

    private static final int MAX_NEWS_COUNT = 15;
    private static final int TIMEOUT_MS = 15000;

    @Override
    public List<News> crawl(Platform platform) {
        String url;
        
        if (platform == Platform.PC) {
            url = "https://www.gamemeca.com/news.php?ca=W";
        } else if (platform == Platform.MOBILE) {
            url = "https://www.gamemeca.com/news.php?ca=M";
        } else {
            log.warn("게임메카: 지원하지 않는 플랫폼 - {}", platform);
            return Collections.emptyList();
        }

        try {
            log.info("게임메카 크롤링 시작: {} ({})", platform, url);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(TIMEOUT_MS)
                    .get();

            Elements newsItems = doc.select("li");

            if (newsItems.isEmpty()) {
                log.warn("게임메카: 뉴스 항목을 찾을 수 없음 - {}", url);
                return Collections.emptyList();
            }

            log.info("게임메카: 발견된 뉴스 항목 {}개", newsItems.size());

            List<News> newsList = new ArrayList<>();
            for (Element item : newsItems) {
                if (newsList.size() >= MAX_NEWS_COUNT) break;

                try {
                    News news = parseNewsItem(item, platform);
                    if (news != null) {
                        newsList.add(news);
                        log.debug("게임메카: 뉴스 파싱 성공 - {} (이미지: {})", 
                                 news.getTitle().substring(0, Math.min(30, news.getTitle().length())), 
                                 news.getImageUrl() != null ? "있음" : "없음");
                    }
                } catch (Exception e) {
                    log.debug("게임메카: 뉴스 항목 파싱 실패 - {}", e.getMessage());
                }
            }

            log.info("게임메카 크롤링 완료: {} - {}개 수집", platform, newsList.size());
            return newsList;

        } catch (Exception e) {
            log.error("게임메카 크롤링 실패: {}", platform, e);
            throw new NewsCollectionException("게임메카 " + platform + " 뉴스 수집 실패", e);
        }
    }

    @Override
    public String getSourceName() {
        return "게임메카";
    }

    @Override
    public List<Platform> getSupportedPlatforms() {
        return List.of(Platform.PC, Platform.MOBILE);
    }

    private News parseNewsItem(Element item, Platform platform) {
        // a 태그 찾기
        Element linkElem = item.selectFirst("a[href*='view.php']");
        if (linkElem == null) {
            return null;
        }

        String newsUrl = linkElem.attr("abs:href");
        if (newsUrl.isEmpty()) {
            return null;
        }

        // 제목 찾기
        Element titleElem = item.selectFirst("strong");
        if (titleElem == null) {
            return null;
        }

        String title = titleElem.text().trim();
        if (title.isEmpty()) {
            return null;
        }

        // 광고, 공지 제외
        if (title.contains("[공지]") || title.contains("[이벤트]") ||
            title.contains("[AD]") || title.contains("광고")) {
            return null;
        }

        // 이미지 찾기
        Element imgElem = item.selectFirst("img[src*='resize_gm']");
        String imageUrl = null;
        
        if (imgElem != null) {
            imageUrl = imgElem.attr("abs:src");
            
            // 빈 이미지 URL 처리
            if (imageUrl != null && (imageUrl.trim().isEmpty() ||
                imageUrl.contains("blank") || imageUrl.contains("default"))) {
                imageUrl = null;
            } else if (imageUrl != null && !imageUrl.isEmpty()) {
                log.debug("게임메카: 이미지 발견 - {}", imageUrl);
            }
        }

        // 내용 추출
        String content = item.text().trim();
        if (content.startsWith(title)) {
            content = content.substring(title.length()).trim();
        }
        
        if (content.isEmpty()) {
            content = title;
        }

        if (content.length() > 1000) {
            content = content.substring(0, 997) + "...";
        }

        return News.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .platform(platform)
                .source(getSourceName())
                .publishedAt(LocalDateTime.now())
                .originalUrl(newsUrl)
                .build();
    }
}
