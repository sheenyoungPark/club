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

    //사업자의 상품 ID 목록 조회
    public List<Integer> getItemIdsByBusinessId(String business_id){
        return itemRepository.getItemIdsByBusinessId(business_id);
    }

    //상품 번호로 정보 조회
    public BusinessItemBean getItemById(String item_id){
        return itemRepository.getItemById(item_id);
    }

    //아이템 리스트를 랜덤 순으로
    public List<BusinessItemBean> randomItemList(){
        return itemRepository.randomItemList();
    }

}
