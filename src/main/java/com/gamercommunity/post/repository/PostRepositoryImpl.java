package com.gamercommunity.post.repository;

import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.entity.PostSort;
import com.gamercommunity.post.entity.Tag;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gamercommunity.post.entity.QPost.post;
import static com.gamercommunity.user.entity.QUser.user;
import static com.gamercommunity.category.entity.QCategory.category;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> findByCategoryWithFilters(Long categoryId, Tag tag, PostSort postSort) {
        return queryFactory
                .selectFrom(post)
                .join(post.author, user).fetchJoin()
                .join(post.category, category).fetchJoin()
                .where(
                        categoryIdEq(categoryId),
                        tagEq(tag),
                        post.status.eq(com.gamercommunity.global.enums.ContentStatus.ACTIVE)
                )
                .orderBy(getOrderSpecifier(postSort))
                .distinct()
                .fetch();
    }

    // 동적 쿼리 조건
    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? post.category.id.eq(categoryId) : null;
    }

    private BooleanExpression tagEq(Tag tag) {
        return tag != null ? post.tag.eq(tag) : null;
    }

    // 정렬 조건
    private OrderSpecifier<?> getOrderSpecifier(PostSort postSort) {
        if (postSort == null) {
            return post.createdAt.desc();
        }

        return switch (postSort) {
            case LATEST -> post.createdAt.desc();
            case OLDEST -> post.createdAt.asc();
            case VIEWED -> post.views.desc().nullsLast();
            case LIKED -> post.likeCount.desc().nullsLast();
            case COMMENTED -> post.commentCount.desc().nullsLast();

        };
    }
}
