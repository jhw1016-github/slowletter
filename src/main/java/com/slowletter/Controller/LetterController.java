package com.slowletter.Controller;
import com.slowletter.Service.LetterService;
import com.slowletter.Service.UserService;
import com.slowletter.db.LetterEntity;
import com.slowletter.db.LetterRepository;
import com.slowletter.Handler.WebSocketHandler;
import com.slowletter.db.UserEntity;
import com.slowletter.domain.dto.SendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;



//편지 서비스 동작 컨트롤러
@Controller
@RequiredArgsConstructor
@RequestMapping("/letter")
public class LetterController {

    private final LetterRepository letterRepository;
    private final LetterService letterService;
    private final WebSocketHandler webSocketHandler;

    private final UserService userService;

//    @Autowired
//    public LetterController(LetterRepository letterRepository, LetterService letterService, WebSocketHandler webSocketHandler) {
//        this.letterService = letterService;
//        this.letterRepository = letterRepository;
//        this.webSocketHandler = webSocketHandler;
//    }
    //편지 서비스 화면 /letter 경로에 getmapping
    @GetMapping("")
    public String LetterPage(Model model){
        model.addAttribute("pageName","편지 서비스 화면");
        return "letter";
    }


    //편지 전송 기능  /letter/send 경로에 getmapping해놓았고 sendRequest()보낸다.
    @GetMapping("/send")
    public String sendLetter(Model model){
        model.addAttribute("pageName", "편지 전송");
        model.addAttribute("sendRequest", new SendRequest());//sendRequest라는 속성을 담아서 model에 addAttribute해서 new SendRequest()한다
        //이렇게 하면 html에서 timeleaf할때나 SendRequest를 html에 바인딩시키는 느낌 /letter
        return "letterSend";

    }

    //편지 전송 기능 /letter/send postmapping db에 값을 보낸다.
    //
    @PostMapping("/send")
    public String sendLetter(@ModelAttribute SendRequest sendRequest, BindingResult bindingResult, Model model) {
        model.addAttribute("pageName","편지전송완료화면");
        letterService.send(sendRequest);//send(sendRequest)로 인해 편지 전송을 하게된다. 이
        // Save the letter to the database
        // Notify the receiver via WebSocket
        //notifyReceiver(letter);
        return "letterSend";
    }
    @GetMapping("/sendBox")
    public String sendBoxPage(@ModelAttribute SendRequest sendRequest, @SessionAttribute(name = "userId", required = false) Long userId, Model model){

        Optional<LetterEntity> sendLetters=letterService.getAllSendLetters(sendRequest); //db로부터 가져온값을 letterEntity에 저장한다.
        System.out.println(sendLetters.toString());
        model.addAttribute("sendLetters", sendLetters); // 저장한걸
        //db로 부터 값을 받아올때는 굳이 sendRequest를 안거쳐도 되지 그냥 db에 있는 형식 그대로 findall하면 되는데
        //jpa repository사용해서

        return "sendBox";
    }



}







