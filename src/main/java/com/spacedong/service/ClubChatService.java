// src/main/java/com/spacedong/service/ClubChatService.java
package com.spacedong.service;

import com.spacedong.beans.ChatRoom;
import com.spacedong.beans.ClubBean;
import com.spacedong.beans.ClubMemberBean;
import com.spacedong.mapper.ClubMemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 클럽 채팅 관련 서비스 클래스
 * 기존 ChatService와 ClubService를 연결하는 역할
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClubChatService {

    @Autowired
    private final ChatService chatService;

    @Autowired
    private final ClubService clubService;

    @Autowired
    private ClubMemberService clubMemberService;

    /**
     * 클럽 채팅방 생성 또는 접근
     * @param clubId 클럽 ID
     * @return 클럽 채팅방 정보
     */
    public ChatRoom getOrCreateClubChatRoom(int clubId) {
        // 클럽 정보 조회
        ClubBean club = clubService.oneClubInfo(clubId);
        if (club == null) {
            throw new RuntimeException("클럽 정보를 찾을 수 없습니다.");
        }

        // 클럽 멤버 목록 조회
        List<ClubMemberBean> clubMembers = clubMemberService.getPendingMembers(clubId);

        // 멤버 ID 목록 추출
        List<String> memberIds = clubMembers.stream()
                .map(ClubMemberBean::getMember_id)
                .collect(Collectors.toList());

        // 채팅방 생성 또는 접근
        return chatService.getOrCreateClubChatRoom(clubId, club.getClub_name(), memberIds);
    }

    /**
     * 새 멤버가 클럽에 가입할 때 채팅방에 추가
     * @param clubId 클럽 ID
     * @param memberId 멤버 ID
     */
    public void addMemberToClubChat(int clubId, String memberId) {
        // 클럽 채팅방 조회
        ChatRoom chatRoom = chatService.getClubChatRoomByClubId(clubId);

        if (chatRoom != null) {
            // 참여자 목록에 새 멤버 추가
            List<String> participants = chatRoom.getParticipants();
            if (!participants.contains(memberId)) {
                participants.add(memberId);

                // 채팅방 정보 업데이트
                chatService.updateChatRoomParticipants(chatRoom.getId(), participants);

                log.info("클럽 채팅방 [{}]에 멤버 [{}] 추가됨", chatRoom.getRoomName(), memberId);
            }
        }
    }

    /**
     * 멤버가 클럽에서 탈퇴할 때 채팅방에서 제거
     * @param clubId 클럽 ID
     * @param memberId 멤버 ID
     */
    public void removeMemberFromClubChat(int clubId, String memberId) {
        // 클럽 채팅방 조회
        ChatRoom chatRoom = chatService.getClubChatRoomByClubId(clubId);

        if (chatRoom != null) {
            // 참여자 목록에서 멤버 제거
            List<String> participants = chatRoom.getParticipants();
            if (participants.contains(memberId)) {
                participants.remove(memberId);

                // 채팅방 정보 업데이트
                chatService.updateChatRoomParticipants(chatRoom.getId(), participants);

                log.info("클럽 채팅방 [{}]에서 멤버 [{}] 제거됨", chatRoom.getRoomName(), memberId);
            }
        }
    }
}