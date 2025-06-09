package com.gamercommunity.post.service;

import com.gamercommunity.post.dto.PostRequest;
import com.gamercommunity.post.dto.PostResponse;
import com.gamercommunity.post.entity.Post;
import com.gamercommunity.post.repository.PostRepository;
import com.gamercommunity.user.entity.User;
import com.gamercommunity.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (!post.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 게시글만 삭제할 수 있습니다.");
        }
        postRepository.delete(post);
    }

    // 게시글 수정
    @Transactional
    public PostResponse updatePost(Long postId, PostRequest postRequest, String loginId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        if (!post.getAuthor().getLoginId().equals(loginId)) {
            throw new AccessDeniedException("본인 게시글만 수정할 수 있습니다.");
        }

        post.update(postRequest.getTitle(), postRequest.getContent());

        return PostResponse.from(post);
    }
}
