package com.spacedong.controller;

import com.spacedong.beans.*;
import com.spacedong.repository.ChatRepository;
import com.spacedong.service.AdminNotificationService;
import com.spacedong.service.ChatService;
import com.spacedong.service.ClubMemberService;
import com.spacedong.service.MemberService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/club")
public class ClubMemberController {

    @Resource(name = "loginMember")
    private MemberBean loginMember;
    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private AdminNotificationService adminNotificationService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private ChatRepository chatRepository;

    // 대기 중인 회원 목록 페이지
    @GetMapping("pending")
    public String pendingMembers(@RequestParam("club_id") int clubId, Model model) {
        // 현재 로그인한 사용자 ID 가져오기
        String loggedInMemberId = loginMember.getMember_id();

        // 사용자가 해당 클럽의 마스터인지 확인
        ClubMemberBean currentMember = clubMemberService.getMemberInfo(clubId, loggedInMemberId);

        if (currentMember == null || !currentMember.getMember_role().equals("master")) {
            return "redirect:/club/info?club_id=" + clubId + "&error=unauthorized";
        }
        // 대기 중인 회원 목록 가져오기
        List<ClubMemberBean> pendingMembers = clubMemberService.getPendingMembers(clubId);
        model.addAttribute("pendingMembers", pendingMembers);
        model.addAttribute("club_id", clubId);
        for(ClubMemberBean s : pendingMembers){
            System.out.println("!  : " +  s.getMember_nickname());
        }

        return "club/club_member_pending";
    }

    @PostMapping("/approve")
    public String approveMembers(
            @RequestParam("club_id") int clubId,
            @RequestParam("member_ids") String[] memberIds) {

        // 현재 로그인한 사용자 ID 가져오기
        String loggedInMemberId = loginMember.getMember_id();

        // 사용자가 해당 클럽의 마스터인지 확인
        ClubMemberBean currentMember = clubMemberService.getMemberInfo(clubId, loggedInMemberId);

        if (currentMember == null || !currentMember.getMember_role().equals("master")) {
            return "redirect:/club/info?club_id=" + clubId + "&error=unauthorized";
        }

        // 선택된 회원들 승인 처리
        for (String memberId : memberIds) {
            // 회원 승인 처리
            clubMemberService.approveMember(clubId, memberId);

            // 승인 알림 전송
            adminNotificationService.sendApprovalNotification(memberId, "MEMBER", "APPROVED",
                    "클럽 가입 신청", "");

            // 해당 클럽의 채팅방 조회
            List<ChatRoomBean> clubRooms = chatService.getChatRoomsByClubId((long)clubId);
            if (!clubRooms.isEmpty()) {
                // 채팅방이 존재하면 첫 번째 채팅방에 회원 추가
                ChatRoomBean chatRoom = clubRooms.get(0);

                // 회원 정보 조회
                MemberBean member = memberService.getMemberById(memberId);
                if (member != null) {
                    // 참가자 객체 생성
                    ChatParticipantBean participant = new ChatParticipantBean();
                    participant.setRoom_id(chatRoom.getRoom_id());
                    participant.setUser_id(memberId);
                    participant.setUser_type("MEMBER");
                    participant.setUser_nickname(member.getMember_nickname());
                    participant.setUserProfile(member.getMember_profile());
                    participant.setJoinDate(LocalDateTime.now());

                    // 이미 참가자인지 확인 (중복 방지)
                    ChatParticipantBean existingParticipant =
                            chatRepository.getParticipant(chatRoom.getRoom_id(), memberId);

                    if (existingParticipant == null) {
                        // 참가자 추가
                        chatRepository.addParticipant(participant);

                        // 중요: 채팅방의 모든 메시지 조회
                        List<ChatMessageBean> messages = chatService.getRoomMessages((long)chatRoom.getRoom_id(), memberId);

                        // 메시지가 있는 경우만 읽음 처리
                        if (messages != null && !messages.isEmpty()) {
                            // 이전 메시지 읽음 처리
                            chatService.markPreviousMessagesAsRead(chatRoom.getRoom_id(), memberId);
                        } else {
                            // 메시지가 없는 경우, 로그에 기록 (필요시)
                            System.out.println("클럽 채팅방 " + chatRoom.getRoom_id() + "에 메시지가 없습니다. 읽음 처리가 필요하지 않습니다.");
                        }

                        // 마지막 읽은 메시지 ID 업데이트 (메시지가 없더라도 현재 상태를 기록)
                        // 이렇게 하면 새 메시지가 올 때만 unreadCount가 증가함
                        chatRepository.updateLastReadMsgId((long)chatRoom.getRoom_id(), memberId, 0L);
                    }
                }
            }
        }

        return "redirect:/club/pending?club_id=" + clubId + "&success=approved";
    }

    // 회원 거부 처리
    @PostMapping("/reject")
    public String rejectMembers(
            @RequestParam("club_id") int clubId,
            @RequestParam("member_ids") String[] memberIds) {

        // 현재 로그인한 사용자 ID 가져오기
        String loggedInMemberId = loginMember.getMember_id();

        // 사용자가 해당 클럽의 마스터인지 확인
        ClubMemberBean currentMember = clubMemberService.getMemberInfo(clubId, loggedInMemberId);


        if (currentMember == null || !currentMember.getMember_role().equals("master")) {
            return "redirect:/club/info?club_id=" + clubId + "&error=unauthorized";
        }

        // 선택된 회원들 거부 처리 (삭제)
        for (String memberId : memberIds) {
            clubMemberService.deleteMember(clubId, memberId);
            adminNotificationService.sendApprovalNotification(memberId, "MEMBER", "REJECTED1",
                    "클럽 가입 신청", "");

        }

        return "redirect:/club/club_info?club_id=" + clubId;
    }

    @GetMapping("/leaveClub")
    public String leaveClub(@RequestParam("club_id") int clubId, Model model) {
        String loggedInMemberId = loginMember.getMember_id();
        clubMemberService.deleteMember(clubId, loggedInMemberId);
        return "redirect:/club/club_info?club_id=" + clubId;
    }
}
