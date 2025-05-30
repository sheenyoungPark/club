package com.spacedong.mapper;

import java.util.List;
import java.util.Map;

import com.spacedong.beans.ClubBean;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.spacedong.beans.BusinessItemBean;

@Mapper
public interface ItemMapper {
	
	//item 등록
	@Insert("insert into business_items values(#{business_id}, #{business_item}, #{item_img})")
	void additem(BusinessItemBean items);


	//모든 아이템
	@Select("select * from business_item")
	List<BusinessItemBean> getAllItems();

	@Select("SELECT i.item_title, b.business_name " +
			"FROM business_item i " +
			"JOIN business b ON i.business_id = b.business_id " +
			"WHERE i.item_id = #{itemId}")
	Map<String, String> getItemInfo(@Param("itemId") String itemId);

	//큰 분류(카테고리)별 아이템
	@Select("SELECT * FROM business_item, category WHERE category_type = #{categoryType} and category.category_name = business_item.item_Category")
	List<BusinessItemBean> getItemByCategory(@Param("categoryType")String categoryType);

	//소분류(카테고리)별 아이템
	@Select("SELECT * FROM business_item, category WHERE category_Name = #{subCategoryName} and category.category_name = business_item.item_category")
	List<BusinessItemBean> getItemBySubCategory(@Param("subCategoryName") String subCategoryName);

	//사업자의 상품 ID 목록 조회
	@Select("select item_id from business_item where business_id = #{buisness_id}")
	List<Integer> getItemIdsByBusinessId(String business_id);

	//상품 번호로 정보 조회
	@Select("select * from business_item where item_id = #{item_id}")
	BusinessItemBean getItemById(String item_id);

	//아이템 리스트를 랜덤 순으로
	@Select("SELECT * FROM business_item ORDER BY DBMS_RANDOM.VALUE")
	List<BusinessItemBean> randomItemList();



}
