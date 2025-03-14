package com.spacedong.service;

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
}
