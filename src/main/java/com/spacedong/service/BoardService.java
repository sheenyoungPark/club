package com.spacedong.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
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

    /** ✅ 댓글 작성 */
    public void writeComment(String boardType, BoardCommentBean comment) {
        boardRepository.writeComment(boardType, comment);
    }


    /** ✅ 게시글 작성 + ID 반환 */
    public int writeBoard(String boardType, BoardBean board) {
        System.out.println("📌 서비스: 게시글 저장 (boardType: " + boardType + ")");
        return boardRepository.writeBoard(boardType, board);
    }

    /** ✅ 게시글에 이미지 저장 */
    public void saveBoardImage(String boardType, int boardId, String fileName) {
        System.out.println("📌 DB에 이미지 저장: " + fileName + " (boardId: " + boardId + ")");
        boardRepository.saveBoardImage(boardType, boardId, fileName);
    }



    /** ✅ 특정 게시글의 이미지 목록 가져오기 */
    public List<String> getBoardImages(String boardType, int boardId) { // ✅ boardType 추가
        return boardRepository.getBoardImages(boardType, boardId);
    }

    // ✅ 게시글 삭제 (물리적 파일 삭제 포함)
    public void deleteBoard(String boardType, int boardId) {
        System.out.println("🗑 서비스: 게시글 삭제 (boardType: " + boardType + ", boardId: " + boardId + ")");

        // 1️⃣ 해당 게시글에 포함된 이미지 리스트 가져오기
        List<String> images = boardRepository.getBoardImages(boardType, boardId);

        // 2️⃣ 이미지 파일 삭제 (DB 삭제는 ON DELETE CASCADE가 처리함)
        if (images != null && !images.isEmpty()) {
            String uploadDir = "C:/upload/image/" + boardType + "BoardImg/";
            for (String imageName : images) {
                File imageFile = new File(uploadDir + imageName);
                if (imageFile.exists()) {
                    if (imageFile.delete()) {
                        System.out.println("🗑 이미지 삭제 완료: " + imageFile.getAbsolutePath());
                    } else {
                        System.out.println("🚨 이미지 삭제 실패: " + imageFile.getAbsolutePath());
                    }
                }
            }
        }

        // 3️⃣ 게시글 DB에서 삭제 (댓글, 이미지도 같이 삭제됨 - ON DELETE CASCADE)
        boardRepository.deleteBoard(boardType, boardId);
        System.out.println("🗑 게시글 삭제 완료 (boardId: " + boardId + ")");
    }

}
