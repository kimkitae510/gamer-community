package com.gamercommunity.category.entity;

import com.gamercommunity.genre.entity.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "board_category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean writable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Category> children = new ArrayList<>();

    private String imageUrl;

    //반정규화 컬럼
    private Double rating = 0.0;
    private Long reviewCount = 0L;
    private Long postCount = 0L;


    @CreatedDate
    private LocalDateTime createdAt;


    @Builder
    public Category(String name, boolean writable, Category parent, String imageUrl) {
        this.name = name;
        this.writable = writable;
        this.imageUrl = imageUrl;
        this.parent = parent;
        this.rating = 0.0;
        this.reviewCount = 0L;
        this.postCount = 0L;
    }

    // 이미지 URL 변경
    public void changeImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // 이름 수정
    public void updateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("카테고리 이름은 필수입니다.");
        }
        this.name = name;
    }

}
