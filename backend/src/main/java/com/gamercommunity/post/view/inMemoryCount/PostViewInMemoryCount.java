package com.gamercommunity.post.view.inMemoryCount;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;


@Component
public class PostViewInMemoryCount {

    private volatile Map<Long, LongAdder> viewCountMap = new ConcurrentHashMap<>();

    //조회수 증가
    public void increment(Long postId) {
        viewCountMap.computeIfAbsent(postId, k -> new LongAdder())
                    .incrementAndGet();
    }

    //스냅샷 분리
    public Map<Long, LongAdder> getAndReset() {
        Map<Long, LongAdder> snapshot = viewCountMap;
        viewCountMap = new ConcurrentHashMap<>();
        return snapshot;
    }

    //실패분 머지
    public void merge(Map<Long, LongAdder> failed) {
        failed.forEach((postId, count) ->
            viewCountMap.computeIfAbsent(postId, k -> new LongAdder())
                        .addAndGet(count.get())
        );
    }
}
