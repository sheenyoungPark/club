package com.spacedong.controller;

import com.spacedong.beans.BoardBean;
import com.spacedong.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/community")
public class BoardController {

	@Autowired
	private BoardService userBoardService;

	@GetMapping("/board")
	public String getBoard(@RequestParam(value = "boardType", required = false) String boardType,
			@RequestParam(value = "page", defaultValue = "1") int page, Model model) {

		int pageSize = 10; // 한 페이지에 보여줄 게시글 수
		int pageBlock = 10; // 한 번에 보여줄 페이지 번호 개수
		int totalPosts = 0;
		int totalPages = 0;
		
		List<BoardBean> boardList = null;
		
		if ("user".equals(boardType)) {
			// 회원 게시판만 조회
			totalPosts = userBoardService.getUserBoardCount();
			boardList = userBoardService.getUserBoardList(page, pageSize);
			model.addAttribute("userBoardList", boardList);

		} else if ("business".equals(boardType)) {
            // 판매자 게시판 조회
            totalPosts = userBoardService.getBusinessBoardCount();
            boardList = userBoardService.getBusinessBoardList(page, pageSize);
            model.addAttribute("businessBoardList", boardList);

        } else if ("admin".equals(boardType)) {
            // 운영자 게시판 조회
            totalPosts = userBoardService.getAdminBoardCount();
            boardList = userBoardService.getAdminBoardList(page, pageSize);
            model.addAttribute("adminBoardList", boardList);

        } else {
            // 통합 게시판 (모든 게시글 최신순으로 조회)
            totalPosts = userBoardService.getAllBoardCount();
            boardList = userBoardService.getAllBoardList(page, pageSize);
            model.addAttribute("allBoardList", boardList);
        }


		totalPages = (int) Math.ceil((double) totalPosts / pageSize);

		int startPage = ((page - 1) / pageBlock) * pageBlock + 1;
		int endPage = Math.min(startPage + pageBlock - 1, totalPages);

		model.addAttribute("boardList", boardList);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("startPage", startPage);
		model.addAttribute("endPage", endPage);
		model.addAttribute("boardType", boardType);
		
		return "community/board";
	}
}
