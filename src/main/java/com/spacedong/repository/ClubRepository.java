package com.spacedong.repository;

import java.util.ArrayList;
import java.util.List;

import com.spacedong.beans.Category;
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
}