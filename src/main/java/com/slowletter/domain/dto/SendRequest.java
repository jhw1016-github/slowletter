package com.slowletter.domain.dto;


import com.slowletter.db.LetterEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class SendRequest {

    @NotBlank(message = "보내는이가 비어있습니다")
    private String sender;

    @NotBlank(message = "받는이가 비어있습니다")
    private String receiver;

    private String message;

    public LetterEntity toEntity(){
        return LetterEntity.builder()
                .sender(this.sender)
                .receiver(this.receiver)
                .message(this.message)
                .build();
    }
}
