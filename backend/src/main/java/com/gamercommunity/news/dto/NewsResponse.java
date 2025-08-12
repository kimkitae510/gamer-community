package com.gamercommunity.news.dto;

import com.gamercommunity.news.entity.Platform;
import com.gamercommunity.news.entity.News;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NewsResponse {

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private Platform platform;
    private String source;
    private LocalDateTime publishedAt;
    private String originalUrl;

    public static NewsResponse from(News news) {
        return NewsResponse.builder()
                .id(news.getId())
                .title(news.getTitle())
                .content(news.getContent())
                .imageUrl(news.getImageUrl())
                .platform(news.getPlatform())
                .source(news.getSource())
                .publishedAt(news.getPublishedAt())
                .originalUrl(news.getOriginalUrl())
                .build();
    }
}
