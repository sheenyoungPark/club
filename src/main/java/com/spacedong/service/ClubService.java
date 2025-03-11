package com.spacedong.service;

import java.util.ArrayList;
import java.util.List;

import com.spacedong.beans.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spacedong.beans.ClubBean;
import com.spacedong.repository.ClubRepository;

@Service
public class ClubService {
    
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
}