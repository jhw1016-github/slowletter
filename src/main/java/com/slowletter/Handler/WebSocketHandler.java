package com.slowletter.Handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.slowletter.Service.UserService;
import com.slowletter.db.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.socket.CloseStatus;//추가
import org.springframework.web.socket.TextMessage;//추가
import org.springframework.web.socket.WebSocketSession;//추가
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.handler.TextWebSocketHandler;//추가

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;//추가
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;//추가
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock; //추가

import static com.slowletter.Controller.SessionLoginController.httpSessionListEachClient;
import static com.slowletter.Controller.SessionLoginController.messageQueue;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class WebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> SocketSessionListEachClient = new ConcurrentHashMap<String, WebSocketSession>();
    //CLIENTS 라는 변수에 세션을 담아두기위한 맵형식의 공간


    private final ReentrantLock lock = new ReentrantLock(); //이것도 추가해줌


    private void enqueueMessage(String receiver, TextMessage message) {
        messageQueue.computeIfAbsent(receiver, k -> new ConcurrentLinkedQueue<>()).add(message);
    }


    private final UserService userService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //################33 websockethandler에서는 httpsession에 접근할 수 없대.
        SocketSessionListEachClient.put(session.getId(), session);//저장을 웹소켓과 연결된 사용자로그인id로 안한다.
        //그냥 웹소켓세션id를 키값으로 준다.왜냐면 사용자로그인id로 해버리면 한 key값에 여러개의 웹소켓세션이 들어가니깐
        //session.getAttributes()원래 하면 아무것도 없었는데 HttpHandsha
        //그래서 만약 jhw와 연결했던 웹소켓세션id를 찾고싶으면
        for(Map.Entry<String, WebSocketSession> entry : SocketSessionListEachClient.entrySet()){
            String loginId=entry.getValue().getAttributes().get("socketConnectId").toString(); //웹소켓 세션내에 저장한 attribute값 웹소켓세션과 연결했던 클라이언트loginid
                //jhw와 일치하면 뭐 연결을 다시 하든가 편지를 보내던가
                //반복문 도는 이유는 jhw가 보낸 편지가 여러개일수도 있자나 그럼 소켓도 여러개이겠지
            String websocketSessionId= entry.getKey();
            System.out.println("연결된 사용자 id : "+loginId+", 연결된 웹소켓세션id : "+websocketSessionId);
        }
    }
    //이 코드는 사용자가 웹소켓 서버에 접속하게 되면 동작하는 메소드 입니다.
    //
    //이때 WebSocketSession 값이 생성 되는데 그 값을 위에서 미리 만들어준, CLIENTS 변수에 put으로 담아줍니다.(키값은 세션의 고유값 입니다.)

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SocketSessionListEachClient.remove(session.getId());
        System.out.println("삭제되느거"+session.getId());
    }
    //이 코드는 웹소켓 서버접속이 끝났을때 동작하는 메소드 입니다.
    //
    //이때 CLIENTS 변수에 있는 해당 세션을 제거 합니다.

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //딜레이 계산을 통해 알림 보내는거랑 전송 처리
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(message.getPayload());
        System.out.println("handleTest");
        String sender=node.has("sender") ? node.get("sender").asText() : null;
        String receiver = node.has("receiver") ? node.get("receiver").asText() : null;
        System.out.println(receiver);
        String receiveLocation =node.has("receiveLocation") ? node.get("receiveLocation").asText() : null;
        System.out.println(receiveLocation);
        String sendLocation = node.has("sendLocation") ? node.get("sendLocation").asText() : null;
        System.out.println(sendLocation);
        String interests = node.has("interest") ? node.get("interest").asText() : null;
        System.out.println(interests);
        String sendDate=node.has("sendDate")? node.get("sendDate").asText() : null;
        System.out.println("sendDate"+sendDate);
        if (receiver == null && receiveLocation != null && sendLocation != null) {
            //랜덤 편지 전송하기 전에 랜덤 수신자 먼저 추출하기  서버가 전달받은 sender, receiveLocation, sendLocation, interest를 이용해서
            try {
                //일단 수신자를 랜덤으로 뽑아야한다.
                List<UserEntity> randomUsers=userService.chooseRandomByCountry(receiveLocation,interests,sender);
                System.out.println(randomUsers);
                //sendLocation과 receiveLocation을 이용해서
                //랜덤 수신자를 추출했으면 어차피 특정 지역으로 전송하는거니까 sendLocation 이랑 receiveLocation은 동일하다.
                long delayTime=calculateDelayTime(sendLocation,receiveLocation);
                ((ObjectNode) node).put("delayTime", delayTime);  //delayTime message에 함께 넣어주고
                if(delayTime<24*60*60*1000){
                    //계산된 딜레이 시간이 1일 이하이면 알림 하나만 보낸다.
                    //알림을 하나만 보내깐 랜덤 수신자 for문으로 각각 1개만 전송
                    System.out.println("test중");
                    sendRandomUserMessages(randomUsers, (ObjectNode) node, mapper, session, 0, 1);
                }else {
                    int alarmCount= (int) (delayTime/24*60*60*1000);//1일로 나눴을때 ex) 7일이면
                    //알림을 7번 보내야한다면 0~6 for문 돌려서 0일때 각 사용자에게 다 알림보내고 또 1일대 각사용자에게 다 알림 보내고 이런식으로함
                    sendPeriodicRandomUserMessages(randomUsers, (ObjectNode) node, mapper, session, alarmCount);
                }
                //이 반복문은 단순 편지만 보내는 로직
                //flag 1 로 해서 알림보내는 기능도 넣어야함.
                sendRandomUserMessages(randomUsers, (ObjectNode) node, mapper, session, delayTime, 0);

            }catch (JsonProcessingException e){

            }
            //랜덤 편지 전송
            //scheduleDelayedSendingRandom(session, message);


        } else {
            handleDirectMessage(session, (ObjectNode) node, mapper, sender, receiver);
        }
    }


    //랜덤쓰기 일때 딜레이시간 1일보다 적을때 알림 및 편지 보내는 함수
    private void sendRandomUserMessages(List<UserEntity> randomUsers, ObjectNode node, ObjectMapper mapper, WebSocketSession session, long delayTime, int flag) throws JsonProcessingException {
        for (UserEntity randomUser : randomUsers) {
            ((ObjectNode) node).put("receiver", randomUser.getLoginId());
            System.out.println("test중중입니다");
            String updatedMessage = mapper.writeValueAsString(node);
            TextMessage updatedTextMessage = new TextMessage(updatedMessage);
            scheduleDelayedSending(session, updatedTextMessage, delayTime, flag);
        }
    }
    
    //랜덤쓰기 일때 딜레이시간 1일보다 길때 알림 및 편지 보내는 함수
    private void sendPeriodicRandomUserMessages(List<UserEntity> randomUsers, ObjectNode node, ObjectMapper mapper, WebSocketSession session, int alarmCount) throws JsonProcessingException {
        for (int i = 0; i < alarmCount; i++) {
            for (UserEntity randomUser : randomUsers) {
                ((ObjectNode) node).put("receiver", randomUser.getLoginId());
                String updatedMessage = mapper.writeValueAsString(node);
                TextMessage updatedTextMessage = new TextMessage(updatedMessage);
                scheduleDelayedSending(session, updatedTextMessage, i * 24 * 60 * 60 * 1000, 1);
            }
        }
    }
    //1대일 또는 내게 쓰기 일때 알림 및 편지 보내는 함수
    private void handleDirectMessage(WebSocketSession session, ObjectNode node, ObjectMapper mapper, String sender, String receiver) throws JsonProcessingException {
        //일반 1대1 전송 or 내게 쓰기
        //일단 전송 딜레이 시간을 계산한다.
        String senderCountry=userService.getCountryByLoginId(sender);
        String receiverCountry=userService.getCountryByLoginId(receiver);

        //계산된 딜레이시간을 알림보낼때 담아서 같이 보내기 위해서 "delayTime"이라는 fieldName에 저장함.
        long delayTime=calculateDelayTime(senderCountry,receiverCountry);
        ((ObjectNode) node).put("delayTime", delayTime);
        TextMessage message = new TextMessage(node.toString());

        if(delayTime<24*60*60*1000){
            //계산된 딜레이 시간이 1일 이하이면 알림 하나만 보낸다.
            scheduleDelayedSending(session,message,0,1);//1일 이하면 딜레이 전송을 보내자마자 수신자에게 알림가게 딜레이를 0으로 설정
        }else {
            int alarmCount= (int) (delayTime/24*60*60*1000);//1일로 나눴을때 ex) 7일이면
            for (int i = 0; i <alarmCount ; i++) {
                scheduleDelayedSending(session,message,i*24*60*60*1000,1); //반복문으로 i=0이면 바로 보내고 i=1이면 1일이후에 알람 보내고  i=2이면 2일이후에 알람 보내고 이런식으로
            }
        }
        scheduleDelayedSending(session,message, calculateDelayTime(senderCountry,receiverCountry),0);//최종적으로 편지 전송
    }

    private long calculateDelayTime(String location1, String location2) {
       if(location1.equals(location2)){
           Long delayTime= (long) 5000.0;
           return delayTime;
       }
       return (long) 10000.0;
    }


    //flag=0 =>편지 flag=1 => 알림 1개 보내는 함수
    private void scheduleDelayedSending(WebSocketSession session, TextMessage message, long delayTime,int flag) {
        String id = session.getId();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode messageObject;
        try {
            messageObject = objectMapper.readTree(message.getPayload());
            String sender = messageObject.get("sender").asText();
            String receiver = messageObject.get("receiver").asText();
            String letterContent = messageObject.get("message").asText();

            System.out.println("그냥 delaytime 저장된거"+messageObject.get("delayTime").asLong());

            if (flag == 1) { //알림을 보내는거는 delayTime을 정보로 넘긴다.
                long remainDelayTime=messageObject.get("delayTime").asLong()-delayTime;
                ((ObjectNode) messageObject).put("remainDelayTime", remainDelayTime);
                message= new TextMessage(messageObject.toString());
                System.out.println("남은 delaytime"+messageObject.get("remainDelayTime"));
            }

            TaskScheduler scheduler = new ConcurrentTaskScheduler(); // Use appropriate scheduler
            TextMessage updatedMessage = message;
            scheduler.schedule(() -> {
                boolean isConnected = false;
                lock.lock();
                try {
                    for (Map.Entry<String, WebSocketSession> entry : SocketSessionListEachClient.entrySet()) {
                        Object socketConnectId = entry.getValue().getAttributes().get("socketConnectId");
                        if (socketConnectId != null && socketConnectId.toString().equals(receiver)) {
                            isConnected = true;
                            if (entry.getValue().isOpen()) {
                                try {
                                    entry.getValue().sendMessage(updatedMessage);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("성공적으로 " + receiver + "에게 전달하였습니다");
                            } else {
                                System.out.println("WebSocket session is not open for receiver: " + receiver);
                            }
                            break;
                        }
                    }

                    if (!isConnected) {
                        System.out.println(receiver + "와 연결된 소켓이 존재하지 않습니다 클라이언트에게 소켓연결요청을 보내세요");
                        HttpSession receiverSession = httpSessionListEachClient.get(receiver);
                        if (receiverSession != null) {
                            receiverSession.setAttribute("requestWebSocketConnection", true);
                            boolean isWebSocketSessionAdded = false;

                            while (!isWebSocketSessionAdded) {
                                for (WebSocketSession webSocketSession : SocketSessionListEachClient.values()) {
                                    Object socketConnectId = webSocketSession.getAttributes().get("socketConnectId");
                                    if (socketConnectId != null && socketConnectId.toString().equals(receiver)) {
                                        if (webSocketSession.isOpen()) {
                                            try {
                                                webSocketSession.sendMessage(updatedMessage);
                                            } catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }
                                            System.out.println("Letter sent successfully to " + receiver);
                                            isWebSocketSessionAdded = true;
                                            break;
                                        } else {
                                            System.out.println("WebSocket session is not open for receiver: " + receiver);
                                        }
                                    }
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            System.out.println(receiver + "와 연결된 HttpSession이 존재하지 않습니다.");
                            System.out.println(receiver + "에 전달되어야할 편지를 서버에서 queue에서 관리하고 있다가 수신자가 로그인하면 웹소켓 연결해서 바로 보내주고 바로 팝업창 띄우도록해라");
                            enqueueMessage(receiver,updatedMessage);
                            System.out.println(messageQueue.get(receiver));

                        }
                    }
                } finally {
                    lock.unlock();
                }
            }, Instant.now().plusMillis(delayTime));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void sendQueuedMessages(String loginId) {
        Queue<TextMessage> queuedMessages = messageQueue.get(loginId);
        System.out.println("Is it working?");
        if (queuedMessages != null) {
            WebSocketSession session = SocketSessionListEachClient.values().stream()
                    .filter(s -> loginId.equals(s.getAttributes().get("socketConnectId")))
                    .findFirst().orElse(null);
            System.out.println("Is it working? 2");

            if (session != null && session.isOpen()) {
                System.out.println("Is it working? 3");

                // Group messages by sender
                Map<String, Queue<TextMessage>> messagesBySender = new HashMap<>();
                while (!queuedMessages.isEmpty()) {
                    TextMessage message = queuedMessages.poll();
                    if (message != null) {
                        String sender = getSenderFromMessage(message);
                        messagesBySender.computeIfAbsent(sender, k -> new LinkedList<>()).add(message);
                    }
                }

                // Send messages for each sender in order
                for (Map.Entry<String, Queue<TextMessage>> entry : messagesBySender.entrySet()) {
                    String sender = entry.getKey();
                    Queue<TextMessage> senderMessages = entry.getValue();

                    while (!senderMessages.isEmpty()) {
                        TextMessage message = senderMessages.poll();
                        if (message != null) {
                            try {
                                session.sendMessage(message);
                                System.out.println("Message from " + sender + " was sent to " + loginId + ".");
                            } catch (IOException e) {
                                System.err.println("Failed to send message from " + sender + ": " + e.getMessage());
                                e.printStackTrace();
                                // Re-add the remaining messages for this sender back to the queue
                                messageQueue.computeIfAbsent(loginId, k -> new LinkedList<>()).addAll(senderMessages);
                                break; // Stop processing further senders if there's an error
                            }
                        }
                    }
                }
            } else {
                System.err.println("WebSocket session does not exist or is not open: " + loginId);
            }
        } else {
            System.out.println("There are no messages waiting in queue: " + loginId);
        }
    }

    private static String getSenderFromMessage(TextMessage message) {
        // Extract the sender information from the message payload
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(message.getPayload());
            return node.has("sender") ? node.get("sender").asText() : "unknown";
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "unknown";
        }
    }

}


//랜덤 쓰기 할때 동기화처리없이 알림이랑 함께 편지 보내면 같은 websocket 세션에 충돌이 일어난다고 동기화처리 하기 전 코드
//scheduler.schedule(() -> {
//                boolean isConnected = false;
//                for (Map.Entry<String, WebSocketSession> entry : SocketSessionListEachClient.entrySet()) {
//                    Object socketConnectId = entry.getValue().getAttributes().get("socketConnectId");
//                    if (socketConnectId != null && socketConnectId.toString().equals(receiver)) {
//                        isConnected = true;
//                        try {
//                            entry.getValue().sendMessage(updatedMessage);
//                            System.out.println("성공적으로"+receiver+"에게 전달하였습니다");
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                        break; // 연결된 사용자를 찾았으므로 반복문 종료
//                    }
//                }
//
//                if (!isConnected) {
//                    System.out.println(receiver+"와 연결된 소켓이 존재하지 않습니다 클라이언트에게 소켓연결요청을 보내세요");
//                    // 연결되어 있지 않은 경우 처리
//                    // 사용자에게 소켓 연결 요청 등을 수행
//                    HttpSession receiverSession = httpSessionListEachClient.get(receiver);
//                    System.out.println(receiver + "와 연결된 소켓이 존재하지 않습니다. 클라이언트에게 소켓 연결 요청을 보내세요.");
//                    if (receiverSession != null) {
//                        // Set an attribute to signal the client to establish WebSocket connection
//                        receiverSession.setAttribute("requestWebSocketConnection", true);
//                        boolean isWebSocketSessionAdded = false;
//
//                        while (!isWebSocketSessionAdded) {
//                            // Check if WebSocket session information is added
//                            for (WebSocketSession webSocketSession : SocketSessionListEachClient.values()) {
//                                Object socketConnectId = webSocketSession.getAttributes().get("socketConnectId");
//                                if (socketConnectId != null && socketConnectId.toString().equals(receiver)) {
//                                    try {
//                                        webSocketSession.sendMessage(updatedMessage);
//                                        System.out.println("Letter sent successfully to " + receiver);
//                                        isWebSocketSessionAdded = true; // Exit the loop
//                                        break; // Exit the loop
//                                    } catch (IOException e) {
//                                        // Handle exception
//                                        e.printStackTrace();
//                                    }
//                                }
//                            }
//
//                            // Wait for a short duration before checking again
//                            try {
//                                Thread.sleep(1000); // Wait for 1 second
//                            } catch (InterruptedException e) {
//                                // Handle exception
//                                e.printStackTrace();
//                            }
//                        }
//
//                    } else {
//                        System.out.println(receiver + "와 연결된 HttpSession이 존재하지 않습니다.");
//                        System.out.println(receiver+"에 전달되어야할 편지를 서버에서 queue에서 관리하고 있다가 수신자가 로그인하면 웹소켓 연결해서 바로 보내주고 바로 팝업창 띄우도록해라");
//                        //enqueueMessage(receiver, updatedMessage);//큐에 추가
//                    }
//                }
//            }, Instant.now().plusMillis(delayTime));














    //이 코드는 사용자의 메세지를 받게되면 동작하는 메소드입니다.
    //CLIENT 변수에 담긴 세션값들을 가져와서 반복문으로 돌려서,
    // 위 처럼 메세지를 발송해주면, 본인 이외의 사용자에게 메세지를 보낼 수 있는 코드가 됩니다.

//    private  int executionCount=0;
//    @Scheduled(initialDelay = 10000,fixedDelay = 1000)
//    public void sendDelayedLetter(){
//        if(executionCount>=10){
//            return;
//        }
//        //String id=session.getId();
//        System.out.println("지연 전송이 완료되었습니다?");
//
//        executionCount++;
////        CLIENTS.entrySet().forEach(arg -> {
////            if (!arg.getKey().equals(session.getid)) {  //같은 아이디가 아니면 메시지를 전달합니다.
////                try {
////                    arg.getValue().sendMessage(message);
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////        });
//
//    }

    //    @Scheduled(initialDelay = 5000) // Run every 7 days
//    public void sendDelayedLetterWithImage() {
//        // Create the delayed letter with image URL
//        String delayedLetter = "This is a delayed letter sent 7 days later.";
//        String imageUrl = "https://example.com/images/letter_image.jpg";
//        String letterWithImage = delayedLetter + "<br><img src='" + imageUrl + "' alt='Letter Image'>";
//
//        // Send the letter with image URL to all connected clients
//        TextMessage textMessage = new TextMessage(letterWithImage);
//        CLIENTS.forEach((id, session) -> {
//            try {
//                session.sendMessage(textMessage);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }



//    @Scheduled(fixedDelay = 7 * 24 * 60 * 60 * 1000 - 7 * 24 * 60 * 60 * 1000) // Run 7 days before the letter arrives
//    public void notifyRecipient() {
//        String notificationMessage = "Someone delivered the letter! Your letter will arrive soon";
//        TextMessage textMessage = new TextMessage(notificationMessage);
//        CLIENTS.forEach((id, session) -> {
//            try {
//                session.sendMessage(textMessage);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }
//}


