package com.gamercommunity.reviewLike.entity;

import com.gamercommunity.review.entity.Review;
import com.gamercommunity.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "review_like", uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_like_user_review", columnNames = {"user_id", "review_id"})
})
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Review review;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

}

