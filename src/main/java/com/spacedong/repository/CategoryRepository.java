package com.spacedong.repository;

import java.util.List;

import com.spacedong.mapper.ClubMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.CategoryBean;
import com.spacedong.mapper.CategoryMapper;

@Repository
public class CategoryRepository {

   @Autowired
   public CategoryMapper categoryMapper;
    @Autowired
    private ClubMapper clubMapper;

   // 카테고리 리스트
   public List<CategoryBean> categoryType() {

      List<CategoryBean> list = categoryMapper.categoryType();

      return list;
   }

        public List<CategoryBean> categoryInfo(String type) {
      List<CategoryBean> list = categoryMapper.categoryInfo(type);
      return list;
   }

   public List<String> getAllCategoryType(){
      return categoryMapper.getAllCategoryType();
   }
   //겹치는거 제외 카테고리
   public List<CategoryBean> categoryList(){
      return categoryMapper.categoryList();
   }

   //소분류로 대분류 찾기
   public CategoryBean getCategoryByName(String category_name){
      return categoryMapper.getCategoryByName(category_name);
   }

   //대분류별 인원 많은순
   public List<CategoryBean> categoryTypeCount(){
      return categoryMapper.categoryTypeCount();
   }

   public List<CategoryBean> getCategoryTypeCount() {
      return categoryMapper.categoryTypeCount();
   }


}
