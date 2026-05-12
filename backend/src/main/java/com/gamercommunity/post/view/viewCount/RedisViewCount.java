package com.gamercommunity.post.view.viewCount;

import com.gamercommunity.post.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedisViewCount implements ViewCount {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "post:viewCount:";

    @Override
    public void increment(Long postId, Post post) {
        String key = KEY_PREFIX + postId;
        try {
            // 키가 없으면 DB 현재값으로 초기화 후 +1
            redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(post.getViews()));
            Long count = redisTemplate.opsForValue().increment(key);
            log.debug("[Redis] key={}, count={}", key, count);
        } catch (Exception e) {
            log.error("[Redis] 조회수 increment 실패 - key={}, error={}", key, e.getMessage(), e);
        }
    }
}
