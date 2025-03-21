package com.spacedong.controller;

import com.spacedong.beans.BoardBean;
import com.spacedong.beans.BoardCommentBean;
import com.spacedong.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

		List<String> images = boardService.getBoardImages(boardType, boardId);

		model.addAttribute("comments", comments);
		model.addAttribute("post", post);
		model.addAttribute("boardType", boardType);
		model.addAttribute("images", images);

		return "community/boardDetail";
	}

	/** ✅ 게시글 좋아요 토글 API */
	@PostMapping("/like")
	@ResponseBody
	public Map<String, Object> toggleLike(
			@RequestParam("board_id") int boardId,
			@RequestParam("boardType") String boardType,
			@RequestParam(value = "action", required = false) String action) {

		Map<String, Object> response = new HashMap<>();

		// action 파라미터에 따라 좋아요 증가 또는 감소
		int newLikeCount;
		if ("unlike".equals(action)) {
			// 좋아요 취소 (감소)
			boardService.decrementLike(boardType, boardId);
			newLikeCount = boardService.getLikeCount(boardType, boardId);
		} else {
			// 좋아요 (증가)
			boardService.incrementLike(boardType, boardId);
			newLikeCount = boardService.getLikeCount(boardType, boardId);
		}

		// JSON 응답 생성
		response.put("success", true);
		response.put("newLikeCount", newLikeCount);

		return response;
	}


	/** ✅ 댓글 작성 */
	@PostMapping("/comment/write")
	public String writeComment(@RequestParam("board_id") int boardId,
							   @RequestParam("comment_text") String commentText,
							   @RequestParam(value = "comment_writer_id", required = false) String writerId, // ✅ NULL 허용
							   @RequestParam(value = "comment_writer_name", required = false) String writerName, // ✅ NULL 허용
							   @RequestParam("boardType") String boardType,
							   @RequestParam(value = "parent_comment_id", required = false) Integer parentCommentId) {

		// ✅ 필수값 체크
		if (writerId == null || writerId.trim().isEmpty()) {
			System.out.println("🚨 오류: comment_writer_id가 NULL입니다!");
			return "redirect:/community/boardDetail?id=" + boardId + "&boardType=" + boardType + "&error=missing_writer_id";
		}

		// ✅ 댓글 객체 생성
		BoardCommentBean comment = new BoardCommentBean();
		comment.setBoard_id(boardId);
		comment.setComment_writer_id(writerId);
		comment.setComment_writer_name(writerName); // ✅ 닉네임 or 비즈니스명 저장
		comment.setComment_text(commentText);
		comment.setParent_comment_id(parentCommentId);

		// ✅ 유동적인 boardType으로 댓글 저장
		boardService.writeComment(boardType, comment);

		return "redirect:/community/boardDetail?id=" + boardId + "&boardType=" + boardType;
	}




	/** ✅ 1. 글쓰기 페이지 이동 (복구된 부분) */
	@GetMapping("/boardWrite")
	public String showBoardWritePage(@RequestParam(value = "boardType", required = false) String boardType, Model model) {
		model.addAttribute("boardType", boardType);
		return "community/boardWrite"; // 글쓰기 페이지로 이동
	}

	@PostMapping("/write")
	public String writeBoard(@RequestParam("boardType") String boardType,
							 @RequestParam("board_title") String title,
							 @RequestParam("board_text") String text,
							 @RequestParam("board_writer_id") String writerId,
							 @RequestParam(value = "images", required = false) List<MultipartFile> images) {

		// 1️⃣ 게시글 정보 저장
		BoardBean board = new BoardBean();
		board.setBoard_title(title);
		board.setBoard_text(text);
		board.setBoard_writer_id(writerId);

		int boardId = boardService.writeBoard(boardType, board);
		System.out.println("📌 저장된 게시글 ID: " + boardId);

		// 2️⃣ 이미지 저장 (이미지가 있을 때만 실행)
		String uploadDir = "C:/upload/image/" + boardType + "BoardImg/"; // ✅ 저장 경로 변경
		File dir = new File(uploadDir);
		if (!dir.exists()) {
			boolean created = dir.mkdirs();
			if (created) {
				System.out.println("📌 디렉토리 생성 완료: " + uploadDir);
			} else {
				System.out.println("🚨 디렉토리 생성 실패!");
			}
		}

		if (images != null && !images.isEmpty()) {
			for (MultipartFile image : images) {
				if (!image.isEmpty()) {
					try {
						String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
						File destFile = new File(uploadDir + fileName);
						image.transferTo(destFile);

						System.out.println("📌 이미지 저장 완료: " + destFile.getAbsolutePath());

						// 3️⃣ DB에 이미지 정보 저장
						boardService.saveBoardImage(boardType, boardId, fileName);
					} catch (IOException e) {
						System.out.println("🚨 이미지 저장 실패: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("📌 업로드된 이미지 없음.");
		}

		return "redirect:/community/board?boardType=" + boardType;
	}

	@PostMapping("/board/delete")
	public String deleteBoard(@RequestParam("board_id") int boardId,
							  @RequestParam("boardType") String boardType) {

		// ✅ 삭제할 게시글 정보 가져오기 (예: 이미지 파일도 함께 삭제하기 위함)
		List<String> images = boardService.getBoardImages(boardType, boardId);

		// ✅ 1. 게시글 이미지 삭제 (물리적 파일 삭제)
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

		// ✅ 2. 게시글 DB에서 삭제
		boardService.deleteBoard(boardType, boardId);
		System.out.println("🗑 게시글 삭제 완료: ID " + boardId);

		// ✅ 3. 삭제 후 게시판 목록으로 이동
		return "redirect:/community/board?boardType=" + boardType;
	}

	/** ✅ 게시글 수정 페이지 이동 */
	@GetMapping("/boardEdit")
	public String showBoardEditPage(@RequestParam("id") int boardId,
									@RequestParam("boardType") String boardType,
									Model model) {
		// 게시글 정보 가져오기
		BoardBean post = boardService.getBoardDetail(boardType, boardId);

		// 게시글 이미지 가져오기
		List<String> images = boardService.getBoardImages(boardType, boardId);

		model.addAttribute("post", post);
		model.addAttribute("boardType", boardType);
		model.addAttribute("images", images);

		return "community/boardEdit";
	}

	/** ✅ 게시글 수정 처리 */
	@PostMapping("/edit")
	public String editBoard(@RequestParam("board_id") int boardId,
							@RequestParam("boardType") String boardType,
							@RequestParam("board_title") String title,
							@RequestParam("board_text") String text,
							@RequestParam(value = "images", required = false) List<MultipartFile> newImages,
							@RequestParam(value = "keep_images", required = false) List<String> keepImages) {

		// 1. 게시글 정보 업데이트
		BoardBean board = new BoardBean();
		board.setBoard_id(boardId);
		board.setBoard_title(title);
		board.setBoard_text(text);

		boardService.updateBoard(boardType, board);
		System.out.println("📌 게시글 정보 업데이트 완료: ID " + boardId);

		// 2. 유지할 이미지 처리
		List<String> currentImages = boardService.getBoardImages(boardType, boardId);

		// 삭제할 이미지 식별 (keepImages에 없는 현재 이미지)
		List<String> imagesToDelete = new ArrayList<>();
		for (String img : currentImages) {
			if (keepImages == null || !keepImages.contains(img)) {
				imagesToDelete.add(img);
			}
		}

		// 이미지 삭제
		if (!imagesToDelete.isEmpty()) {
			String uploadDir = "C:/upload/image/" + boardType + "BoardImg/";
			for (String imageName : imagesToDelete) {
				// DB에서 이미지 정보 삭제
				boardService.deleteBoardImage(boardType, boardId, imageName);

				// 파일 시스템에서 이미지 삭제
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

		// 3. 새 이미지 업로드 처리
		String uploadDir = "C:/upload/image/" + boardType + "BoardImg/";
		File dir = new File(uploadDir);
		if (!dir.exists()) {
			boolean created = dir.mkdirs();
			if (created) {
				System.out.println("📌 디렉토리 생성 완료: " + uploadDir);
			} else {
				System.out.println("🚨 디렉토리 생성 실패!");
			}
		}

		if (newImages != null && !newImages.isEmpty()) {
			for (MultipartFile image : newImages) {
				if (!image.isEmpty()) {
					try {
						String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
						File destFile = new File(uploadDir + fileName);
						image.transferTo(destFile);

						System.out.println("📌 이미지 저장 완료: " + destFile.getAbsolutePath());

						// DB에 이미지 정보 저장
						boardService.saveBoardImage(boardType, boardId, fileName);
					} catch (IOException e) {
						System.out.println("🚨 이미지 저장 실패: " + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}

		return "redirect:/community/boardDetail?id=" + boardId + "&boardType=" + boardType;
	}
}
