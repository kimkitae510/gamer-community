package com.gamercommunity.popular.entity;

import com.gamercommunity.global.time.Time;
import com.gamercommunity.post.entity.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "popular_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularScore extends Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private Post post;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer commentScore; // 댓글로 얻은 점수

    @Column(nullable = false)
    private Integer likeScore; // 추천으로 얻은 점수

    @Builder
    public PopularScore(Post post, Integer score, Integer commentScore, Integer likeScore) {
        this.post = post;
        this.score = score != null ? score : 0;
        this.commentScore = commentScore != null ? commentScore : 0;
        this.likeScore = likeScore != null ? likeScore : 0;
    }
}
