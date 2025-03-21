package com.spacedong.repository;

import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.mapper.ItemMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ItemRepository {

    @Autowired
    private ItemMapper itemMapper;

    public List<BusinessItemBean> getAllItems(){
        return itemMapper.getAllItems();
    }
    
    //대분류별 아이템
    public List<BusinessItemBean> getItemByCategory(@Param("categoryType")String categoryType){
        return itemMapper.getItemByCategory(categoryType);
    }

    //소분류별 아이템
    public List<BusinessItemBean> getItemBySubCategory(@Param("subCategoryName") String subCategoryName){
        return itemMapper.getItemBySubCategory(subCategoryName);
    }

    //사업자의 상품 ID 목록 조회
    public List<Integer> getItemIdsByBusinessId(String business_id){
        return itemMapper.getItemIdsByBusinessId(business_id);
    }

    //상품 번호로 정보 조회
    public BusinessItemBean getItemById(String item_id){
        return itemMapper.getItemById(item_id);
    }







}
