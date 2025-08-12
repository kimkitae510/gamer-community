package com.gamercommunity.post.entity;

import com.gamercommunity.category.entity.Category;
import com.gamercommunity.global.enums.ContentStatus;
import com.gamercommunity.global.time.Time;
import com.gamercommunity.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "author_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User author;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "category_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category category;


    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    //반정규화 컬럼
    private int views;
    private int likeCount;
    private int commentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tag tag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status;

    @Builder
    public Post(User author, String title, String content, Category category, Tag tag,
                int views, int likeCount, int commentCount) {
        this.author = author;
        this.category = category;
        this.title = title;
        this.content = content;
        this.tag = tag;
        this.views = views;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.status = ContentStatus.ACTIVE;
    }

    // 게시글 수정 메서드
    public void update(String newTitle, String newContent) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("제목은 필수입니다.");
        }
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("내용은 필수입니다.");
        }

        boolean changed = false;

        if (!Objects.equals(this.title, newTitle)) {
            this.title = newTitle;
            changed = true;
        }

        if (!Objects.equals(this.content, newContent)) {
            this.content = newContent;
            changed = true;
        }

        if (changed) {
            updateTimestamp();  // BaseEntity의 메서드
        }
    }

    // 게시글 소프트 삭제
    public void softDelete() {
        this.status = ContentStatus.DELETED;
    }

}
