package com.gamercommunity.news.crawler;

import com.gamercommunity.news.entity.Platform;
import com.gamercommunity.global.exception.custom.NewsCollectionException;
import com.gamercommunity.news.entity.News;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 루리웹 RSS 피드 크롤러
 */
@Slf4j
@Component
public class RuliwebCrawler implements NewsCrawler {

    private static final int MAX_NEWS_COUNT = 15;
    private static final int TIMEOUT_MS = 15000;

    // RSS 피드 URL
    private static final Map<Platform, String> PLATFORM_RSS_MAP = Map.of(
        Platform.PLAYSTATION, "https://bbs.ruliweb.com/news/524/rss",
        Platform.XBOX, "https://bbs.ruliweb.com/news/526/rss",
        Platform.NINTENDO, "https://bbs.ruliweb.com/news/527/rss",
        Platform.PC, "https://bbs.ruliweb.com/news/529/rss"
    );

    @Override
    public List<News> crawl(Platform platform) {
        String rssUrl = PLATFORM_RSS_MAP.get(platform);
        if (rssUrl == null) {
            log.warn("루리웹: 지원하지 않는 플랫폼 - {}", platform);
            return Collections.emptyList();
        }

        try {
            log.info("루리웹 RSS 크롤링 시작: {} ({})", platform, rssUrl);

            // RSS XML 파싱
            Document doc = Jsoup.connect(rssUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(TIMEOUT_MS)
                    .parser(Parser.xmlParser())  // XML 파서 사용
                    .get();

            // RSS item 찾기
            Elements items = doc.select("item");

            if (items.isEmpty()) {
                log.warn("루리웹: RSS 항목을 찾을 수 없음 - {}", rssUrl);
                return Collections.emptyList();
            }

            log.info("루리웹: 발견된 RSS 항목 {}개", items.size());

            List<News> newsList = new ArrayList<>();
            for (Element item : items) {
                if (newsList.size() >= MAX_NEWS_COUNT) break;

                try {
                    News news = parseRssItem(item, platform);
                    if (news != null) {
                        newsList.add(news);
                        log.debug("루리웹: RSS 파싱 성공 - {} (이미지: {})", 
                                 news.getTitle().substring(0, Math.min(30, news.getTitle().length())), 
                                 news.getImageUrl() != null ? "있음" : "없음");
                    }
                } catch (Exception e) {
                    log.debug("루리웹: RSS 항목 파싱 실패 - {}", e.getMessage());
                }
            }

            log.info("루리웹 RSS 크롤링 완료: {} - {}개 수집", platform, newsList.size());
            return newsList;

        } catch (Exception e) {
            log.error("루리웹 RSS 크롤링 실패: {}", platform, e);
            throw new NewsCollectionException("루리웹 " + platform + " RSS 수집 실패", e);
        }
    }

    @Override
    public String getSourceName() {
        return "루리웹";
    }

    @Override
    public List<Platform> getSupportedPlatforms() {
        return List.of(Platform.PLAYSTATION, Platform.XBOX, Platform.NINTENDO, Platform.PC);
    }

    private News parseRssItem(Element item, Platform platform) {
        // 제목
        String title = getElementText(item, "title");
        if (title == null || title.isEmpty()) {
            return null;
        }

        // URL
        String newsUrl = getElementText(item, "link");
        if (newsUrl == null || newsUrl.isEmpty()) {
            return null;
        }

        // 광고, 공지 제외
        if (title.contains("[공지]") || title.contains("[이벤트]") ||
            title.contains("[AD]") || title.contains("광고")) {
            return null;
        }

        // 내용 (description)
        String content = getElementText(item, "description");
        if (content == null || content.isEmpty()) {
            content = title;
        } else {
            // HTML 태그 제거
            content = Jsoup.parse(content).text();
            if (content.length() > 500) {
                content = content.substring(0, 497) + "...";
            }
        }

        // 이미지 추출
        String imageUrl = extractImageFromRss(item);
        if (imageUrl != null && !imageUrl.isEmpty()) {
            log.info("루리웹 RSS: 이미지 URL = {}", imageUrl);  // 실제 URL 확인용
        } else {
            log.warn("루리웹 RSS: 이미지 없음 - 제목: {}", getElementText(item, "title"));
        }

        // 발행일
        LocalDateTime publishedAt = parsePublishedDate(item);

        return News.builder()
                .title(title)
                .content(content)
                .imageUrl(imageUrl)
                .platform(platform)
                .source(getSourceName())
                .publishedAt(publishedAt)
                .originalUrl(newsUrl)
                .build();
    }

    private String extractImageFromRss(Element item) {
        // description 안의 img 태그
        String description = getElementText(item, "description");
        if (description != null && description.contains("<img")) {
            Document descDoc = Jsoup.parse(description);
            Element img = descDoc.selectFirst("img");
            if (img != null) {
                String src = img.attr("src");
                if (src == null || src.isEmpty()) {
                    src = img.attr("data-src");
                }
                if (src == null || src.isEmpty()) {
                    src = img.attr("data-original");
                }
                
                if (src != null && !src.isEmpty()) {
                    // 상대 경로 처리
                    if (src.startsWith("//")) {
                        src = "https:" + src;  // //img.ruliweb.com → https://img.ruliweb.com
                    } else if (src.startsWith("/")) {
                        src = "https://bbs.ruliweb.com" + src;  // /img/... → https://bbs.ruliweb.com/img/...
                    }
                    // 이미 http:// 또는 https://로 시작하면 그대로 사용
                    
                    // 유효성 검사
                    if (!src.contains("blank") && !src.contains("default") && 
                        !src.contains("no_img") && !src.contains("placeholder")) {
                        log.debug("루리웹 RSS: description에서 이미지 추출 - {}", src);
                        return src;
                    }
                }
            }
        }

        //enclosure 태그 (RSS 표준)
        Element enclosure = item.selectFirst("enclosure[type^=image]");
        if (enclosure != null) {
            String url = enclosure.attr("url");
            if (url != null && !url.isEmpty()) {
                log.debug("루리웹 RSS: enclosure에서 이미지 추출 - {}", url);
                return url;
            }
        }

        //media:content 또는 media:thumbnail
        Element mediaThumbnail = item.selectFirst("media\\:thumbnail, thumbnail");
        if (mediaThumbnail != null) {
            String url = mediaThumbnail.attr("url");
            if (url != null && !url.isEmpty()) {
                log.debug("루리웹 RSS: media:thumbnail에서 이미지 추출 - {}", url);
                return url;
            }
        }

        Element mediaContent = item.selectFirst("media\\:content, content");
        if (mediaContent != null) {
            String url = mediaContent.attr("url");
            if (url != null && !url.isEmpty()) {
                log.debug("루리웹 RSS: media:content에서 이미지 추출 - {}", url);
                return url;
            }
        }

        log.debug("루리웹 RSS: 이미지를 찾을 수 없음");
        return null;
    }

    private LocalDateTime parsePublishedDate(Element item) {
        String pubDate = getElementText(item, "pubDate");
        if (pubDate != null && !pubDate.isEmpty()) {
            try {
                //RFC 822 형식 파싱 (예: Wed, 27 Dec 2025 10:30:00 +0900)
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(pubDate, 
                    DateTimeFormatter.RFC_1123_DATE_TIME);
                return zonedDateTime.toLocalDateTime();
            } catch (Exception e) {
                log.debug("루리웹: 날짜 파싱 실패 - {}", pubDate);
            }
        }
        return LocalDateTime.now();
    }

    private String getElementText(Element parent, String tagName) {
        Element element = parent.selectFirst(tagName);
        return element != null ? element.text() : null;
    }
}
