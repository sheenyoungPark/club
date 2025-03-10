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
   
}