package com.gamercommunity.category.repository;

import com.gamercommunity.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    //부모가 없는 카테고리 찾기 = 부모카테고리 찾기
    List<Category> findByParentIsNull();

}


