package com.spacedong.repository;

import com.spacedong.beans.ClubBean;
import com.spacedong.mapper.SearchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SearchRepository {

    @Autowired
    private SearchMapper searchMapper;

    public List<ClubBean> searchedClubs(String region, int age, String searchtxt) {

        return searchMapper.searchedClubs(region, age, searchtxt);
    }
}
