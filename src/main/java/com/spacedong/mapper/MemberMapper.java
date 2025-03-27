package com.spacedong.mapper;

import com.spacedong.beans.BoardBean;
import com.spacedong.beans.ClubBean;
import org.apache.ibatis.annotations.*;

import com.spacedong.beans.MemberBean;

import java.util.List;

@Mapper
public interface MemberMapper {

    @Insert("INSERT INTO member (member_id, member_pw, member_name, member_email, member_phone, member_address, member_joindate," +
            " member_nickname, member_personality, member_point, member_gender, member_birthdate) " +
            "VALUES (#{member_id}, #{member_pw}, #{member_name}, #{member_email}, #{member_phone}, #{member_address}, sysdate," +
            " #{member_nickname}, NVL(#{member_personality}, 0), NVL(#{member_point}, 0), #{member_gender}, #{member_birthdate})")
    void signupMember(MemberBean memberBean);

    @Select("SELECT member_id, member_pw, member_name, member_email, member_phone, member_address, member_nickname, " +
            "member_gender, member_birthdate, member_profile, member_point, member_personality, member_joindate " +
            "FROM member " +
            "WHERE member_id = #{member_id} " +
            "AND member_pw = #{member_pw}")
    MemberBean getLoginMember(MemberBean tempLoginMember);

    @Select("SELECT COUNT(*) FROM member WHERE member_id = #{member_id}")
    int checkId(String member_id);

    @Select("SELECT COUNT(*) FROM member WHERE member_nickname = #{member_nickname}")
    int checkNickname(String member_nickname);

    // ID로 회원 정보 조회 (닉네임 중복체크용)
    @Select("SELECT * FROM member WHERE member_id = #{member_id}")
    MemberBean getMemberById(@Param("member_id") String member_id);

//	@Insert("insert into member(member_id, sns_id, member_name, member_email, member_phone, sns_type) values(#{member_id}, #{sns_id}, #{member_name}, #{member_email}, #{member_phone}, 'naver')")
//	public void naverLogin(MemberBean memberBean);


    @Select("select * from member where sns_id = #{sns_id} ")
    MemberBean getMemberBySnsId(@Param("sns_id") String sns_id);

    @Update("update member set member_email = #{member_email}, member_name = #{member_name}, member_phone = #{member_phone}  WHERE sns_id = #{sns_id}")
    public void updateMember(MemberBean memberBean);

    @Insert("insert into member(sns_id, member_id, member_name, member_email, member_phone, sns_type, member_nickname, member_personality, member_gender, member_birthdate) values(#{member_id}, #{sns_id}, #{member_name}, #{member_email}, #{member_phone}, 'naver','user' || TO_CHAR(member_nickname_seq.NEXTVAL), 0, #{member_gender}, #{member_birthdate})")
    public void naverSignUp(MemberBean memberBean);

    @Update("UPDATE member SET member_nickname = #{member_nickname}, " +
            "member_phone = #{member_phone}, member_address = #{member_address} " +
            "WHERE member_id = #{member_id}")
    void editMember(MemberBean member);

    @Select("SELECT member_pw FROM member WHERE member_id = #{member_id}")
    String getPasswordById(@Param("member_id") String memberId);

    @Update("UPDATE member SET member_pw = #{newPassword} WHERE member_id = #{member_id}")
    void updatePassword(@Param("member_id") String memberId, @Param("newPassword") String newPassword);


    // 🔹 회원 성격 유형 업데이트 (member_personality 변경)
    @Update("UPDATE member SET member_personality = #{personality} WHERE member_id = #{memberId}")
    void savePersonality(@Param("personality") int personality, @Param("memberId") String memberId);

    @Delete("DELETE FROM member WHERE member_id = #{member_id}")
    void deleteMember(String member_id);

    /** ✅ 프로필 이미지 업데이트 */
    @Update("UPDATE member SET member_profile = #{fileName} WHERE member_id = #{memberId}")
    void updateMemberProfile(@Param("memberId") String memberId, @Param("fileName") String fileName);

