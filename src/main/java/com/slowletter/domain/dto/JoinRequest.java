package com.slowletter.domain.dto;

import com.slowletter.db.UserEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class JoinRequest {

    @NotBlank(message = "로그인 아이디가 비어있습니다.")
    private String loginId;

    @NotBlank(message = "비밀번호가 비어있습니다.")
    private String password;
    private String passwordCheck;

    @NotBlank(message = "닉네임이 비어있습니다.")
    private String nickname;

    private String country;

    private String interest;

    public UserEntity toEntity() {
        return UserEntity.builder()
                .loginId(this.loginId)
                .password(this.password)
                .nickname(this.nickname)
                .country(this.country)
                .interest(this.interest)
                .build();
    }


}