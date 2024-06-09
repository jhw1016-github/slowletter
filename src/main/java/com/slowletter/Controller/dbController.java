package com.slowletter.Controller;

import com.slowletter.Service.LetterService;
import com.slowletter.Service.UserService;
import com.slowletter.db.UserEntity;
import com.slowletter.db.UserRepository;
import com.slowletter.domain.dto.SendRequest;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;


@RequiredArgsConstructor // private final UserRepository userRepository; 생성자로서 userRepository를 주입받겠다는 어노테이션
@RestController
@RequestMapping("/letter2")
public class dbController {
    private final UserRepository userRepository;
    private final LetterService letterService;
    @PostMapping("/receiveSuccess")
    public void autoSave(@RequestBody SendRequest sendRequest){
        letterService.send(sendRequest);
    }
}
