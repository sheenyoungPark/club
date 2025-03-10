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
	
}
