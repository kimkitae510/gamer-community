package com.gamercommunity.commentLike.entity;

import com.gamercommunity.comment.entity.Comment;
import com.gamercommunity.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment_like", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_like_user_comment", columnNames = {"user_id", "comment_id"})
})
public class CommentLike
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    Comment comment;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

}
