package com.spacedong.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.spacedong.beans.BoardBean;
import com.spacedong.mapper.BoardMapper;

@Repository
public class BoardRepository {

    @Autowired
    private BoardMapper memberBoardMapper;

    /**
     * 회원 게시판 게시글 조회 (페이징 적용)
     */
    public List<BoardBean> getMemeberBoardList(int offset, int pageSize) {
        return memberBoardMapper.getMemberBoardList(offset, pageSize);
    }

    /**
     * 회원 게시판 총 게시글 수 조회
     */
    public int getMemberBoardCount() {
        return memberBoardMapper.getMemberBoardCount();
    }
    /** 판매자 게시판 조회 **/
    public List<BoardBean> getBusinessBoardList(int offset, int pageSize) {
        return memberBoardMapper.getBusinessBoardList(offset, pageSize);
    }

    public int getBusinessBoardCount() {
        return memberBoardMapper.getBusinessBoardCount();
    }

    /** 운영자 게시판 조회 **/
    public List<BoardBean> getAdminBoardList(int offset, int pageSize) {
        return memberBoardMapper.getAdminBoardList(offset, pageSize);
    }

    public int getAdminBoardCount() {
        return memberBoardMapper.getAdminBoardCount();
    }

    /** 통합 게시판 조회 **/
    public List<BoardBean> getAllBoardList(int offset, int pageSize) {
        return memberBoardMapper.getAllBoardList(offset, pageSize);
    }

    public int getAllBoardCount() {
        return memberBoardMapper.getAllBoardCount();
    }
}
