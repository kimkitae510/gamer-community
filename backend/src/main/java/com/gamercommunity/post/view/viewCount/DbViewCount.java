package com.gamercommunity.post.view.viewCount;

import com.gamercommunity.popular.service.PopularScoreService;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DbViewCount implements ViewCount {

    private final PostRepository postRepository;
    private final PopularScoreService popularScoreService;

    @Override
    public void increment(Long postId, Post post) {
        postRepository.incrementViewCount(postId);
        //popularScoreService.onPostViewed(postId);
    }
}
