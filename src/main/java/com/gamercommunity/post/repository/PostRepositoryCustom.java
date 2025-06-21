package com.gamercommunity.post.repository;

import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.entity.PostSort;
import com.gamercommunity.post.entity.Tag;

import java.util.List;

public interface PostRepositoryCustom {

    //카테고리별 게시글 목록 조회 (동적 정렬 + Tag 필터링)
    List<Post> findByCategoryWithFilters(Long categoryId, Tag tag, PostSort postSort);
}
