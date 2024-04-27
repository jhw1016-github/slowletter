//package com.slowletter.Controller;
//
//import com.slowletter.db.UserEntity;
//import com.slowletter.db.UserRepository;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//
//import javax.swing.text.html.parser.Entity;
//import java.util.List;
//
//
//@RequiredArgsConstructor // private final UserRepository userRepository; 생성자로서 userRepository를 주입받겠다는 어노테이션
//@RestController
//@RequestMapping("/api")
//public class dbTestController {
//    private final UserRepository userRepository;
//    @GetMapping("/find-all")
//    public List<UserEntity> findAll(){
//        return userRepository.findAll();
//    }
//
//    //autosave의 역할이 /name이후 ?name=장현우 이런식으로 하면 장현우라는 값이 mysql에 user라는 db에 user라는 테이블내에 저장되게된다.
//    //insert문을 웹에서 하는거지
//    @GetMapping("/name")
//    public void autoSave(
//            @RequestParam String name
//    ){
//        var user =UserEntity.builder()
//                .name(name)
//                .build()
//                ;
//        userRepository.save(user);
//    }
//}
