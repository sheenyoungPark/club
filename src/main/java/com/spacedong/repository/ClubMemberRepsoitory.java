package com.spacedong.repository;

import com.spacedong.beans.ClubMemberBean;
import com.spacedong.mapper.ClubMapper;
import com.spacedong.mapper.ClubMemberMapper;
import com.spacedong.service.ClubMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClubMemberRepsoitory {

    @Autowired
    private ClubMemberMapper clubMemberMapper;


    //클럽 직급 확인
    public ClubMemberBean getMemberInfo(int club_id, String member_id){
        return clubMemberMapper.getMemberInfo(club_id,member_id);
    }

    public List<ClubMemberBean> getPendingMembers(int club_id){
        return clubMemberMapper.getPendingMembers(club_id);
    }

    public void UpdateMemberRole(int club_id, String member_id){
        clubMemberMapper.UpdateMemberRole(club_id,member_id);
    }
    public void deleteMember(int club_id, String member_id){
        clubMemberMapper.deleteMember(club_id, member_id);
    }




}

