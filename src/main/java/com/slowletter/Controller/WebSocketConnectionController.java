package com.slowletter.Controller;





//js파일에서 서버로 5초단위로 계속해서 요청한다. 혹시 지금 접속해 있는 client의 httpSession 의 attribute값이 바뀐게 있는지


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/checkWebSocketConnectionRequest")
public class WebSocketConnectionController {
    // Controller endpoint to handle AJAX request to check WebSocket connection request
    @GetMapping("")
    @ResponseBody
    public Map<String, Object> checkWebSocketConnectionRequest(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false);
        if (session != null) {
            Boolean requestWebSocketConnection = (Boolean) session.getAttribute("requestWebSocketConnection");
            if (requestWebSocketConnection != null && requestWebSocketConnection) {
                // Attribute is set, indicate to the client to initiate WebSocket connection

                response.put("requestWebSocketConnection", true);
                // Optionally, reset the attribute after sending the response
                session.removeAttribute("requestWebSocketConnection");
            } else {
                response.put("requestWebSocketConnection", false);
            }
        } else {
            response.put("requestWebSocketConnection", false);
        }
        return response;
    }

}
