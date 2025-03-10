package com.spacedong.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.spacedong.beans.BoardBean;

@Mapper
public interface BoardMapper {

    // 회원 게시판 게시글 조회 (페이징 적용)
    @Select("SELECT * FROM member_board ORDER BY create_date DESC OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getMemberBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM member_board")
    int getMemberBoardCount();
    
    /** 판매자 게시판 **/
    @Select("SELECT * FROM business_board ORDER BY create_date DESC OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getBusinessBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM business_board")
    int getBusinessBoardCount();

    /** 운영자 게시판 **/
    @Select("SELECT * FROM admin_board ORDER BY create_date DESC OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getAdminBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM admin_board")
    int getAdminBoardCount();

    /** 통합 게시판 **/
    @Select("SELECT * FROM (SELECT * FROM member_board UNION ALL SELECT * FROM business_board UNION ALL SELECT * FROM admin_board) ORDER BY create_date DESC OFFSET #{offset} ROWS FETCH NEXT #{pageSize} ROWS ONLY")
    List<BoardBean> getAllBoardList(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM (SELECT board_id FROM member_board UNION ALL SELECT board_id FROM business_board UNION ALL SELECT board_id FROM admin_board)")
    int getAllBoardCount();
}
