package com.gamercommunity.post.view;

import com.gamercommunity.post.view.inMemoryCount.PostViewFlusher;
import com.gamercommunity.post.view.inMemoryCount.PostViewInMemoryCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;



@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewScheduler {

    private final PostViewInMemoryCount postViewInMemoryCount;
    private final PostViewFlusher postViewFlusher;



    //3초마다 메모리 조회수 스냅샷을 DB에 flush
    @Scheduled(fixedDelay = 3000)
    public void flushViewCounts() {
        Map<Long, AtomicInteger> snapshot = postViewInMemoryCount.getAndReset();

        if (snapshot.isEmpty()) {
            log.debug("[ViewFlush] 업데이트할 조회수 없음, 건너뜀");
            return;
        }

        log.info("[ViewFlush] {}건 조회수 DB 반영 시작", snapshot.size());
        postViewFlusher.flush(snapshot);
        log.info("[ViewFlush] DB 반영 완료");
    }
}
