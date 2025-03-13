package com.spacedong.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.spacedong.beans.Category;

@Mapper
public interface CategoryMapper {

   @Select("SELECT DISTINCT category_type FROM category")
   public List<Category> categoryType();
   
   @Select("select * from category where category_type = #{type}")
   public List<Category> categoryInfo(String type);

   @Select("SELECT DISTINCT category_type FROM category")
   public List<String> getAllCategoryType();

   //겹치는거 제외 카테고리
   @Select("select distinct * from category")
   public List<Category> categoryList();

   @Select("select * from category where category_name = #{category_name}")
   public Category getCategoryByName(String category_name);

   //대분류별 인원 많은순
   @Select("SELECT c.category_type,  COUNT(cm.member_id) AS member_count FROM  category c JOIN club cl ON c.category_name = cl.club_category JOIN club_member cm ON cl.club_id = cm.club_id GROUP BY c.category_type ORDER BY member_count DESC")
   public List<Category> categoryTypeCount();

}