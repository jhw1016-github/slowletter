package com.slowletter.Controller;

import com.slowletter.Handler.WebSocketHandler;
import com.slowletter.Service.UserService;
import com.slowletter.db.UserEntity;
import com.slowletter.domain.dto.JoinRequest;
import com.slowletter.domain.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
@RequestMapping("/session-login")
public class SessionLoginController {

    private final UserService userService;

    public static final ConcurrentHashMap<String, Queue<TextMessage>> messageQueue = new ConcurrentHashMap<>();


    //session별로 clinet가 로그인한다. 로그인할때 생성되는 client와 1대1 매칭되는 session정보 관리를 위한 static변수임.
    public static HashMap<String, HttpSession> httpSessionListEachClient = new HashMap<>();
    @GetMapping(value={"","/"})
    public String home(Model model, @SessionAttribute(name = "loginId", required = false) String loginId) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");
        UserEntity loginUser = userService.getLoginUserByLoginId(loginId);
        if(loginId == null) {
            System.out.println("로그인 하지 않음");
        }
        else{
            model.addAttribute("nickname", loginUser.getNickname());
        }
        return "home";
    }

    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");

        model.addAttribute("joinRequest", new JoinRequest());
        return "joinTest";
    }

    @PostMapping("/join")
    public String join(@Valid @ModelAttribute JoinRequest joinRequest, BindingResult bindingResult, Model model) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");

        // loginId 중복 체크
        if(userService.checkLoginIdDuplicate(joinRequest.getLoginId())) {
            bindingResult.addError(new FieldError("joinRequest", "loginId", "로그인 아이디가 중복됩니다."));
        }
        // 닉네임 중복 체크
        if(userService.checkNicknameDuplicate(joinRequest.getNickname())) {
            bindingResult.addError(new FieldError("joinRequest", "nickname", "닉네임이 중복됩니다."));
        }
        // password와 passwordCheck가 같은지 체크
        if(!joinRequest.getPassword().equals(joinRequest.getPasswordCheck())) {
            bindingResult.addError(new FieldError("joinRequest", "passwordCheck", "바밀번호가 일치하지 않습니다."));
        }

        if(bindingResult.hasErrors()) {
            return "join";
        }

        userService.join(joinRequest);
        return "redirect:/session-login";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");

        model.addAttribute("loginRequest", new LoginRequest());
        return "loginTest";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequest loginRequest, BindingResult bindingResult,
                        HttpServletRequest httpServletRequest, Model model) throws InterruptedException {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");

        UserEntity user = userService.login(loginRequest);
        // 로그인 아이디나 비밀번호가 틀린 경우 global error return
        if(user == null) {
            bindingResult.reject("loginFail", "로그인 아이디 또는 비밀번호가 틀렸습니다.");
        }
        if(bindingResult.hasErrors()) {
            return "login";
        }

        // 로그인 성공 => 세션 생성

        // 세션을 생성하기 전에 기존의 세션 파기
        httpServletRequest.getSession().invalidate();
        HttpSession session = httpServletRequest.getSession(true);  // Session이 없으면 생성
        // 세션에 userId를 넣어줌
        session.setAttribute("loginId", user.getLoginId());
        session.setMaxInactiveInterval(1800); // Session이 30분동안 유지
        httpSessionListEachClient.put(session.getAttribute("loginId").toString(),session);
        for(Map.Entry<String, HttpSession> entry : httpSessionListEachClient.entrySet()){
            String loginId = entry.getKey();
            String  httpSession= entry.getValue().getId();
            System.out.println("loginId: " + loginId + ",httpSession : " + httpSession);
        }
        System.out.println(messageQueue.get(user.getLoginId()));

        if (messageQueue.containsKey(user.getLoginId())) {
            session.setAttribute("requestWebSocketConnection",true);
            //Queue<TextMessage> queuedMessages = messageQueue.get(user.getLoginId());
            System.out.println("Queued messages for user " + user.getLoginId() + " have been sent.");
        }


        return "redirect:/session-login";
    }

    @PostMapping("/websocket-ready")
    public ResponseEntity<String> websocketReady(@SessionAttribute(name = "loginId", required = false) String loginId) {
        if (loginId != null) {
            WebSocketHandler.sendQueuedMessages(loginId);
            return ResponseEntity.ok("WebSocket ready and messages sent.");
        } else {
            return ResponseEntity.badRequest().body("No user logged in.");
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, Model model) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");
        HttpSession session = request.getSession(false);  // Session이 없으면 null return
        httpSessionListEachClient.remove(session.getAttribute("loginId").toString());

        if(session != null) {
            session.invalidate();
        }
        return "redirect:/session-login";
    }

    @GetMapping("/info")
    public String userInfo(@SessionAttribute(name = "loginId", required = false) String loginId, Model model) {
        model.addAttribute("loginType", "session-login");
        model.addAttribute("pageName", "세션 로그인");
        UserEntity loginUser = userService.getLoginUserByLoginId(loginId);
        if(loginUser == null) {
            return "redirect:/session-login/login";
        }

        model.addAttribute("user", loginUser);
        return "info";
    }

}