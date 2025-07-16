package com.gamercommunity.news.repository;

import com.gamercommunity.news.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // 중복 체크
    boolean existsByOriginalUrl(String originalUrl);

}
