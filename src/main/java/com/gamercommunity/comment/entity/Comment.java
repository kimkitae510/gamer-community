package com.gamercommunity.comment.entity;

import com.gamercommunity.common.enums.ContentStatus;
import com.gamercommunity.global.time.Time;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "comment")
public class Comment extends Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = true, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> children = new ArrayList<>();

    private int likeCount;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private ContentStatus status;

    @Builder
    public Comment(String content, Post post, User author, Comment parent) {
        this.content = content;
        this.post = post;
        this.author = author;
        this.parent = parent;
        this.likeCount = 0;
        this.status = ContentStatus.ACTIVE;
    }

    //자식 댓글 추가
    public void addChild(Comment child) {
        this.children.add(child);
        child.parent = this;
    }

    // 댓글 내용 수정
    public void updateContent(String newContent) {
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("댓글 내용은 필수입니다.");
        }

        boolean changed = !Objects.equals(this.content, newContent);

        if (changed) {
            this.content = newContent;
            updateTimestamp();
        }
    }

    // 소프트 삭제 (원본 데이터 유지, status만 변경)
    public void softDelete() {
        this.status = ContentStatus.DELETED;
    }
}

