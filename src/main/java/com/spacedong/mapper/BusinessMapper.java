package com.spacedong.mapper;

import com.spacedong.beans.BusinessBean;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface BusinessMapper {


    @Insert("insert into business(business_id, business_pw, business_name, business_email, business_phone, business_address, business_joindate, business_public) "
            + "values(#{business_id}, #{business_pw}, #{business_name}, #{business_email}, #{business_phone}, #{business_address}, sysdate,'WAIT')")
    public void businessJoin(BusinessBean businessBean);

    @Select("select * from business where business_id = #{business_id} and business_pw = #{business_pw}")
    BusinessBean getLoginMember(BusinessBean businessBean);

}
