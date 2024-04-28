package com.slowletter.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity(name="logintestuser")//오브젝트와 user라는 table에 있는 컬럼들을 매칭시키기 위한 어노테이션이다.
//url: jdbc:mysql://localhost:3306/slowletter?useSSL=false&useUnicode=true&allowPublicKeyRetrieval=true
//resources의 application.yml에서 user라는 db에 연결시킨거다. db는 workbench에서 확인가능하다.

public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //얘가 생성되는 방식은 identity하게 생성되게하겠다라는 느낌
    private Long id; //id는 primary key로 동작하기 때문에 @id라는 어노테이션을 달아준다.
    private String loginId;
    private String password;
    private String nickname;

    private String country;
    private String interest;

}

