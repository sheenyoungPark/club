package com.spacedong.mapper;

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
}
