package com.gamercommunity.stats.entity;

import com.gamercommunity.category.entity.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "category_monthly_stats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "year_month"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryMonthlyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(name = "post_count", nullable = false)
    private Long postCount;

}
