package com.spacedong.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BoardCommentBean;
import com.spacedong.repository.BoardRepository;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    // 회원 게시판 조회 (페이징 적용)
    public List<BoardBean> getMemberBoardList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return boardRepository.getMemberBoardList(offset, pageSize);
    }

    // 회원 게시판 총 개수
    public int getMemberBoardCount() {
        return boardRepository.getMemberBoardCount();
    }

    /** 판매자 게시판 조회 **/
    public List<BoardBean> getBusinessBoardList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return boardRepository.getBusinessBoardList(offset, pageSize);
    }

    public int getBusinessBoardCount() {
        return boardRepository.getBusinessBoardCount();
    }

    /** 운영자 게시판 조회 **/
    public List<BoardBean> getAdminBoardList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return boardRepository.getAdminBoardList(offset, pageSize);
    }

    public int getAdminBoardCount() {
        return boardRepository.getAdminBoardCount();
    }

    /** 통합 게시판 조회 (모든 게시글) **/
    public List<BoardBean> getAllBoardList(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return boardRepository.getAllBoardList(offset, pageSize);
    }

    public int getAllBoardCount() {
        return boardRepository.getAllBoardCount();
    }

    /** 게시글이 존재하는 테이블 찾기 **/
    public String findBoardType(int boardId) {
        return boardRepository.findBoardType(boardId);
    }

    /** 게시글 상세 조회 **/
    public BoardBean getBoardDetail(String boardType, int boardId) {
        return boardRepository.getBoardDetail(boardType, boardId);
    }

    /** 게시글 조회수 증가 **/
    public void incrementViewCount(String boardType, int boardId) {
        boardRepository.incrementViewCount(boardType, boardId);
    }

    /** 댓글 조회 (계층 구조 정렬) **/
    public List<BoardCommentBean> getCommentHierarchy(String boardType, int boardId) {
        List<BoardCommentBean> comments = boardRepository.getCommentsByBoardId(boardType, boardId);

        // 부모 댓글과 대댓글을 매핑할 맵 생성
        Map<Integer, List<BoardCommentBean>> commentMap = new HashMap<>();

        for (BoardCommentBean comment : comments) {
            if (comment.getParent_comment_id() != null) {
                commentMap.computeIfAbsent(comment.getParent_comment_id(), k -> new ArrayList<>()).add(comment);
            }
        }

        // 부모 댓글 목록을 구성하고, 대댓글을 추가
        List<BoardCommentBean> sortedComments = new ArrayList<>();
        for (BoardCommentBean comment : comments) {
            if (comment.getParent_comment_id() == null) {
                sortedComments.add(comment);
                if (commentMap.containsKey(comment.getComment_id())) {
                    sortedComments.addAll(commentMap.get(comment.getComment_id()));
                }
            }
        }

        return sortedComments;
    }

    /** 댓글 작성 **/
    public void writeComment(BoardCommentBean comment) {
        boardRepository.writeComment(comment);
    }

    /** ✅ 게시글 작성 기능 추가 **/
    public void writeBoard(String boardType, BoardBean board) {
        boardRepository.writeBoard(boardType, board);
    }
}
