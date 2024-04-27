package com.slowletter.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>  {
    boolean existsByLoginId(String loginId);
    boolean existsByNickname(String nickname);

    Optional<UserEntity> findByLoginId(String loginId);//query문 역할을 하는 함수 snake말고로 구분하는거라서 이름 짓는거 주의
}
