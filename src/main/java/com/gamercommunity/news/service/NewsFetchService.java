package com.gamercommunity.news.service;

import com.gamercommunity.global.enums.Platform;
import com.gamercommunity.news.entity.News;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFetchService {

    private final NewsService newsService;

    // 플스 뉴스 수집
    public void fetchPlayStationNews() {
        try {
            String feedUrl = "https://blog.playstation.com/feed/";
            List<News> newsList = parseFeed(feedUrl, Platform.PLAYSTATION, "PlayStation Blog");
            newsList.forEach(news -> {
                try {
                    newsService.saveNews(news);
                } catch (Exception e) {
                    log.error("PlayStation 뉴스 저장 실패: {}", news.getTitle(), e);
                }
            });
            log.info("PlayStation 뉴스 수집 완료: {}개", newsList.size());
        } catch (Exception e) {
            log.error("PlayStation 뉴스 수집 실패", e);
        }
    }

    // 엑스박스 뉴스 수집
    public void fetchXboxNews() {
        try {
            String feedUrl = "https://news.xbox.com/en-us/feed/";
            List<News> newsList = parseFeed(feedUrl, Platform.XBOX, "Xbox Wire");
            newsList.forEach(news -> {
                try {
                    newsService.saveNews(news);
                } catch (Exception e) {
                    log.error("Xbox 뉴스 저장 실패: {}", news.getTitle(), e);
                }
            });
            log.info("Xbox 뉴스 수집 완료: {}개", newsList.size());
        } catch (Exception e) {
            log.error("Xbox 뉴스 수집 실패", e);
        }
    }

    // 닌텐도 뉴스 수집
    public void fetchNintendoNews() {
        try {
            String feedUrl = "https://www.nintendo.com/us/whatsnew/rss.xml";
            List<News> newsList = parseFeed(feedUrl, Platform.NINTENDO, "Nintendo News");
            newsList.forEach(news -> {
                try {
                    newsService.saveNews(news);
                } catch (Exception e) {
                    log.error("Nintendo 뉴스 저장 실패: {}", news.getTitle(), e);
                }
            });
            log.info("Nintendo 뉴스 수집 완료: {}개", newsList.size());
        } catch (Exception e) {
            log.error("Nintendo 뉴스 수집 실패", e);
        }
    }

    // 스팀/PC 뉴스 수집
    public void fetchSteamNews() {
        try {
            String feedUrl = "https://www.pcgamer.com/rss/";
            List<News> newsList = parseFeed(feedUrl, Platform.STEAM, "PC Gamer");
            newsList.forEach(news -> {
                try {
                    newsService.saveNews(news);
                } catch (Exception e) {
                    log.error("Steam 뉴스 저장 실패: {}", news.getTitle(), e);
                }
            });
            log.info("Steam/PC 뉴스 수집 완료: {}개", newsList.size());
        } catch (Exception e) {
            log.error("Steam 뉴스 수집 실패", e);
        }
    }

    // 피씨 게임 뉴스 수집
    public void fetchPCNews() {
        try {
            String feedUrl = "https://www.rockpapershotgun.com/feed";
            List<News> newsList = parseFeed(feedUrl, Platform.PC, "Rock Paper Shotgun");
            newsList.forEach(news -> {
                try {
                    newsService.saveNews(news);
                } catch (Exception e) {
                    log.error("PC 뉴스 저장 실패: {}", news.getTitle(), e);
                }
            });
            log.info("PC 게임 뉴스 수집 완료: {}개", newsList.size());
        } catch (Exception e) {
            log.error("PC 게임 뉴스 수집 실패", e);
        }
    }

    // RSS 피드 파싱
    private List<News> parseFeed(String feedUrl, Platform platform, String source) {
        List<News> newsList = new ArrayList<>();
        
        try {
            log.info("RSS 피드 파싱 시작: {}", feedUrl);
            
            URL url = new URL(feedUrl);
            SyndFeedInput input = new SyndFeedInput();
            

            try (XmlReader reader = new XmlReader(url)) {
                SyndFeed feed = input.build(reader);
                
                if (feed == null || feed.getEntries() == null) {
                    log.warn("피드가 비어있음: {}", feedUrl);
                    return newsList;
                }

                // 최신 10개
                int count = 0;
                for (SyndEntry entry : feed.getEntries()) {
                    if (count >= 10) break;

                    try {
                        String title = entry.getTitle();
                        if (title == null || title.trim().isEmpty()) {
                            log.warn("제목이 없는 뉴스 스킵");
                            continue;
                        }

                        String content = entry.getDescription() != null 
                            ? cleanHtmlTags(entry.getDescription().getValue())
                            : "";
                        

                        if (content.length() > 1000) {
                            content = content.substring(0, 997) + "...";
                        }
                        
                        String imageUrl = extractImageUrl(entry);
                        String originalUrl = entry.getLink();
                        
                        if (originalUrl == null || originalUrl.trim().isEmpty()) {
                            log.warn("URL이 없는 뉴스 스킵: {}", title);
                            continue;
                        }

                        LocalDateTime publishedAt = convertToLocalDateTime(entry.getPublishedDate());

                        News news = News.builder()
                                .title(title)
                                .content(content)
                                .imageUrl(imageUrl)
                                .platform(platform)
                                .source(source)
                                .publishedAt(publishedAt)
                                .originalUrl(originalUrl)
                                .build();

                        newsList.add(news);
                        count++;
                        
                    } catch (Exception e) {
                        log.error("뉴스 항목 파싱 실패", e);
                    }
                }
                
                log.info("RSS 피드 파싱 완료: {} - {}개 항목", feedUrl, newsList.size());
            }
            
        } catch (Exception e) {
            log.error("RSS 피드 파싱 실패: {}", feedUrl, e);
        }

        return newsList;
    }

    // HTML 태그 제거
    private String cleanHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }

    // 이미지 URL 추출
    private String extractImageUrl(SyndEntry entry) {
        try {
            // Enclosure에서 이미지 찾기
            if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
                String enclosureUrl = entry.getEnclosures().get(0).getUrl();
                if (enclosureUrl != null && !enclosureUrl.trim().isEmpty()) {
                    return enclosureUrl;
                }
            }
            
            // img 태그 찾기
            if (entry.getDescription() != null) {
                String description = entry.getDescription().getValue();
                if (description != null) {
                    int imgStart = description.indexOf("<img");
                    if (imgStart != -1) {
                        int srcStart = description.indexOf("src=\"", imgStart);
                        if (srcStart != -1) {
                            srcStart += 5;
                            int srcEnd = description.indexOf("\"", srcStart);
                            if (srcEnd != -1) {
                                return description.substring(srcStart, srcEnd);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("이미지 URL 추출 실패", e);
        }
        return null;
    }

    // Date -> LocalDateTime 변환
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return LocalDateTime.now();
        }
        try {
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            log.debug("날짜 변환 실패, 현재 시간 사용", e);
            return LocalDateTime.now();
        }
    }

    // 모든 플랫폼 뉴스 수집
    public void fetchAllNews() {
        log.info("=== 전체 게임 뉴스 수집 시작 ===");
        
        try {
            fetchPlayStationNews();
        } catch (Exception e) {
            log.error("PlayStation 뉴스 수집 중 예외 발생", e);
        }
        
        try {
            fetchXboxNews();
        } catch (Exception e) {
            log.error("Xbox 뉴스 수집 중 예외 발생", e);
        }
        
        try {
            fetchNintendoNews();
        } catch (Exception e) {
            log.error("Nintendo 뉴스 수집 중 예외 발생", e);
        }
        
        try {
            fetchSteamNews();
        } catch (Exception e) {
            log.error("Steam 뉴스 수집 중 예외 발생", e);
        }
        
        try {
            fetchPCNews();
        } catch (Exception e) {
            log.error("PC 뉴스 수집 중 예외 발생", e);
        }
        
        log.info("=== 전체 게임 뉴스 수집 완료 ===");
    }
}
