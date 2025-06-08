package com.gamercommunity.post.service;

import com.gamercommunity.post.dto.PostRequest;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class PostService {

    private  final UserRepository userRepository;
    private  final PostRepository postRepository;


    // 게시글 작성
    @Transactional
    public long createPost(PostRequest postRequest, String loginId) {
        User author = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Post post = Post.builder()
                .author(author)
                .title(postRequest.getTitle())
                .content(postRequest.getContent())
                .build();

        postRepository.save(post);

        return post.getId();
    }
}
