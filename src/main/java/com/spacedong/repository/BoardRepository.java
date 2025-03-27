package com.spacedong.repository;

import java.util.List;

import org.apache.ibatis.annotations.Update;
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

    /** ✅ 게시글 좋아요 증가 */
    public void incrementLike(String boardType, int boardId) {
        boardMapper.incrementLike(boardType, boardId);
    }

    /** ✅ 현재 게시글의 좋아요 개수 가져오기 */
    public int getLikeCount(String boardType, int boardId) {
        return boardMapper.getLikeCount(boardType, boardId);
    }
    /** ✅ 게시글 좋아요 수 감소 */
    public void decrementLike(String boardType, int boardId) {
        boardMapper.decrementLike(boardType, boardId);
    }

    /** ✅ 사용자가 게시글에 좋아요를 눌렀는지 확인 */
    public boolean checkUserLiked(String boardType, int boardId, String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        return boardMapper.checkUserLiked(boardType, boardId, userId) > 0;
    }
    /** ✅ 게시글 좋아요 추가 */
    public void addBoardLike(String boardType, int boardId, String userId, String userType) {
        boardMapper.addBoardLike(boardType, boardId, userId, userType);
    }

    /** ✅ 게시글 좋아요 삭제 */
    public void removeBoardLike(String boardType, int boardId, String userId) {
        boardMapper.removeBoardLike(boardType, boardId, userId);
    }


    /** 댓글 조회 **/
    public List<BoardCommentBean> getCommentsByBoardId(String boardType, int boardId) {
        return boardMapper.getCommentsByBoardId(boardType, boardId);
    }

    /** ✅ 댓글 저장 */
    public void writeComment(String boardType, BoardCommentBean comment) {
        boardMapper.writeComment(boardType, comment);
    }


    /** ✅ 게시글 작성 후 ID 반환 */
    public int writeBoard(String boardType, BoardBean board) {
        boardMapper.writeBoard(boardType, board.getBoard_title(), board.getBoard_text(), board.getBoard_writer_id());
        System.out.println("📌 Repository: 마지막 게시글 ID 조회");
        return boardMapper.getLastInsertedBoardId(boardType);
    }

    /** ✅ 게시글 이미지 저장 */
    public void saveBoardImage(String boardType, int boardId, String fileName) {
        System.out.println("📌 Repository: " + boardType + "_board_image 테이블에 저장");
        boardMapper.saveBoardImage(boardType, boardId, fileName);
    }


    /** ✅ 특정 게시글의 이미지 목록 가져오기 **/
    public List<String> getBoardImages(String boardType, int boardId) {
        return boardMapper.getBoardImages(boardType, boardId);
    }



    // ✅ 게시글 삭제 (ON DELETE CASCADE 덕분에 관련 댓글과 이미지 정보도 삭제됨)
    public void deleteBoard(String boardType, int boardId) {
        System.out.println("🗑 Repository: 게시글 삭제 (boardType: " + boardType + ", boardId: " + boardId + ")");
        boardMapper.deleteBoard(boardType, boardId);
    }

    /** ✅ 게시글 수정 */
    public void updateBoard(String boardType, BoardBean board) {
        boardMapper.updateBoard(boardType, board.getBoard_id(), board.getBoard_title(), board.getBoard_text());
    }

    /** ✅ 특정 게시글의 특정 이미지 삭제 */
    public void deleteBoardImage(String boardType, int boardId, String fileName) {
        boardMapper.deleteBoardImage(boardType, boardId, fileName);
    }

    /** 게시판 검색 기능 */
    public List<BoardBean> searchBoards(String searchType, String keyword) {
        return boardMapper.searchBoards(searchType, keyword);
    }

}
