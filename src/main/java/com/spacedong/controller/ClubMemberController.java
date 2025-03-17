package com.spacedong.controller;

import com.spacedong.beans.ClubMemberBean;
import com.spacedong.beans.MemberBean;
import com.spacedong.service.ClubMemberService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/club")
public class ClubMemberController {

    @Resource(name = "loginMember")
    private MemberBean loginMember;
    @Autowired
    private ClubMemberService clubMemberService;

    // 대기 중인 회원 목록 페이지
    @GetMapping("/pending")
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

        return "club/club_member_pending";
    }

    // 회원 승인 처리
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
            clubMemberService.approveMember(clubId, memberId);
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
        }

        return "redirect:/club/pending?club_id=" + clubId + "&success=rejected";
    }


}
