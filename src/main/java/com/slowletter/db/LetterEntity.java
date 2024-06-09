package com.slowletter.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity(name="letter")
public class LetterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    private String receiver;
    private String message;

    private int delayTime;

    private int alarm; //이게 0이면 편지이고 1이면 알림.
    private String interest;
    private Date sendDate;
    private Date receiveDate;


    @Column(name = "`read`")
    private Boolean read = false;}
