package com.gamercommunity.news.controller;

import com.gamercommunity.news.entity.Platform;
import com.gamercommunity.news.dto.NewsResponse;
import com.gamercommunity.news.service.NewsFetchService;
import com.gamercommunity.news.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    private final NewsFetchService newsFetchService;

    // 플랫폼별 뉴스 조회
    @GetMapping("/platform/{platform}")
    public ResponseEntity<List<NewsResponse>> getNewsByPlatform(
            @PathVariable Platform platform) {
        return ResponseEntity.ok(newsService.getNewsByPlatform(platform));
    }

    // 전체 뉴스 조회
    @GetMapping
    public ResponseEntity<List<NewsResponse>> getAllNews() {
        return ResponseEntity.ok(newsService.getAllNews());
    }

    // 수동 뉴스 수집
    @PostMapping("/fetch")
    public ResponseEntity<String> fetchNews() {
        newsFetchService.fetchAllNews();
        return ResponseEntity.ok("뉴스 수집 완료");
    }
}
