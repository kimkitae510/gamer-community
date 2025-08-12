package com.gamercommunity.news.entity;

import com.gamercommunity.global.time.Time;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class News extends Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Platform platform;

    @Column(length = 100)
    private String source;

    @Column(nullable = false)
    private LocalDateTime publishedAt;

    @Column(unique = true, length = 1000)
    private String originalUrl;

    @Builder
    public News(String title, String content, String imageUrl, Platform platform,
                String source, LocalDateTime publishedAt, String originalUrl) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.platform = platform;
        this.source = source;
        this.publishedAt = publishedAt;
        this.originalUrl = originalUrl;
    }
}
