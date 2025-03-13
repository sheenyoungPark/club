package com.spacedong.mapper;

import java.util.ArrayList;
import java.util.List;

import com.spacedong.beans.Category;
import com.spacedong.beans.ClubMemberBean;
import com.spacedong.repository.ClubRepository;
import org.apache.ibatis.annotations.*;

import com.spacedong.beans.ClubBean;

@Mapper
public interface ClubMapper {


    /**
     * 모든 동호회 목록을 조회
     * @return 전체 동호회 목록
     */
    @Select("SELECT * FROM club")
    List<ClubBean> getAllClub();
    
    /**
     * 특정 메인 카테고리에 속한 동호회 목록 조회
     * @param categoryType 메인 카테고리 타입
     * @return 해당 메인 카테고리에 속한 동호회 목록
     */
    @Select("SELECT * FROM club, category WHERE category_type = #{categoryType} and category.category_name = club.club_category")
    List<ClubBean> getClubsByCategory(@Param("categoryType") String categoryType);
    
    /**
     * 특정 서브 카테고리에 속한 동호회 목록 조회
     * @param subCategoryName 서브 카테고리 이름
     * @return 해당 서브 카테고리에 속한 동호회 목록
     */
    @Select("SELECT * FROM club, category WHERE category_Name = #{subCategoryName} and category.category_name = club.club_category")
    List<ClubBean> getClubsBySubCategory(@Param("subCategoryName") String subCategoryName);

    //club_id 별로 조회
    @Select("select * from club where club_id = #{club_id}")
    public ClubBean oneClubInfo(@Param("club_id") int club_id);
    
    /**
     * 동호회명으로 검색
     * @param keyword 검색 키워드
     * @return 검색된 동호회 목록
     */
    @Select("SELECT * FROM club WHERE club_name LIKE '%' || #{keyword} || '%'")
    List<ClubBean> searchClubsByName(@Param("keyword") String keyword);


    /**
     * 카테고리로 검색
     * @param keyword 검색 키워드
     * @return 검색된 동호회 목록
     */
    @Select("SELECT * FROM club WHERE club_category LIKE '%' || #{keyword} || '%'")
    List<ClubBean> searchClubsByCategory(@Param("keyword") String keyword);
    
    /**
     * 상태 업데이트
     * @param club_id 동호회 ID
     * @param status 변경할 상태
     */
    @Update("UPDATE club SET club_public = #{status} WHERE club_id = #{club_id}")
    void updateClubStatus(@Param("club_id") int club_id, @Param("status") String status);

    /*
    * 같은 카테고리 숫자별 카운트
    * */
    @Select("SELECT ca.category_name,ca.category_type, COUNT(*) AS category_count FROM club c JOIN category ca ON c.club_category = ca.category_name GROUP BY ca.category_type, ca.category_name ORDER BY category_count DESC")
    List<Category> countCategory();

    //클럽별 인원수 순서
    @Select("select cm.club_id, c.club_name , count(*) as club_count from club_member cm join club c on c.club_id = cm.club_id group by cm.club_id, c.club_name order by cm.club_id")
    List<ClubBean> countClub();

    //이미 해당 클럽에 가입했는지 확인
    @Select("select  * from club_member where club_id = #{club_id} and member_id = #{member_id}")
    ClubMemberBean getClubMember(@Param("club_id") int club_id,@Param("member_id") String member_id);

    //클럽에 멤버 가입
    @Insert("insert into club_member(club_id, member_id, member_joinDate, member_role) values (#{club_id}, #{member_id}, sysdate, 'normal')")
    void join_club(@Param("club_id") int club_id,@Param("member_id") String member_id);

    //클럽 생성시 역할 회장
    @Insert("insert into club_member(club_id, member_id, member_joinDate, member_role) values (#{club_id}, #{member_id}, sysdate, 'master')")
    void create_join_club(@Param("club_id") int club_id,@Param("member_id") String member_id);

    // 클럽 생성 (프로필 이미지 추가됨)
    @Insert("INSERT INTO club (club_id, club_name, club_info, club_joindate, club_point, club_category, club_profile, club_public, club_agemin, club_region) " +
            "VALUES (club_id_seq.nextval, #{club_name}, #{club_info}, SYSDATE, 0, #{club_category}, #{club_profile, jdbcType=VARCHAR}, 'WAIT', #{club_agemin}, #{club_region})")
    void create(ClubBean clubBean);


    //동호회 명으로 클럽 객체 찾기
    @Select("select * from club where club_name = #{club_name}")
    ClubBean searchClubName(String club_name);



}