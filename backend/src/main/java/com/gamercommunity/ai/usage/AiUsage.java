package com.gamercommunity.ai.usage;

import com.gamercommunity.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "ai_usage",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "usage_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AiUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    // 오늘 남은 사용 횟수
    @Column(nullable = false)
    private int remaining;

    // DB 기반 락 (true = 작업 진행 중, false = 유휴)
    @Column(nullable = false)
    @Builder.Default
    private boolean isLocked = false;

    // 마지막 요청 시각 (30초 타임아웃 판단용)
    @Column
    private LocalDateTime lastRequestAt;
}
