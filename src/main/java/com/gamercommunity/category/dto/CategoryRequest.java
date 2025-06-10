package com.gamercommunity.category.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequest {

    private Long id;
    private String name;
    private Long parentId;
    private boolean writable;

}
