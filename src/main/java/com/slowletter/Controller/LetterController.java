package com.slowletter.Controller;
import com.slowletter.Service.LetterService;
import com.slowletter.Service.UserService;
import com.slowletter.db.LetterEntity;
import com.slowletter.db.LetterRepository;
import com.slowletter.Handler.WebSocketHandler;
import com.slowletter.db.UserEntity;
import com.slowletter.domain.dto.SendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


//편지 서비스 동작 컨트롤러
@Controller
@RequiredArgsConstructor
@RequestMapping("/letter")
public class LetterController {

    private final LetterRepository letterRepository;
    private final LetterService letterService;
    private final WebSocketHandler webSocketHandler;

    private final UserService userService;

    //편지 서비스 화면 /letter 경로에 getmapping
    @GetMapping("")
    public String LetterPage(Model model){
        model.addAttribute("pageName","편지 서비스 화면");
        model.addAttribute("sendRequest", new SendRequest());//sendRequest라는 속성을 담아서 model에 addAttribute해서 new SendRequest()한다

        return "letter";
    }

    @PostMapping("/receiveSuccess")
    public void sendSuccess(@RequestBody SendRequest sendRequest, Model model, BindingResult bindingResult){
        letterService.send(sendRequest);
    }

    @GetMapping("/sendrandom")
    public String sendLetterRandom(@SessionAttribute(name = "loginId", required = false) String loginId, Model model){
        model.addAttribute("pageName", "랜덤 쓰기");
        model.addAttribute("sendRequest", new SendRequest());//sendRequest라는 속성을 담아서 model에 addAttribute해서 new SendRequest()한다
        UserEntity loginUser = userService.getLoginUserByLoginId(loginId);
        model.addAttribute("user",loginUser);
        return "randomSenderTest";

    }
    @PostMapping("/sendrandom")
    public String sendLetterRandom(@ModelAttribute SendRequest sendRequest, BindingResult bindingResult, Model model) {
        model.addAttribute("pageName","편지전송완료화면");
        System.out.println(sendRequest.getInterest());
        System.out.println(sendRequest.getSendLocation());
        //letterService.send(sendRequest);//send(sendRequest)로 인해 편지 전송을 하게된다. 이
        // Save the letter to the database
        // Notify the receiver via WebSocket
        //notifyReceiver(letter);
        return "sendSuccess";
    }

    @GetMapping("/sendtome")
    public String sendLetterToMe(@SessionAttribute(name = "loginId", required = false) String loginId, Model model){
        model.addAttribute("pageName", "내게 쓰기");
        model.addAttribute("sendRequest", new SendRequest());//sendRequest라는 속성을 담아서 model에 addAttribute해서 new SendRequest()한다
        UserEntity loginUser = userService.getLoginUserByLoginId(loginId);
        System.out.println(loginUser.getLoginId());
        model.addAttribute("user",loginUser);
        return "letterSendToMeTest";

    }
    @PostMapping("/sendtome")
    public String sendLetterToMe(@ModelAttribute SendRequest sendRequest, BindingResult bindingResult, Model model) {
        model.addAttribute("pageName","편지전송완료화면");
        //letterService.send(sendRequest);//send(sendRequest)로 인해 편지 전송을 하게된다. 이
        // Save the letter to the database
        // Notify the receiver via WebSocket
        //notifyReceiver(letter);
        return "sendSuccess";
    }






    //편지 전송 기능  /letter/send 경로에 getmapping해놓았고 sendRequest()보낸다.

    @GetMapping("/send")
    public String sendLetter(@SessionAttribute(name = "loginId", required = false) String loginId,Model model){
        model.addAttribute("pageName", "편지 쓰기");
        model.addAttribute("sendRequest", new SendRequest());//sendRequest라는 속성을 담아서 model에 addAttribute해서 new SendRequest()한다
        //이렇게 하면 html에서 timeleaf할때나 SendRequest를 html에 바인딩시키는 느낌 /letter

        UserEntity loginUser = userService.getLoginUserByLoginId(loginId);
        System.out.println(loginUser.getLoginId());
        model.addAttribute("user",loginUser);


        return "letterSenderTest";

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

    @GetMapping("/alarm")
    public String alarmPage(@ModelAttribute SendRequest sendRequest, @SessionAttribute(name = "loginId", required = false) String loginId, Model model){
        UserEntity userEntity=userService.getLoginUserByLoginId(loginId);
        List<LetterEntity> allAlarmAndLetter = letterService.getAllLetters(userEntity);
        model.addAttribute("allAlarmAndLetters",allAlarmAndLetter);

        return "alarm";
    }

    @PostMapping("/markAsRead")
    @ResponseBody
    public ResponseEntity<String> markAsRead(@RequestParam Long id) {
        letterService.markAsRead(id);
        return ResponseEntity.ok("Marked as read");
    }

    @GetMapping("/notificationCount")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getNotificationCount(@SessionAttribute(name = "loginId", required = false) String loginId) {
        UserEntity userEntity = userService.getLoginUserByLoginId(loginId);
        int count = letterService.getUnreadLettersCount(userEntity);
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/sendBox")
    public String sendBoxPage(@ModelAttribute SendRequest sendRequest, @SessionAttribute(name = "loginId", required = false) String loginId, Model model){
        //session에 로그인해 있는 사용자의 userid를 가져와서 해당 사용자의 userid에 해당하느
        UserEntity userEntity=userService.getLoginUserByLoginId(loginId);
        List<LetterEntity> allSendLetter=letterService.getAllSendLetters(userEntity);
        //db로부터 가져온값을 letterEntity에 저장한다.
        model.addAttribute("allSendLetters", allSendLetter); // 저장한걸
        return "sendBoxTest";
    }
    @GetMapping("/receiveBox")
    public String receiveBoxPage(@SessionAttribute(name = "loginId", required = false) String loginId, Model model){
        //session에 로그인해 있는 사용자의 userid를 가져와서 해당 사용자의 userid에 해당하느
        UserEntity userEntity=userService.getLoginUserByLoginId(loginId);
        List<LetterEntity> allReceiveLetter=letterService.getAllReceiveLetters(userEntity);
        System.out.println(allReceiveLetter);
        //db로부터 가져온값을 letterEntity에 저장한다.
        model.addAttribute("allReceiveLetters", allReceiveLetter); // 저장한걸
        //db로 부터 값을 받아올때는 굳이 sendRequest를 안거쳐도 되지 그냥 db에 있는 형식 그대로 findall하면 되는데
        //jpa repository사용해서
        return "receiveBoxTest";
    }



}







