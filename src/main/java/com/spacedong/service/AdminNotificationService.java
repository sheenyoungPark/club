package com.spacedong.service;

import com.spacedong.beans.ChatMessageBean;
import com.spacedong.beans.ChatRoomCreationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminNotificationService {

    @Autowired
    private ChatService chatService;

    @Value("${admin.id}")
    private String adminId;

    public void sendApprovalNotification(String userId, String userType, String notificationType,
                                         String itemName, String additionalInfo) {
        // 1. 채팅방 생성 또는 조회
        ChatRoomCreationResult result = chatService.getOrCreatePersonalChatRoomWithResult(
                adminId, "ADMIN", userId, userType);

        // 2. 메시지 내용 구성
        String messageContent;
        if (notificationType.equals("REQUEST1")) {
            messageContent = String.format(
                    "안녕하세요, 우주동입니다.\n'%s'에 대한 승인 요청이 접수되었습니다. 검토 후 결과를 알려드리겠습니다.",
                    itemName);
        } else if(notificationType.equals("REQUEST2")){
            messageContent = String.format(
                    "안녕하세요, 관리자님,\n'%s'에 대한 승인 요청이 접수되었습니다.",
                    itemName);
        }
        else if (notificationType.equals("APPROVED")) {
            messageContent = String.format(
                    "안녕하세요, 우주동입니다.\n'%s'에 대한 승인이 완료되었습니다. %s",
                    itemName, additionalInfo);
        } else if (notificationType.equals("REJECTED")) {
            messageContent = String.format(
                    "안녕하세요, 우주동입니다.\n'%s'에 대한 승인이 거절되었습니다. 사유: %s",
                    itemName, additionalInfo);
        } else {
            messageContent = String.format(
                    "안녕하세요, 우주동입니다.\n%s",
                    additionalInfo);
        }

        // 3. 메시지 객체 생성
        ChatMessageBean message = new ChatMessageBean();
        message.setRoomId((long)result.getRoom().getRoom_id());
        message.setSenderId(adminId);
        message.setSenderType("ADMIN");
        message.setMessageContent(messageContent);
        message.setMessageType("TEXT");
        message.setSenderNickname("우주동");

        // 4. 메시지 전송
        chatService.sendMessage(message);
    }
}

