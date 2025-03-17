package com.spacedong.mapper;

import com.spacedong.beans.BusinessBean;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface BusinessMapper {


    @Insert("insert into business(business_id, business_pw, business_name, business_email, business_phone, business_address,business_number, business_joindate, business_public) "
            + "values(#{business_id}, #{business_pw}, #{business_name}, #{business_email}, #{business_phone}, #{business_address}, #{business_number}, sysdate,'WAIT')")
    public void businessJoin(BusinessBean businessBean);

    @Select("select * from business where business_id = #{business_id} and business_pw = #{business_pw}")
    BusinessBean getLoginMember(BusinessBean businessBean);

    // 아이디 중복 검사
    @Select("SELECT COUNT(*) FROM business WHERE business_id = #{business_id}")
    int checkId(String business_id);

    @Select("SELECT COUNT(*) FROM business WHERE business_email = #{business_email}")
    int checkEmail(String business_email);

}
