package com.gamercommunity.post.view.inMemoryCount;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;



@Component
@RequiredArgsConstructor
public class PostViewFlusher {

    private final JdbcTemplate jdbcTemplate;


    //메모리에 쌓인 조회수를 JDBC 배치로 DB에 flush
    @Transactional
    public void flush(Map<Long, AtomicInteger> viewCounts) {
        if (viewCounts == null || viewCounts.isEmpty()) {
            return;
        }

        String sql = "UPDATE post SET views = views + ? WHERE id = ?";

        List<Object[]> batchArgs = viewCounts.entrySet().stream()
                .map(e -> new Object[]{e.getValue().get(), e.getKey()})
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
