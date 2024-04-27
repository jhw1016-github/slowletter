package com.slowletter.Handler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;//추가
import org.springframework.web.socket.TextMessage;//추가
import org.springframework.web.socket.WebSocketSession;//추가
import org.springframework.web.socket.handler.TextWebSocketHandler;//추가

import java.io.IOException;//추가
import java.util.concurrent.ConcurrentHashMap;//추가

@Component
public class WebSocketHandler extends TextWebSocketHandler {


    private static final ConcurrentHashMap<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<String, WebSocketSession>();
    //CLIENTS 라는 변수에 세션을 담아두기위한 맵형식의 공간입니다.

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        CLIENTS.put(session.getId(), session);
        System.out.println(CLIENTS);
    }
    //이 코드는 사용자가 웹소켓 서버에 접속하게 되면 동작하는 메소드 입니다.
    //
    //이때 WebSocketSession 값이 생성 되는데 그 값을 위에서 미리 만들어준, CLIENTS 변수에 put으로 담아줍니다.(키값은 세션의 고유값 입니다.)

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        CLIENTS.remove(session.getId());
    }
    //이 코드는 웹소켓 서버접속이 끝났을때 동작하는 메소드 입니다.
    //
    //이때 CLIENTS 변수에 있는 해당 세션을 제거 합니다.

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String id = session.getId();  //메시지를 보낸 아이디
        CLIENTS.entrySet().forEach(arg -> {
            if (!arg.getKey().equals(id)) {  //같은 아이디가 아니면 메시지를 전달합니다.
                try {
                    arg.getValue().sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    //이 코드는 사용자의 메세지를 받게되면 동작하는 메소드입니다.
    //CLIENT 변수에 담긴 세션값들을 가져와서 반복문으로 돌려서,
    // 위 처럼 메세지를 발송해주면, 본인 이외의 사용자에게 메세지를 보낼 수 있는 코드가 됩니다.
    @Scheduled(fixedDelay = 7 * 24 * 60 * 60 * 1000) // Run every 7 days
    public void sendDelayedLetter() {
        // Create the delayed letter with image URL
        String delayedLetter = "This is a delayed letter sent 7 days later.";
        String imageUrl = "https://example.com/images/letter_image.jpg";
        String letterWithImage = delayedLetter + "<br><img src='" + imageUrl + "' alt='Letter Image'>";

        // Send the letter with image URL to all connected clients
        TextMessage textMessage = new TextMessage(letterWithImage);
        CLIENTS.forEach((id, session) -> {
            try {
                session.sendMessage(textMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Scheduled(fixedDelay = 7 * 24 * 60 * 60 * 1000 - 7 * 24 * 60 * 60 * 1000) // Run 7 days before the letter arrives
    public void notifyRecipient() {
        String notificationMessage = "Someone delivered the letter! Your letter will arrive soon";
        TextMessage textMessage = new TextMessage(notificationMessage);
        CLIENTS.forEach((id, session) -> {
            try {
                session.sendMessage(textMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}