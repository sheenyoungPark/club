package com.spacedong.beans;

import java.time.LocalDateTime;
import java.util.Date;

public class ClubBoardBean {

	private int board_id;
	private int club_id;
	private String board_title;
	private String board_text;
	private String board_writer_id;
	private String board_img;
	private int board_view;
	private int board_like;
	private LocalDateTime create_date;
	private LocalDateTime update_date;

	private String board_writer_nickname; // ✅ 작성자 닉네임 추가

	// Getter & Setter 추가
	public String getBoard_writer_nickname() {
		return board_writer_nickname;
	}

	public void setBoard_writer_nickname(String board_writer_nickname) {
		this.board_writer_nickname = board_writer_nickname;
	}

	public int getBoard_id() {
		return board_id;
	}

	public void setBoard_id(int board_id) {
		this.board_id = board_id;
	}

	public int getClub_id() {
		return club_id;
	}

	public void setClub_id(int club_id) {
		this.club_id = club_id;
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

    public String getBoard_img() {
        return board_img;
    }

    public void setBoard_img(String board_img) {
        this.board_img = board_img;
    }
}
