package com.spacedong.service;

import java.util.ArrayList;
import java.util.List;

import com.spacedong.beans.*;
import com.spacedong.mapper.ClubMapper;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spacedong.repository.ClubRepository;

@Service
public class ClubService {

    @Resource(name = "loginMember")
    private MemberBean loginMemeber;
    
    @Autowired
    private ClubRepository clubRepository;


    /**
     * 모든 동호회 목록을 조회
     * @return 전체 동호회 목록
     */
    public List<ClubBean> getAllClub() {
        return clubRepository.getAllClub();
    }
    
    /**
     * 특정 메인 카테고리에 속한 동호회 목록 조회
     * @param categoryType 메인 카테고리 타입
     * @return 해당 메인 카테고리에 속한 동호회 목록
     */
    public List<ClubBean> getClubsByCategory(String categoryType) {
        return clubRepository.getClubsByCategory(categoryType);
    }
    
    /**
     * 특정 서브 카테고리에 속한 동호회 목록 조회
     * @param subCategoryName 서브 카테고리 이름
     * @return 해당 서브 카테고리에 속한 동호회 목록
     */
    public List<ClubBean> getClubsBySubCategory(String subCategoryName) {
        return clubRepository.getClubsBySubCategory(subCategoryName);
    }
    
    public ClubBean oneClubInfo(int club_id) {
        return clubRepository.oneClubInfo(club_id);
    }
    
    /**
     * 동호회 검색
     * @param searchType 검색 타입 (name, category)
     * @param keyword 검색 키워드
     * @return 검색된 동호회 목록
     */
    public List<ClubBean> searchClubs(String searchType, String keyword) {
        // 검색어가 비어있으면 전체 목록 반환
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllClub();
        }
        
        return clubRepository.searchClubs(searchType, keyword);
    }
    
    /**
     * 동호회 상태 업데이트
     * @param club_id 동호회 ID
     * @param status 변경할 상태 (public, private, wait)
     */
    public void updateClubStatus(int club_id, String status) {
        clubRepository.updateClubStatus(club_id, status);
    }

    public List<Category> countCategory(){
        return clubRepository.countCategory();
    }

    public List<ClubBean> countClub(){
        return clubRepository.countClub();
    }

    //이미 해당 클럽에 가입했는지 확인
    public ClubMemberBean getClubMember(@Param("club_id") int club_id, @Param("member_id") String member_id){
        return clubRepository.getClubMember(club_id, member_id);
    }

    //클럽에 가입
    public void join_club(@Param("club_id") int club_id, @Param("member_id") String member_id){
        clubRepository.join_club(club_id, member_id);
    }

    //클럽 생성시 역할 회장
    public void create_join_club(@Param("club_id") int club_id,@Param("member_id") String member_id){
        clubRepository.create_join_club(club_id, member_id);
    }

    //클럽 생성
    public  void create_club(ClubBean clubBean){

        clubRepository.create(clubBean);

    }
    //클럽 이름으로 클럽 객체 찾기
    public ClubBean searchClubName(String club_name){
        return clubRepository.searchClubName(club_name);
    }

    // ✅ 특정 동호회의 게시글 목록 조회
    public List<ClubBoardBean> getBoardListByClubId(int club_id) {
        return clubRepository.getBoardListByClubId(club_id);
    }

    public boolean isMemberOfClub(int club_id, String member_id) {
        if (member_id == null) {
            return false;
        }
        return clubRepository.checkMemberInClub(club_id, member_id);
    }

    /**
     * ✅ 게시글 작성
     * @param clubBoardBean 게시글 객체
     */
    public void createBoard(ClubBoardBean clubBoardBean) {
        clubRepository.createBoard(clubBoardBean);
    }

    /**
     * ✅ 게시글의 이미지 저장
     * @param board_id 게시글 ID
     * @param imagePath 이미지 경로
     */
    public void saveBoardImage(int board_id, String imagePath) {
        clubRepository.updateBoardImage(board_id, imagePath);
    }



}