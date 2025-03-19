package com.spacedong.beans;

import java.time.LocalDateTime;
import java.util.List;

public class BoardCommentBean {

    private int comment_id; // 댓글 ID
    private int board_id; // 게시글 ID
    private String comment_writer_id; // 댓글 작성자 ID
    private String comment_writer_name; // ✅ 추가: 작성자의 닉네임 또는 비즈니스명
    private String comment_text; // 댓글 내용
    private Integer parent_comment_id; // 부모 댓글 ID (NULL이면 일반 댓글)
    private LocalDateTime create_date; // 작성일

    private List<BoardCommentBean> re_comments; // 대댓글 목록

    public String getComment_writer_name() {
        return comment_writer_name;
    }

    public void setComment_writer_name(String comment_writer_name) {
        this.comment_writer_name = comment_writer_name;
    }

    public int getComment_id() {
        return comment_id;
    }

    public void setComment_id(int comment_id) {
        this.comment_id = comment_id;
    }

    public int getBoard_id() {
        return board_id;
    }

    public void setBoard_id(int board_id) {
        this.board_id = board_id;
    }

    public String getComment_writer_id() {
        return comment_writer_id;
    }

    public void setComment_writer_id(String comment_writer_id) {
        this.comment_writer_id = comment_writer_id;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public Integer getParent_comment_id() { // ✅ int → Integer 변경 (NULL 허용)
        return parent_comment_id;
    }

    public void setParent_comment_id(Integer parent_comment_id) { // ✅ int → Integer 변경
        this.parent_comment_id = parent_comment_id;
    }

    public LocalDateTime getCreate_date() {
        return create_date;
    }

    public void setCreate_date(LocalDateTime create_date) {
        this.create_date = create_date;
    }

    public List<BoardCommentBean> getRe_comments() {
        return re_comments;
    }

    public void setRe_comments(List<BoardCommentBean> re_comments) {
        this.re_comments = re_comments;
    }
}
