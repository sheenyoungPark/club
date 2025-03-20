package com.spacedong.service;

import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.repository.ItemRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {


    @Autowired
    private ItemRepository itemRepository;


    public List<BusinessItemBean> getAllItems(){
        return itemRepository.getAllItems();
    }

    //대분류별 아이템
    public List<BusinessItemBean> getItemByCategory(@Param("categoryType")String categoryType){
        return itemRepository.getItemByCategory(categoryType);
    }

    //소분류별 아이템
    public List<BusinessItemBean> getItemBySubCategory(@Param("subCategoryName") String subCategoryName){
        return itemRepository.getItemBySubCategory(subCategoryName);
    }


}
