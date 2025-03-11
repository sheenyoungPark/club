package com.spacedong.repository;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BoardCommentBean;
import com.spacedong.mapper.BoardMapper;

@Repository
public class BoardRepository {

    @Autowired
    private BoardMapper boardMapper;

    /**
     * 회원 게시판 게시글 조회 (페이징 적용)
     */
    public List<BoardBean> getMemberBoardList(int offset, int pageSize) {
        return boardMapper.getMemberBoardList(offset, pageSize);
    }

    /**
     * 회원 게시판 총 게시글 수 조회
     */
    public int getMemberBoardCount() {
        return boardMapper.getMemberBoardCount();
    }

    /** 판매자 게시판 조회 **/
    public List<BoardBean> getBusinessBoardList(int offset, int pageSize) {
        return boardMapper.getBusinessBoardList(offset, pageSize);
    }

    public int getBusinessBoardCount() {
        return boardMapper.getBusinessBoardCount();
    }

    /** 운영자 게시판 조회 **/
    public List<BoardBean> getAdminBoardList(int offset, int pageSize) {
        return boardMapper.getAdminBoardList(offset, pageSize);
    }

    public int getAdminBoardCount() {
        return boardMapper.getAdminBoardCount();
    }

    /** 통합 게시판 조회 **/
    public List<BoardBean> getAllBoardList(int offset, int pageSize) {
        return boardMapper.getAllBoardList(offset, pageSize);
    }

    public int getAllBoardCount() {
        return boardMapper.getAllBoardCount();
    }

    /** 게시글이 존재하는 테이블 찾기 **/
    public String findBoardType(int boardId) {
        return boardMapper.findBoardType(boardId);
    }

    /** 게시글 상세 조회 **/
    public BoardBean getBoardDetail(String boardType, int boardId) {
        return boardMapper.getBoardDetail(boardType, boardId);
    }

    /** 게시글 조회수 증가 **/
    public void incrementViewCount(String boardType, int boardId) {
        boardMapper.incrementViewCount(boardType, boardId);
    }

    /** 댓글 조회 **/
    public List<BoardCommentBean> getCommentsByBoardId(String boardType, int boardId) {
        return boardMapper.getCommentsByBoardId(boardType, boardId);
    }

    /** 댓글 작성 **/
    public void writeComment(BoardCommentBean comment) {
        boardMapper.writeComment(comment);
    }

    /** ✅ 게시글 작성 메서드 추가 **/
    public void writeBoard(String boardType, BoardBean board) {
        boardMapper.writeBoard(boardType, board.getBoard_title(), board.getBoard_text(), board.getBoard_writer_id());
    }
}
