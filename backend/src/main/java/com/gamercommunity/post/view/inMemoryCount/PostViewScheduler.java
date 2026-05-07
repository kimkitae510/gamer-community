package com.gamercommunity.post.view;

import com.gamercommunity.post.view.inMemoryCount.PostViewFlusher;
import com.gamercommunity.post.view.inMemoryCount.PostViewInMemoryCount;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewScheduler {

    private static final int SHUTDOWN_RETRY_COUNT = 2;

    private final PostViewInMemoryCount postViewInMemoryCount;
    private final PostViewFlusher postViewFlusher;
    private final ReentrantLock flushLock = new ReentrantLock();
    private volatile boolean destroyed = false;

    //주기적 flush
    @Scheduled(fixedDelay = 3000)
    public void flushViewCounts() {
        if (destroyed) {
            return;
        }

        flushLock.lock();
        try {
            doFlush(false);
        } finally {
            flushLock.unlock();
        }
    }

    //서버 종료 시 최종 flush
    @PreDestroy
    public void onShutdown() {
        destroyed = true;

        flushLock.lock();
        try {
            log.info("[ViewFlush] 서버 종료 감지 - 최종 flush 시작");

            Map<Long, AtomicInteger> failed = doFlush(true);

            for (int attempt = 1; attempt <= SHUTDOWN_RETRY_COUNT && !failed.isEmpty(); attempt++) {
                log.warn("[ViewFlush] 종료 flush 재시도 {}/{} ({}건)", attempt, SHUTDOWN_RETRY_COUNT, failed.size());
                failed = postViewFlusher.flush(failed);
            }

            if (!failed.isEmpty()) {
                log.error("[ViewFlush] 최종 flush 실패 {}건 - 데이터 유실", failed.size());
            } else {
                log.info("[ViewFlush] 종료 flush 완료 - 유실 없음");
            }
        } finally {
            flushLock.unlock();
        }
    }

    private Map<Long, AtomicInteger> doFlush(boolean isShutdown) {
        Map<Long, AtomicInteger> snapshot = postViewInMemoryCount.getAndReset();

        if (snapshot.isEmpty()) {
            log.debug("[ViewFlush] 업데이트할 조회수 없음, 건너뜀");
            return Map.of();
        }

        log.info("[ViewFlush] {}건 조회수 DB 반영 시작", snapshot.size());

        Map<Long, AtomicInteger> failed = postViewFlusher.flush(snapshot);

        if (!failed.isEmpty() && !isShutdown) {
            log.warn("[ViewFlush] 실패 {}건 현재 맵에 머지", failed.size());
            postViewInMemoryCount.merge(failed);
        }

        log.info("[ViewFlush] DB 반영 완료 (성공: {}건, 실패: {}건)",
                snapshot.size() - failed.size(), failed.size());

        return failed;
    }
}
