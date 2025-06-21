package com.gamercommunity.post.dto;

import com.gamercommunity.post.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    private String title;
    private String content;
    private Long categoryId;
    private Tag tag;

}

