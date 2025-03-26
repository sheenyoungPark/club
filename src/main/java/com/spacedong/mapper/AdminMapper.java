package com.spacedong.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.spacedong.beans.AdminBean;
import com.spacedong.beans.ClubBean;

@Mapper
public interface AdminMapper {

	@Select("select * from admin where admin_id=#{admin_id} and admin_pw=#{admin_pw}")
	AdminBean getLoginAdmin(AdminBean admin);
	
	@Select("select * from club where club_public='WAIT'")
	List<ClubBean> getWaitClub();

	@Select("SELECT * FROM admin WHERE admin_id LIKE '%'||#{keyword}||'%' OR admin_name LIKE '%'||#{keyword}||'%'")
	List<AdminBean> searchAdminsByKeyword(String keyword);

	@Select("SELECT * FROM admin WHERE admin_id=#{adminId}")
	AdminBean getAdminById(String adminId);

	@Select("SELECT COUNT(*) FROM club")
	int getClubCount();

	@Select("SELECT COUNT(*) FROM club WHERE TRUNC(club_joindate, 'MM') < TRUNC(SYSDATE, 'MM')")
	int getPreviousMonthClubCount();
	
}
