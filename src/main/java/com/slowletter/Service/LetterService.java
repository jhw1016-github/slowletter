package com.slowletter.Service;

import com.slowletter.db.LetterEntity;
import com.slowletter.db.LetterRepository;
import com.slowletter.domain.dto.SendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class LetterService {
    private final LetterRepository letterRepository;


    //LetterSendController에서 /letter/send 포스트 매핑된거 편지 전송! 버튼 누르면 아래send함수 실행되게함. 결국 편지send하면서 database에 저장
    //send(seRequest)를 controller에서 실행시키면 request.toEntity()로 변환시키는거임 이 request는 이전에 getmapping할때 new SendRequest()로 보내놓음
    //
    public void send(SendRequest request){
        letterRepository.save(request.toEntity()); //send하면 입력받은 정보들을 LetterEntity형식에 맞게 저장한다. db에 save가 jpa로 저장하는거임
    }
    //자신이 보낸 편지 확인하는거
    public Optional<LetterEntity> getAllSendLetters(SendRequest request){
        //Optional<LetterEntity> letterEntity=letterRepository.findAllById(request.toEntity());

        Optional<LetterEntity> letterEntity=letterRepository.findBySender(request.getSender());//letterEntity에 findAll()함수로 모든 보낸편지를 가져온다.
        //유저의 id별로 가져오게 해야할듯 optional하게
        return letterEntity;
    }


    //자신이 받은 편지 확인하는거
//    public List<LetterEntity> getAllReceiveLetters(){
//        List<LetterEntity> letterEntity=letterRepository.findAll();//letterEntity에 findAll()함수로 모든 보낸편지를 가져온다.
//
//
//    }


}
