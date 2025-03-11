package com.spacedong.repository;

import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.spacedong.beans.Category;
import com.spacedong.mapper.CategoryMapper;

@Repository
public class CategoryRepository {

   @Autowired
   public CategoryMapper categoryMapper;

   // 카테고리 리스트
   public List<Category> categoryType() {

      List<Category> list = categoryMapper.categoryType();

      return list;
   }

        public List<Category> categoryInfo(String type) {
      List<Category> list = categoryMapper.categoryInfo(type);
      return list;

   }

}
