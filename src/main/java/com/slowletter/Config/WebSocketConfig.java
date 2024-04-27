package com.slowletter.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;//추가
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;//추가

//websocket에 대한 전반적인 설정을 해줘야 합니다.

//어떤 요청에 대한 어떤 응답을 할것인지에 대한 정의를 결정합니다.
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/letter/send").setAllowedOrigins("*");
    }
    //'/아무거나해요' 이부분은 나중에 서버 세팅을 끝난 후 실제로 테스트해볼때 정해지는 url 입니다.
    //
    //ex) ws://127.0.0.1:포트/아무거나해요
    //
    //
    //
    //그리고 setAllowedOrigins("*") 는 웹소켓 cors 정책으로 인해, 허용 도메인을 지정해줘야합니다.
    //
    //현재 저희는 테스트이기때문에 * 와일드카드로 모든 도메인을 열어줍시다.(실제로 개발하실때에는 보안상의 위험으로 와일드카드는 쓰시면안됍니다.)


}
