package com.spacedong.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.spacedong.beans.BusinessItems;

@Mapper
public interface ItemMapper {
	
	@Insert("insert into business_items values(#{business_id}, #{business_item}, #{item_img})")
	void additem(BusinessItems items);
	
	@Select("select business_id, business_item, item_img, b.business_name, b.business_address, b.business_ from business_itmes i, business b where b.business_id = i.business_id")
	List<BusinessItems> getAllItems();

}
