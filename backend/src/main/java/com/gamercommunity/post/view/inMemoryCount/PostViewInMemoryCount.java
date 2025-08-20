package com.gamercommunity.post.view.inMemoryCount;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class PostViewInMemoryCount {

    private Map<Long, AtomicInteger> viewCountMap = new ConcurrentHashMap<>();


    //게시글 조회수 인메모리 저장
    public void increment(Long postId) {
        viewCountMap.computeIfAbsent(postId, k -> new AtomicInteger())
                    .incrementAndGet();
    }

    //기존 맵을 스냅샷으로 분리하고 새 맵으로 교체
    public Map<Long, AtomicInteger> getAndReset() {
        Map<Long, AtomicInteger> snapshot = viewCountMap;
        viewCountMap = new ConcurrentHashMap<>();
        return snapshot;
    }
}
