package com.gamercommunity.stats.entity;

import com.gamercommunity.category.entity.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
    name = "category_daily_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "date"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "post_count", nullable = false)
    private Long postCount;

}
