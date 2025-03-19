package com.spacedong.mapper;

import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.ClubBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchMapper {


            @Select("SELECT * FROM club " +
                    "WHERE club_public = 'PASS' " +
                    "AND club_region LIKE '%' || #{region} || '%' "+
                    "AND club_agemin <= #{age} " +
                    "AND (club_name LIKE '%' || #{searchtxt} || '%' OR club_info LIKE '%' || #{searchtxt} || '%')")
    List<ClubBean> searchedClubs(@Param("region") String region, @Param("age") int age, @Param("searchtxt") String searchtxt);


    // age 매개변수 제거
    @Select("SELECT bi.*, b.business_name, b.business_address " +
            "FROM business_item bi " +
            "JOIN business b ON bi.business_id = b.business_id " +
            "WHERE b.business_public = 'WAIT' " +
            "AND b.business_address LIKE '%' || #{region} || '%' " +
            "AND (bi.item_title LIKE '%' || #{searchtxt} || '%' OR bi.item_text LIKE '%' || #{searchtxt} || '%')")
    List<BusinessItemBean> searchedBusinessItems(@Param("region") String region, @Param("searchtxt") String searchtxt);
}
