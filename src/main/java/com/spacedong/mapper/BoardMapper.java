package com.spacedong.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;
import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BoardCommentBean;

@Mapper
public interface BoardMapper {

    /** ✅ 회원 게시판 조회 **/
    @Select("SELECT mb.*, m.member_nickname AS writer_name " +
            "FROM member_board mb " +
            "LEFT JOIN member m ON mb.board_writer_id = m.member_id " +
            "ORDER BY mb.create_date DESC " +
            "OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getMemberBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM member_board")
    int getMemberBoardCount();

    /** ✅ 판매자 게시판 조회 **/
    @Select("SELECT bb.*, b.business_name AS writer_name " +
            "FROM business_board bb " +
            "LEFT JOIN business b ON bb.board_writer_id = b.business_id " +
            "ORDER BY bb.create_date DESC " +
            "OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getBusinessBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM business_board")
    int getBusinessBoardCount();

    /** ✅ 운영자 게시판 조회 **/
    @Select("SELECT ab.*, '관리자' AS writer_name " +
            "FROM admin_board ab " +
            "ORDER BY ab.create_date DESC " +
            "OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getAdminBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM admin_board")
    int getAdminBoardCount();

    /** ✅ 통합 게시판 조회 (회원 + 판매자 + 운영자) **/
    @Select("SELECT b.board_id, b.board_title, b.board_text, b.board_writer_id, b.board_view, b.board_like, b.create_date, " +
            "       CASE " +
            "           WHEN b.board_type = 'admin' THEN '관리자' " +
            "           ELSE COALESCE(m.member_nickname, bs.business_name) " +
            "       END AS writer_name, " +
            "       b.board_type " +
            "FROM ( " +
            "    SELECT board_id, board_title, board_text, board_writer_id, board_view, board_like, create_date, 'member' AS board_type FROM member_board " +
            "    UNION ALL " +
            "    SELECT board_id, board_title, board_text, board_writer_id, board_view, board_like, create_date, 'business' AS board_type FROM business_board " +
            "    UNION ALL " +
            "    SELECT board_id, board_title, board_text, board_writer_id, board_view, board_like, create_date, 'admin' AS board_type FROM admin_board " +
            ") b " +
            "LEFT JOIN member m ON b.board_writer_id = m.member_id " +
            "LEFT JOIN business bs ON b.board_writer_id = bs.business_id " +
            "ORDER BY " +
        "    CASE WHEN b.board_type = 'admin' THEN 1 ELSE 2 END, " +
        "    b.create_date DESC " +
                "OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getAllBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);



    @Select("SELECT COUNT(*) FROM ("
            + "SELECT board_id FROM member_board "
            + "UNION ALL "
            + "SELECT board_id FROM business_board "
            + "UNION ALL "
            + "SELECT board_id FROM admin_board)")
    int getAllBoardCount();

    /** 게시글이 존재하는 테이블 찾기 **/
    @Select("SELECT CASE " + "WHEN EXISTS (SELECT 1 FROM member_board WHERE board_id = #{boardId}) THEN 'member' "
            + "WHEN EXISTS (SELECT 1 FROM business_board WHERE board_id = #{boardId}) THEN 'business' "
            + "WHEN EXISTS (SELECT 1 FROM admin_board WHERE board_id = #{boardId}) THEN 'admin' " + "ELSE NULL END "
            + "FROM dual")
    String findBoardType(@Param("boardId") int boardId);

    /** ✅ 게시글 상세 조회 **/
    @Select("SELECT board_id, board_title, board_text, board_writer_id, board_view, board_like, create_date, update_date "
            + "FROM ${boardType}_board WHERE board_id = #{boardId}")
    BoardBean getBoardDetail(@Param("boardType") String boardType, @Param("boardId") int boardId);

    /** ✅ 게시글 조회수 증가 **/
    @Update("UPDATE ${boardType}_board SET board_view = board_view + 1 WHERE board_id = #{boardId}")
    void incrementViewCount(@Param("boardType") String boardType, @Param("boardId") int boardId);

    /** ✅ 댓글 조회 **/
    @Select("SELECT * FROM ${boardType}_board_comment WHERE board_id = #{boardId} ORDER BY create_date ASC")
    List<BoardCommentBean> getCommentsByBoardId(@Param("boardType") String boardType, @Param("boardId") int boardId);

    /** ✅ 댓글 작성 **/
    @Insert("INSERT INTO ${boardType}_board_comment (comment_id, board_id, comment_writer_id, comment_text, parent_comment_id, create_date) "
            + "VALUES (comment_id_seq.NEXTVAL, #{comment.board_id}, #{comment.comment_writer_id}, #{comment.comment_text}, #{comment.parent_comment_id, jdbcType=INTEGER}, CURRENT_TIMESTAMP)")
    void writeComment(@Param("boardType") String boardType, @Param("comment") BoardCommentBean comment);


    /** ✅ 게시글 작성 **/
    @Insert("INSERT INTO ${boardType}_board (board_id, board_title, board_text, board_writer_id, board_view, board_like, create_date, update_date) "
            + "VALUES (board_id_seq.NEXTVAL, #{board_title}, #{board_text}, #{board_writer_id}, 0, 0, CURRENT_TIMESTAMP, NULL)")
    void writeBoard(@Param("boardType") String boardType,
                    @Param("board_title") String board_title,
                    @Param("board_text") String board_text,
                    @Param("board_writer_id") String board_writer_id);

    /** ✅ 마지막으로 추가된 게시글의 ID 가져오기 */
    @Select("SELECT board_id FROM ${boardType}_board ORDER BY create_date DESC FETCH FIRST 1 ROWS ONLY")
    int getLastInsertedBoardId(@Param("boardType") String boardType);

    /** ✅ 게시글 이미지 저장 */
    @Insert("INSERT INTO ${boardType}_board_image (board_id, img) VALUES (#{boardId}, #{fileName})")
    void saveBoardImage(@Param("boardType") String boardType, @Param("boardId") int boardId, @Param("fileName") String fileName);

    /** ✅ 특정 게시글의 이미지 목록 가져오기 **/
    @Select("SELECT img FROM ${boardType}_board_image WHERE board_id = #{boardId}")
    List<String> getBoardImages(@Param("boardType") String boardType, @Param("boardId") int boardId);

    /** ✅ 게시글 삭제 (ON DELETE CASCADE로 댓글 및 이미지도 자동 삭제됨) **/
    @Delete("DELETE FROM ${boardType}_board WHERE board_id = #{boardId}")
    void deleteBoard(@Param("boardType") String boardType, @Param("boardId") int boardId);
}
