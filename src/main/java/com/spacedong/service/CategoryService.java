package com.spacedong.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spacedong.beans.Category;
import com.spacedong.repository.CategoryRepository;

@Service
public class CategoryService {

   @Autowired
   private CategoryRepository categoryRepository;

   public List<Category> categoryType() {

      List<Category> list = categoryRepository.categoryType();

      return list;
   }

   public List<Category> categoryInfo(String type) {
      List<Category> list = categoryRepository.categoryInfo(type);
      return list;

   }

   public List<String> getAllCategoryType(){
      return categoryRepository.getAllCategoryType();
   }

   //겹치는거 제외 카테고리
   public List<Category> categoryList(){
      return categoryRepository.categoryList();
   }

   //소분류로 대분류 찾기
   public Category getCategoryByName(String category_name){
      return categoryRepository.getCategoryByName(category_name);
   }

   //대분류별 인원 많은순
   public List<Category> categoryTypeCount(){
      return categoryRepository.categoryTypeCount();
   }
   
   
}