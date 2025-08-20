package com.gamercommunity.post.view.viewCount;

import com.gamercommunity.post.entity.Post;

public interface ViewCount {
    void increment(Long postId, Post post);
}
