package com.slowletter.Handler;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import static com.slowletter.Controller.SessionLoginController.httpSessionListEachClient;

@Component
public class HttpHandshakeInterceptor extends HttpSessionHandshakeInterceptor {


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 세션을 가져와서 사용자 ID를 attributes에 추가합니다.
        System.out.println("핸드세이크");
        if(request instanceof ServletServerHttpRequest){
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession(false); // Obtain the session
            if (session!=null && session.getId().equals(httpSessionListEachClient.get(session.getAttribute("loginId")).getId()) ) {
                //현재 세션에 로그인해서 웹소켓과의 연결을 시도한 사람인거니까 attribute값을 이렇게 준다.
                attributes.put("socketConnectId",session.getAttribute("loginId"));


            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception ex) {
        // 작업을 수행하지 않습니다.
    }

}