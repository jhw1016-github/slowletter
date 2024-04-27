package com.slowletter.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LetterRepository extends JpaRepository<LetterEntity,Long> {
    //Optional<LetterEntity> findAllById();//보낸 편지함을 확인하기위해 해당 유저의 id에 존재하는 편지 전부 찾기

    //Optional<UserEntity> findByLoginId(String loginId);//query문 역할을 하는 함수 snake말고로 구분하는거라서 이름 짓는거 주의



    Optional<LetterEntity> findByReceiver(String receiver);//받은이의 이름을 받아서 받은이의 모든 편지를 찾는 쿼리
    Optional<LetterEntity> findBySender(String sender);//보낸이의 이름을 받아서 보낸 모든 편지를 찾는 과정

    //Optional<LetterEntity> findBySender

}
