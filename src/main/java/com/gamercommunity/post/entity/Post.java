package com.gamercommunity.post.entity;

import com.gamercommunity.gloabal.time.Time;
import com.gamercommunity.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int views = 0;

    @Builder
    public Post(User author, String title, String content) {
        this.author = author;
        this.title = title;
        this.content = content;
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

}
