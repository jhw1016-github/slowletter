package com.slowletter.Service;


import com.slowletter.db.UserEntity;
import com.slowletter.db.UserRepository;
import com.slowletter.domain.dto.JoinRequest;
import com.slowletter.domain.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public boolean checkLoginIdDuplicate(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    /**
     * nickname 중복 체크
     * 회원가입 기능 구현 시 사용
     * 중복되면 true return
     */
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    public void join(JoinRequest req) {
        userRepository.save(req.toEntity());
    }
    /**
     * 회원가입 기능 1
     * 화면에서 JoinRequest(loginId, password, nickname)을 입력받아 User로 변환 후 저장
     * loginId, nickname 중복 체크는 Controller에서 진행 => 에러 메세지 출력을 위해
     */


    public UserEntity login(LoginRequest req) {
        Optional<UserEntity> optionalUser = userRepository.findByLoginId(req.getLoginId());

        // loginId와 일치하는 User가 없으면 null return
        if(optionalUser.isEmpty()) {
            return null;
        }

        UserEntity userEntity = optionalUser.get();

        // 찾아온 User의 password와 입력된 password가 다르면 null return
        if(!userEntity.getPassword().equals(req.getPassword())) {
            return null;
        }

        return userEntity;
    }
    /**
     *  로그인 기능
     *  화면에서 LoginRequest(loginId, password)을 입력받아 loginId와 password가 일치하면 User return
     *  loginId가 존재하지 않거나 password가 일치하지 않으면 null return
     */

    public UserEntity getLoginUserById(Long userId) {
        if(userId == null) return null;

        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()) return null;

        return optionalUser.get();
    }

    public String getCountryByLoginId(String loginId){
        Optional<UserEntity> user =userRepository.findByLoginId(loginId);
        return user.get().getCountry();
    }




    public UserEntity getLoginUserByLoginId(String loginId) {
        if(loginId == null) return null;

        Optional<UserEntity> optionalUser = userRepository.findByLoginId(loginId);
        if(optionalUser.isEmpty()) return null;

        return optionalUser.get();
    }


    public List<UserEntity> chooseRandomByCountry(String receiveLocation, String interest,String sender){
        List<UserEntity> usersByCountry  =userRepository.findAllByCountry(receiveLocation);
        System.out.println(usersByCountry+"sdffksjdfksjf");
        usersByCountry.removeIf(user -> !containsInterest(user.getInterest(), interest)); //편지 보내는이가 설정한 관심사랑 겹치는게 하나도 없는 사람 제외
        System.out.println(usersByCountry+"sdkfaaa");
        usersByCountry.removeIf(user -> user.getLoginId().equals(sender)); //편지 보내는이는 제외한다.
        System.out.println(usersByCountry+"dfjdkf");
        return getRandomUsers(usersByCountry,3); //3명의 사용자를 랜덤하게 뽑는다
    }
    private boolean containsInterest(String userInterest, String interest) {
        if (userInterest.contains(interest)) {
            return true;
        }
        return false;
    }

    // 리스트에서 무작위 사용자 선택
    private List<UserEntity> getRandomUsers(List<UserEntity> users, int count) {
        List<UserEntity> randomUsers = new ArrayList<>();
        if (users.isEmpty() || count <= 0) {
            return randomUsers;
        }

        // 중복을 허용하지 않는 무작위 인덱스를 선택하기 위해 Set을 사용합니다.
        Set<Integer> indexes = new HashSet<>();
        Random random = new Random();

        // 원하는 수만큼의 사용자가 선택될 때까지 반복합니다.
        while (indexes.size() < Math.min(count, users.size())) {
            int index = random.nextInt(users.size());
            indexes.add(index);
        }

        // 선택된 인덱스에 해당하는 사용자를 리스트에 추가합니다.
        for (int index : indexes) {
            randomUsers.add(users.get(index));
        }

        return randomUsers;
    }


}