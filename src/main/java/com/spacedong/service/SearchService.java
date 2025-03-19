package com.spacedong.service;

import com.spacedong.beans.BusinessBean;
import com.spacedong.beans.BusinessItemBean;
import com.spacedong.beans.ClubBean;
import com.spacedong.repository.SearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    @Autowired
    private SearchRepository searchRepository;

    public List<ClubBean> searchedClubs(String region, int age, String searchtxt) {

        return searchRepository.searchedClubs(region, age, searchtxt);
    }
    // age 매개변수 제거
    public List<BusinessItemBean> searchedBusinessItems(String region, String searchtxt) {
        return searchRepository.searchedBusinessItems(region, searchtxt);
    }
}
