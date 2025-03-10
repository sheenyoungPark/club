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
   
   
   
}