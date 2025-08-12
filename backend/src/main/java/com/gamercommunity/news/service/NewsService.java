package com.gamercommunity.news.service;

import com.gamercommunity.news.dto.NewsResponse;
import com.gamercommunity.news.entity.News;
import com.gamercommunity.news.entity.Platform;
import com.gamercommunity.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    //뉴스 저장
    @Transactional
    public void saveNews(News news) {
        validateNews(news);

        if (newsRepository.existsByOriginalUrl(news.getOriginalUrl())) {
            log.debug("중복 뉴스 스킵: {}", news.getTitle());
            return;
        }

        newsRepository.save(news);
        log.info("새 뉴스 저장: {} - {}", news.getPlatform(), news.getTitle());
    }

    //뉴스 유효성 검증
    private void validateNews(News news) {
        if (news == null) {
            throw new IllegalArgumentException("뉴스 정보가 없습니다.");
        }

        if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("뉴스 제목이 없습니다.");
        }

        if (news.getOriginalUrl() == null || news.getOriginalUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("뉴스 URL이 없습니다.");
        }

        if (news.getPlatform() == null) {
            throw new IllegalArgumentException("뉴스 플랫폼이 없습니다.");
        }
    }

    //플랫폼별 뉴스 조회
    @Transactional(readOnly = true)
    public List<NewsResponse> getNewsByPlatform(Platform platform) {
        if (platform == null) {
            throw new IllegalArgumentException("플랫폼을 지정해주세요.");
        }

        return newsRepository.findByPlatformOrderByPublishedAtDesc(platform)
                .stream()
                .map(NewsResponse::from)
                .collect(Collectors.toList());
    }

    //전체 뉴스 조회
    @Transactional(readOnly = true)
    public List<NewsResponse> getAllNews() {
        return newsRepository.findAllByOrderByPublishedAtDesc()
                .stream()
                .map(NewsResponse::from)
                .collect(Collectors.toList());
    }
}
