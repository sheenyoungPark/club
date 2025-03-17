package com.spacedong.repository;

import java.util.ArrayList;
import java.util.List;

import com.spacedong.beans.Category;
import com.spacedong.beans.ClubBoardBean;
import com.spacedong.beans.ClubMemberBean;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.ClubBean;
import com.spacedong.mapper.ClubMapper;

@Repository
public class ClubRepository {
   @Autowired
   private ClubMapper clubMapper;
   
   public List<ClubBean> getAllClub() {
      List<ClubBean> list;
      list = clubMapper.getAllClub();
      return list;
   }
   
   public List<ClubBean> getClubsByCategory(@Param("categoryType") String categoryType){
       return clubMapper.getClubsByCategory(categoryType);
   }
   
   // 선택적으로 필요할 수 있는 세부 카테고리별 동호회 조회 메소드
   public List<ClubBean> getClubsBySubCategory(String subCategoryName) {
       // 세부 카테고리명에 해당하는 동호회 반환
       return clubMapper.getClubsBySubCategory(subCategoryName);
   }
   
   public ClubBean oneClubInfo(int club_id) {
       return clubMapper.oneClubInfo(club_id);
   }
   
   // 동호회 검색 - 검색 타입에 따라 다른 메서드 호출
   public List<ClubBean> searchClubs(String searchType, String keyword) {
       switch (searchType) {
           case "name":
               return clubMapper.searchClubsByName(keyword);
           case "category":
               return clubMapper.searchClubsByCategory(keyword);
           default:
               return clubMapper.getAllClub();
       }
   }
   
   // 동호회 상태 업데이트
   public void updateClubStatus(int club_id, String status) {
       clubMapper.updateClubStatus(club_id, status);
   }

   public List<Category> countCategory(){
       return clubMapper.countCategory();
   }
   public List<ClubBean> countClub(){
       return clubMapper.countClub();
   }

   //이미 해당 클럽에 가입했는지 확인
   public ClubMemberBean getClubMember(@Param("club_id") int club_id, @Param("member_id") String member_id){
       return clubMapper.getClubMember(club_id, member_id);
   }

   //클럽에 가입
   public void join_club(@Param("club_id") int club_id,@Param("member_id") String member_id){
       clubMapper.join_club(club_id, member_id);
   }

   //클럽 생성시 역할 회장
   public void create_join_club(@Param("club_id") int club_id,@Param("member_id") String member_id){
       clubMapper.create_join_club(club_id, member_id);
   }
   
   //클럽 생성
   public  void create(ClubBean clubBean){
       clubMapper.create(clubBean);
   }

    //클럽 이름으로 클럽 객체 찾기
    public ClubBean searchClubName(String club_name){
       return clubMapper.searchClubName(club_name);
    }

    public List<ClubBoardBean> getBoardListByClubId(int club_id) {
        return clubMapper.getBoardListByClubId(club_id);
    }


    // ✅ 게시글 작성
    public void createBoard(ClubBoardBean clubBoardBean) {
        clubMapper.insertBoard(clubBoardBean);
    }

    // ✅ 게시글의 이미지 저장
    public void updateBoardImage(int board_id, String imagePath) {
        clubMapper.updateBoardImage(board_id, imagePath);
    }

    // ✅ 사용자가 특정 동호회의 회원인지 확인
    public boolean checkMemberInClub(int club_id, String member_id) {
        return clubMapper.checkMemberInClub(club_id, member_id) > 0;
    }

}