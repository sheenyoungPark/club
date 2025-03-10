package com.spacedong.beans;

import java.time.LocalDateTime;

public class BoardBean {

	private int board_id; // 게시글 ID
	private String board_title; // 게시글 제목
	private String board_text; // 게시글 내용
	private String board_writer_id; // 작성자 ID
	private int board_view; // 조회수
	private int board_like; // 좋아요 수
	private LocalDateTime create_date; // 생성일
	private LocalDateTime update_date; // 수정일
	
	public int getBoard_id() {
		return board_id;
	}
	public void setBoard_id(int board_id) {
		this.board_id = board_id;
	}
	public String getBoard_title() {
		return board_title;
	}
	public void setBoard_title(String board_title) {
		this.board_title = board_title;
	}
	public String getBoard_text() {
		return board_text;
	}
	public void setBoard_text(String board_text) {
		this.board_text = board_text;
	}
	public String getBoard_writer_id() {
		return board_writer_id;
	}
	public void setBoard_writer_id(String board_writer_id) {
		this.board_writer_id = board_writer_id;
	}
	public int getBoard_view() {
		return board_view;
	}
	public void setBoard_view(int board_view) {
		this.board_view = board_view;
	}
	public int getBoard_like() {
		return board_like;
	}
	public void setBoard_like(int board_like) {
		this.board_like = board_like;
	}
	public LocalDateTime getCreate_date() {
		return create_date;
	}
	public void setCreate_date(LocalDateTime create_date) {
		this.create_date = create_date;
	}
	public LocalDateTime getUpdate_date() {
		return update_date;
	}
	public void setUpdate_date(LocalDateTime update_date) {
		this.update_date = update_date;
	}
	

}
