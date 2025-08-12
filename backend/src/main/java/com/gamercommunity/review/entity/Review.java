package com.gamercommunity.review.entity;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.global.enums.ContentStatus;
import com.gamercommunity.global.time.Time;
import com.gamercommunity.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review")
public class Review extends Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Review parent;

    @OneToMany(mappedBy = "parent")
    private List<Review> children = new ArrayList<>();

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = true)
    private Integer rating; // 1~5

    private int likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status;

    @Builder
    public Review(Category game, User author, String content, Integer rating, Review parent) {
        if (parent == null && rating != null) {
            validateRating(rating);
        }
        this.game = game;
        this.author = author;
        this.content = content;
        this.rating = rating;
        this.parent = parent;
        this.likeCount = 0;
        this.status = ContentStatus.ACTIVE;
    }

    // 검증 로직
    private void validateRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
        }
    }

    // 리뷰 수정
    public void update(String newContent, Integer newRating) {
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }

        if (this.parent == null) {
            validateRating(newRating);
        }

        boolean changed = false;

        if (!Objects.equals(this.content, newContent)) {
            this.content = newContent;
            changed = true;
        }

        if (this.parent == null && !Objects.equals(this.rating, newRating)) {
            this.rating = newRating;
            changed = true;
        }

        if (changed) {
            updateTimestamp();
        }
    }

    // 대댓글 내용만 수정
    public void updateContent(String newContent) {
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }

        boolean changed = !Objects.equals(this.content, newContent);

        if (changed) {
            this.content = newContent;
            updateTimestamp();
        }
    }

    // 자식 리뷰 추가
    public void addChild(Review child) {
        this.children.add(child);
        child.parent = this;
    }

    // 원본 데이터 유지 status만 변경
    public void softDelete() {
        this.status = ContentStatus.DELETED;
    }

}

