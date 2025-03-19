package com.spacedong.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.spacedong.beans.BusinessItemBean;

@Mapper
public interface ItemMapper {
	
	@Insert("insert into business_items values(#{business_id}, #{business_item}, #{item_img})")
	void additem(BusinessItemBean items);
	
	@Select("select business_id, business_item, item_img, b.business_name, b.business_address, b.business_ from business_itmes i, business b where b.business_id = i.business_id")
	List<BusinessItemBean> getAllItems();

	@Select("SELECT i.item_title, b.business_name " +
			"FROM business_item i " +
			"JOIN business b ON i.business_id = b.business_id " +
			"WHERE i.item_id = #{itemId}")
	Map<String, String> getItemInfo(@Param("itemId") String itemId);

}
