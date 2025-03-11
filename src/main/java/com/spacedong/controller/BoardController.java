package com.spacedong.controller;

import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BoardCommentBean;
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
	private BoardService boardService;

	/** ✅ 게시판 목록 조회 */
	@GetMapping("/board")
	public String getBoard(@RequestParam(value = "boardType", required = false) String boardType,
						   @RequestParam(value = "page", defaultValue = "1") int page, Model model) {

		int pageSize = 10; // 한 페이지에 보여줄 게시글 수
		int pageBlock = 10; // 한 번에 보여줄 페이지 번호 개수
		int totalPosts = 0;
		int totalPages = 0;
		List<BoardBean> boardList = null;

		if ("member".equals(boardType)) {
			totalPosts = boardService.getMemberBoardCount();
			boardList = boardService.getMemberBoardList(page, pageSize);
			model.addAttribute("memberBoardList", boardList);
		} else if ("business".equals(boardType)) {
			totalPosts = boardService.getBusinessBoardCount();
			boardList = boardService.getBusinessBoardList(page, pageSize);
			model.addAttribute("businessBoardList", boardList);
		} else if ("admin".equals(boardType)) {
			totalPosts = boardService.getAdminBoardCount();
			boardList = boardService.getAdminBoardList(page, pageSize);
			model.addAttribute("adminBoardList", boardList);
		} else {
			totalPosts = boardService.getAllBoardCount();
			boardList = boardService.getAllBoardList(page, pageSize);
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

	/** ✅ 게시글 상세 조회 */
	@GetMapping("/boardDetail")
	public String getBoardDetail(@RequestParam("id") int boardId,
								 @RequestParam(value = "boardType", required = false) String boardType,
								 Model model) {

		System.out.println("board_id 값: " + boardId);
		System.out.println("boardType 값: " + boardType);

		if ("all".equals(boardType)) {
			boardType = boardService.findBoardType(boardId);
			if (boardType == null) {
				model.addAttribute("errorMessage", "해당 ID(" + boardId + ")의 게시글을 찾을 수 없습니다.");
				return "community/error";
			}
		}

		BoardBean post = boardService.getBoardDetail(boardType, boardId);
		if (post == null) {
			model.addAttribute("errorMessage", "게시글을 불러올 수 없습니다.");
			return "community/error";
		}

		boardService.incrementViewCount(boardType, boardId);

		List<BoardCommentBean> comments = boardService.getCommentHierarchy(boardType, boardId);
		model.addAttribute("comments", comments);
		model.addAttribute("post", post);
		model.addAttribute("boardType", boardType);

		return "community/boardDetail";
	}

	/** ✅ 댓글 작성 */
	@PostMapping("/comment/write")
	public String writeComment(@RequestParam("board_id") int boardId,
							   @RequestParam("comment_text") String commentText,
							   @RequestParam("comment_writer_id") String writerId,
							   @RequestParam("boardType") String boardType,
							   @RequestParam(value = "parent_comment_id", required = false) Integer parentCommentId) {

		System.out.println("board_id: " + boardId);
		System.out.println("boardType: " + boardType);
		System.out.println("comment_writer_id: " + writerId);
		System.out.println("comment_text: " + commentText);
		System.out.println("parent_comment_id: " + parentCommentId);

		// ✅ 댓글 객체 생성
		BoardCommentBean comment = new BoardCommentBean();
		comment.setBoard_id(boardId);
		comment.setComment_writer_id(writerId);
		comment.setComment_text(commentText);
		comment.setParent_comment_id(parentCommentId);

		// ✅ 유동적인 boardType으로 댓글 저장
		boardService.writeComment(boardType, comment);
		System.out.println(boardId);
		System.out.println(writerId);
		return "redirect:/community/boardDetail?id=" + boardId + "&boardType=" + boardType;
	}

	/** ✅ 1. 글쓰기 페이지 이동 (복구된 부분) */
	@GetMapping("/boardWrite")
	public String showBoardWritePage(@RequestParam(value = "boardType", required = false) String boardType, Model model) {
		model.addAttribute("boardType", boardType);
		return "community/boardWrite"; // 글쓰기 페이지로 이동
	}

	/** ✅ 게시글 작성 */
	@PostMapping("/write")
	public String writeBoard(@RequestParam("boardType") String boardType,
							 @RequestParam("board_title") String title,
							 @RequestParam("board_text") String text,
							 @RequestParam("board_writer_id") String writerId) {

		BoardBean board = new BoardBean();
		board.setBoard_title(title);
		board.setBoard_text(text);
		board.setBoard_writer_id(writerId);

		boardService.writeBoard(boardType, board);
		return "redirect:/community/board?boardType=" + boardType;
	}
}
