package com.spacedong.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.spacedong.beans.BoardBean;
import com.spacedong.repository.BoardRepository;

@Service
public class BoardService {

    @Autowired
    private BoardRepository userBoardRepository;

    // 회원 게시판 조회 (페이징 적용)
    public List<BoardBean> getUserBoardList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return userBoardRepository.getUserBoardList(offset, pageSize);
    }

    // 회원 게시판 총 개수
    public int getUserBoardCount() {
        return userBoardRepository.getUserBoardCount();
    }
    /** 판매자 게시판 조회 **/
    public List<BoardBean> getBusinessBoardList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return userBoardRepository.getBusinessBoardList(offset, pageSize);
    }

    public int getBusinessBoardCount() {
        return userBoardRepository.getBusinessBoardCount();
    }

    /** 운영자 게시판 조회 **/
    public List<BoardBean> getAdminBoardList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return userBoardRepository.getAdminBoardList(offset, pageSize);
    }

    public int getAdminBoardCount() {
        return userBoardRepository.getAdminBoardCount();
    }

    /** 통합 게시판 조회 (모든 게시글) **/
    public List<BoardBean> getAllBoardList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return userBoardRepository.getAllBoardList(offset, pageSize);
    }

    public int getAllBoardCount() {
        return userBoardRepository.getAllBoardCount();
    }

}
