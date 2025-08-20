package com.gamercommunity.post.view.viewCount;

import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.view.inMemoryCount.PostViewInMemoryCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

//@Primary
@Component
@RequiredArgsConstructor
public class MemoryViewCount implements ViewCount {

    private final PostViewInMemoryCount postViewInMemoryCount;

    @Override
    public void increment(Long postId, Post post) {
        postViewInMemoryCount.increment(postId);
    }
}
