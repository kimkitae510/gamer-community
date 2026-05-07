package com.gamercommunity.post.view.inMemoryCount;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@Component
public class PostViewInMemoryCount {

    private volatile Map<Long, AtomicInteger> viewCountMap = new ConcurrentHashMap<>();

    //조회수 증가
    public void increment(Long postId) {
        viewCountMap.computeIfAbsent(postId, k -> new AtomicInteger())
                    .incrementAndGet();
    }

    //스냅샷 분리
    public Map<Long, AtomicInteger> getAndReset() {
        Map<Long, AtomicInteger> snapshot = viewCountMap;
        viewCountMap = new ConcurrentHashMap<>();
        return snapshot;
    }

    //실패분 머지
    public void merge(Map<Long, AtomicInteger> failed) {
        failed.forEach((postId, count) ->
            viewCountMap.computeIfAbsent(postId, k -> new AtomicInteger())
                        .addAndGet(count.get())
        );
    }
}
