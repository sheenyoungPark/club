package com.spacedong.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BoardCommentBean;

@Mapper
public interface BoardMapper {

    /** ✅ 회원 게시판 조회 **/
    @Select("SELECT * FROM member_board ORDER BY create_date DESC OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getMemberBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM member_board")
    int getMemberBoardCount();

    /** ✅ 판매자 게시판 조회 **/
    @Select("SELECT * FROM business_board ORDER BY create_date DESC OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getBusinessBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM business_board")
    int getBusinessBoardCount();

    /** ✅ 운영자 게시판 조회 **/
    @Select("SELECT * FROM admin_board ORDER BY create_date DESC OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getAdminBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM admin_board")
    int getAdminBoardCount();

    /** ✅ 통합 게시판 조회 (회원 + 판매자 + 운영자) **/
    @Select("SELECT * FROM ("
            + "SELECT * FROM member_board "
            + "UNION ALL "
            + "SELECT * FROM business_board "
            + "UNION ALL "
            + "SELECT * FROM admin_board "
            + ") ORDER BY create_date DESC OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
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
    @Insert("INSERT INTO ${boardType}_board_comment (board_id, comment_writer_id, comment_text, parent_comment_id, create_date) "
            + "VALUES (#{board_id}, #{comment_writer_id}, #{comment_text}, #{parent_comment_id, jdbcType=INTEGER}, CURRENT_TIMESTAMP)")
    void writeComment(BoardCommentBean comment);

    /** ✅ 게시글 작성 **/
    @Insert("INSERT INTO ${boardType}_board (board_id, board_title, board_text, board_writer_id, board_view, board_like, create_date, update_date) "
            + "VALUES (board_id_seq.NEXTVAL, #{board_title}, #{board_text}, #{board_writer_id}, 0, 0, CURRENT_TIMESTAMP, NULL)")
    void writeBoard(@Param("boardType") String boardType,
                    @Param("board_title") String board_title,
                    @Param("board_text") String board_text,
                    @Param("board_writer_id") String board_writer_id);
}
