package com.gamercommunity.news.service;

import com.gamercommunity.news.entity.News;
import com.gamercommunity.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    // 뉴스 저장
    @Transactional
    public void saveNews(News news) {
        if (news == null) {
            throw new IllegalArgumentException("뉴스 정보가 없습니다.");
        }
        
        if (news.getOriginalUrl() == null || news.getOriginalUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("뉴스 URL이 없습니다.");
        }

        if (!newsRepository.existsByOriginalUrl(news.getOriginalUrl())) {
            newsRepository.save(news);
            log.info("새 뉴스 저장: {} - {}", news.getPlatform(), news.getTitle());
        } else {
            log.debug("중복 뉴스 스킵: {}", news.getTitle());
        }
    }
}
