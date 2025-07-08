package com.gamercommunity.review.entity;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.common.enums.ContentStatus;
import com.gamercommunity.global.time.Time;
import com.gamercommunity.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private int rating; // 1~5

    private int likeCount;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private ContentStatus status;

    @Builder
    public Review(Category game, User author, String content, int rating) {
        validateRating(rating);
        this.game = game;
        this.author = author;
        this.content = content;
        this.rating = rating;
        this.likeCount = 0;
        this.status = ContentStatus.ACTIVE;
    }

    // 검증 로직
    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
        }
    }

    // 리뷰 수정
    public void update(String newContent, int newRating) {
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("리뷰 내용은 필수입니다.");
        }
        validateRating(newRating);

        boolean changed = false;

        if (!Objects.equals(this.content, newContent)) {
            this.content = newContent;
            changed = true;
        }

        if (this.rating != newRating) {
            this.rating = newRating;
            changed = true;
        }

        if (changed) {
            updateTimestamp();
        }
    }

    // 소프트 삭제 (원본 데이터 유지, status만 변경)
    public void softDelete() {
        this.status = ContentStatus.DELETED;
    }

}

