package com.spacedong.repository;

import java.util.Iterator;
import java.util.List;

import com.spacedong.mapper.ClubMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.Category;
import com.spacedong.mapper.CategoryMapper;

@Repository
public class CategoryRepository {

   @Autowired
   public CategoryMapper categoryMapper;
    @Autowired
    private ClubMapper clubMapper;

   // 카테고리 리스트
   public List<Category> categoryType() {

      List<Category> list = categoryMapper.categoryType();

      return list;
   }

        public List<Category> categoryInfo(String type) {
      List<Category> list = categoryMapper.categoryInfo(type);
      return list;
   }

   public List<String> getAllCategoryType(){
      return categoryMapper.getAllCategoryType();
   }
   //겹치는거 제외 카테고리
   public List<Category> categoryList(){
      return categoryMapper.categoryList();
   }

   //소분류로 대분류 찾기
   public Category getCategoryByName(String category_name){
      return categoryMapper.getCategoryByName(category_name);
   }


}
