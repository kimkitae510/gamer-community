package com.gamercommunity.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.gamercommunity.user.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    // 로그인 ID 존재여부 확인
    boolean existsByLoginId(String loginId);

    // 닉네임 존재여부 확인
    boolean existsByNickname(String nickname);

    // 본인확인용 로그인 아이디 찾기
    Optional<User> findByLoginId(String loginId);

}
