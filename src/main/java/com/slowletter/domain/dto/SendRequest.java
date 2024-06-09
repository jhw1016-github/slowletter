package com.slowletter.domain.dto;


import com.slowletter.db.LetterEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class SendRequest {

    private String sender;

    private String receiver;

    private String interest;

    private String sendLocation;
    private String receiveLocation;
    private String message;
    private int delayTime;
    private Date receiveDate;
    private Date sendDate;

    private int alarm;
    public LetterEntity toEntity(){
        return LetterEntity.builder()
                .sender(this.sender)
                .receiver(this.receiver)
                .message(this.message)
                .alarm(this.alarm)
                .delayTime(this.delayTime)
                .sendDate(this.sendDate)
                .build();
    }
}
