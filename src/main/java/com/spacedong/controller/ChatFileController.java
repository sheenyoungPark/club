package com.spacedong.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.spacedong.beans.ChatMessageBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.ChatService;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/chat")
public class ChatFileController {

    @Resource(name = "loginMember")
    private MemberBean loginMember;

    @Autowired
    private ChatService chatService;

    @Value("${file.upload.dir:C:/upload/chat}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<ChatMessageBean> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("roomId") Long roomId) {

        if (loginMember == null || !loginMember.isLogin()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 업로드 디렉토리가 없으면 생성
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 파일 이름 및 경로 설정
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = Paths.get(uploadDir, newFilename);

            // 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 파일 URL 생성 (상대 경로)
            String fileUrl = "/uploads/chat/" + newFilename;

            // 메시지 타입 결정 (이미지인지 일반 파일인지)
            String messageType = "FILE";
            String contentType = file.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                messageType = "IMAGE";
            }

            // 채팅 메시지 생성 및 전송
            ChatMessageBean message = new ChatMessageBean();
            message.setRoomId(roomId);
            message.setSenderId(loginMember.getMember_id());
            message.setSenderType("MEMBER"); // 현재는 MEMBER만 지원, 필요시 확장
            message.setMessageType(messageType);
            message.setFilePath(fileUrl);

            if (messageType.equals("IMAGE")) {
                message.setMessageContent("[이미지]");
            } else {
                message.setMessageContent("[파일] " + originalFilename);
            }

            // 메시지 전송
            chatService.sendMessage(message);

            return ResponseEntity.ok(message);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}