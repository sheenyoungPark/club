package com.spacedong.service;

import com.spacedong.beans.ClubMemberBean;
import com.spacedong.repository.ClubMemberRepsoitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubMemberService {

    @Autowired
    private ClubMemberRepsoitory clubMemberRepsoitory;

    public ClubMemberBean getMemberInfo(int club_id, String member_id){
        return clubMemberRepsoitory.getMemberInfo(club_id, member_id);
    }

    public List<ClubMemberBean> getPendingMembers(int club_id){
        return clubMemberRepsoitory.getPendingMembers(club_id);
    }
    public void approveMember(int clubId, String memberId) {
        clubMemberRepsoitory.UpdateMemberRole(clubId, memberId);
    }
    public void deleteMember(int club_id, String member_id){
        clubMemberRepsoitory.deleteMember(club_id, member_id);
    }

    public List<ClubMemberBean> getClubMemberList(int club_id) {
        return clubMemberRepsoitory.getClubMemberList(club_id);
    }
}
