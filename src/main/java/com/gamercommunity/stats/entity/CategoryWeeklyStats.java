package com.gamercommunity.stats.entity;

import com.gamercommunity.category.entity.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "category_weekly_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "year", "week"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryWeeklyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer week;

    @Column(name = "post_count", nullable = false)
    private Long postCount;

}