    /** ✅ 가입한 클럽 목록 조회 (회장 여부 포함) */
    @Select("SELECT c.club_id, c.club_name, " +
            "       NVL(cm.member_role, 'normal') AS member_role " +
            "FROM club c " +
            "LEFT JOIN club_member cm ON c.club_id = cm.club_id " +
            "WHERE cm.member_id = #{memberId} " +
            "AND c.club_public = 'PASS'")
    List<ClubBean> getJoinedClubsWithRole(String memberId);


    // 사용자가 작성한 게시글 조회
    @Select("SELECT * FROM member_board WHERE board_writer_id = #{memberId} ORDER BY create_date DESC")
    List<BoardBean> getUserPosts(String memberId);

    @Select("SELECT * " +
            "FROM member " +
            "WHERE member_id = #{memberId}")
    MemberBean selectMemberById(String memberId);

    @Select("select * from member where member_id LIKE '%'||#{keyword}||'%' OR member_nickname LIKE '%'||#{keyword}||'%'")
    List<MemberBean> searchUsersByKeyword(String keyword);

    //회원 포인트 조회
    @Select("SELECT member_point FROM member WHERE member_id = #{memberId}")
    Integer getMemberPoint(String memberId);


    //회원 포인트 업데이트
    @Update("UPDATE member SET member_point = member_point + #{points} WHERE member_id = #{memberId}")
    int updateMemberPoints(@Param("memberId") String memberId, @Param("points") Integer points);





    // 전체 회원 목록 조회
    @Select("SELECT * FROM member ORDER BY member_joindate DESC")
    List<MemberBean> getAllMembers();

    // ID로 회원 검색
    @Select("SELECT * FROM member WHERE member_id LIKE '%' || #{keyword} || '%' ORDER BY member_joindate DESC")
    List<MemberBean> searchMembersById(@Param("keyword") String keyword);

    // 이름으로 회원 검색
    @Select("SELECT * FROM member WHERE member_name LIKE '%' || #{keyword} || '%' ORDER BY member_joindate DESC")
    List<MemberBean> searchMembersByName(@Param("keyword") String keyword);

    // 이메일로 회원 검색
    @Select("SELECT * FROM member WHERE member_email LIKE '%' || #{keyword} || '%' ORDER BY member_joindate DESC")
    List<MemberBean> searchMembersByEmail(@Param("keyword") String keyword);

    // 전화번호로 회원 검색
    @Select("SELECT * FROM member WHERE member_phone LIKE '%' || #{keyword} || '%' ORDER BY member_joindate DESC")
    List<MemberBean> searchMembersByPhone(@Param("keyword") String keyword);

    // 모든 필드로 회원 검색
    @Select("SELECT * FROM member WHERE " +
            "member_id LIKE '%' || #{keyword} || '%' OR " +
            "member_name LIKE '%' || #{keyword} || '%' OR " +
            "member_email LIKE '%' || #{keyword} || '%' OR " +
            "member_phone LIKE '%' || #{keyword} || '%' OR " +
            "member_nickname LIKE '%' || #{keyword} || '%' " +
            "ORDER BY member_joindate DESC")
    List<MemberBean> searchMembersByAllFields(@Param("keyword") String keyword);
    @Select("SELECT COUNT(*) FROM member")
    int getMemberCount();
    @Select("SELECT COUNT(*) FROM member WHERE TRUNC(member_joindate, 'MM') < TRUNC(SYSDATE, 'MM')")
    int getPreviousMonthMemberCount();
    @Select("SELECT COUNT(*) FROM member WHERE EXTRACT(MONTH FROM member_joindate) = #{month} AND EXTRACT(YEAR FROM member_joindate) = EXTRACT(YEAR FROM SYSDATE)")
    int getMemberCountByMonth(int month);

    @Select("select member_id from member where member_phone = #{member_phone}")
    String findByPhone(String member_phone);

    @Select("Select * from member where member_id = #{member_id} and member_phone = #{member_phone}")
    MemberBean findMemberByIdAndPhone(@Param("member_id") String member_id,@Param("member_phone") String member_phone);

    @Update("UPDATE member SET member_pw = #{newPassword} WHERE member_id = #{member_id}")
    void resetPw(@Param("member_id") String member_id,@Param("newPassword") String newPassword);


}
