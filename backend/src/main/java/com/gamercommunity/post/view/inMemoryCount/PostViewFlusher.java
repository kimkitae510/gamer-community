package com.gamercommunity.post.view.inMemoryCount;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewFlusher {

    private static final int BATCH_SIZE = 1000;
    private final JdbcTemplate jdbcTemplate;

    //조회수 벌크 flush (실패 시 1회 재시도, 재시도 실패분 반환)
    public Map<Long, LongAdder> flush(Map<Long, LongAdder> viewCounts) {
        Map<Long, LongAdder> failed = new HashMap<>();

        if (viewCounts == null || viewCounts.isEmpty()) {
            return failed;
        }

        List<Map.Entry<Long, LongAdder>> entries = new ArrayList<>(viewCounts.entrySet());

        for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entries.size());
            List<Map.Entry<Long, LongAdder>> chunk = entries.subList(i, end);
            int chunkNumber = (i / BATCH_SIZE) + 1;

            try {
                bulkUpdate(chunk);
                log.info("[ViewFlush] 청크 {} 성공 ({}건)", chunkNumber, chunk.size());
            } catch (Exception e) {
                log.warn("[ViewFlush] 청크 {} 실패, 재시도 ({}건): {}", chunkNumber, chunk.size(), e.getMessage());
                try {
                    bulkUpdate(chunk);
                    log.info("[ViewFlush] 청크 {} 재시도 성공 ({}건)", chunkNumber, chunk.size());
                } catch (Exception retryEx) {
                    log.error("[ViewFlush] 청크 {} 재시도 실패 ({}건): {}", chunkNumber, chunk.size(), retryEx.getMessage());
                    chunk.forEach(entry -> failed.put(entry.getKey(), entry.getValue()));
                }
            }
        }

        return failed;
    }

    //벌크 UPDATE
    private void bulkUpdate(List<Map.Entry<Long, LongAdder>> chunk) {
        StringBuilder sql = new StringBuilder("UPDATE post SET views = views + CASE id ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<Long, LongAdder> entry : chunk) {
            sql.append("WHEN ? THEN ? ");
            params.add(entry.getKey());
            params.add(entry.getValue().sum());
        }

        sql.append("END WHERE id IN (");

        for (int j = 0; j < chunk.size(); j++) {
            sql.append(j > 0 ? ",?" : "?");
            params.add(chunk.get(j).getKey());
        }

        sql.append(")");

        jdbcTemplate.update(sql.toString(), params.toArray());
    }
}
