package com.slowletter.Service;

import com.slowletter.db.LetterEntity;
import com.slowletter.db.LetterRepository;
import com.slowletter.db.UserEntity;
import com.slowletter.domain.dto.SendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class LetterService {
    private final LetterRepository letterRepository;

    public void markAsRead(Long id) {
        LetterEntity letter = letterRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Letter not found"));
        letter.setRead(true);
        letterRepository.save(letter);
    }


    //LetterSendController에서 /letter/send 포스트 매핑된거 편지 전송! 버튼 누르면 아래send함수 실행되게함. 결국 편지send하면서 database에 저장
    //send(seRequest)를 controller에서 실행시키면 request.toEntity()로 변환시키는거임 이 request는 이전에 getmapping할때 new SendRequest()로 보내놓음
    //
    public void send(SendRequest request){
        letterRepository.save(request.toEntity()); //send하면 입력받은 정보들을 LetterEntity형식에 맞게 저장한다. db에 save가 jpa로 저장하는거임
    }

    //자신이 보낸 편지 확인하는거
    public List<LetterEntity> getAllSendLetters(UserEntity userEntity){
        //Optional<LetterEntity> letterEntity=letterRepository.findAllById(request.toEntity());
        List<LetterEntity> letterEntity=letterRepository.findAllBySender(userEntity.getLoginId());//letterEntity에 findAll()함수로 모든 보낸편지를 가져온다.
        for (int i = 0; i < letterEntity.size(); i++) {
            if(letterEntity.get(i).getAlarm()==1){
                letterEntity.remove(i);
            }
        }

        //유저의 id별로 가져오게 해야할듯 optional하게
        return letterEntity;
    }
    public List<LetterEntity> getAllLetters(UserEntity userEntity){
        List<LetterEntity> letterEntity=letterRepository.findAllByReceiverOrderBySendDateDesc(userEntity.getLoginId());//letterEntity에 findAll()함수로 모든 보낸편지를 가져온다.
        return letterEntity;
    }
    public List<LetterEntity> getAllReceiveLetters(UserEntity userEntity){
        //Optional<LetterEntity> letterEntity=letterRepository.findAllById(request.toEntity());
        List<LetterEntity> letterEntity=letterRepository.findAllByReceiver(userEntity.getLoginId());//letterEntity에 findAll()함수로 모든 보낸편지를 가져온다.
        for (int i = 0; i < letterEntity.size(); i++) {

            if(letterEntity.get(i).getAlarm()==1){
                letterEntity.remove(i);
            }
        }

        //유저의 id별로 가져오게 해야할듯 optional하게
        return letterEntity;
    }

    public int getUnreadLettersCount(UserEntity userEntity) {
        List<LetterEntity> unreadLetters = letterRepository.findAllByReceiverAndReadIsNull(userEntity.getLoginId());
        return unreadLetters.size();
    }


    //자신이 받은 편지 확인하는거
//    public List<LetterEntity> getAllReceiveLetters(){
//        List<LetterEntity> letterEntity=letterRepository.findAll();//letterEntity에 findAll()함수로 모든 보낸편지를 가져온다.
//
//
//    }


}
