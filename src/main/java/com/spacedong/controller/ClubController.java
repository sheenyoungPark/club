package com.spacedong.controller;

import com.spacedong.beans.*;
import com.spacedong.service.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/club")
public class ClubController {

	// ✅ 클럽 프로필 이미지 저장 경로
	private static final String UPLOAD_DIR = "C:/upload/image/clubprofile/";

	@Resource(name = "loginMember")
	private MemberBean loginMember;

	@Resource(name = "loginBusiness")
	private BusinessBean loginBusiness;

	@Autowired
	private ClubService clubService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ClubMemberService clubMemberService;

	@Autowired
	private ReservationService reservationService;

	@Autowired
	private AdminNotificationService adminNotificationService;

    @Autowired
    private ChatService chatService;

	// ✅ 클럽 정보 페이지
	@GetMapping("/club_info")
	public String club_info(@RequestParam("club_id") int club_id, Model model) {
		ClubBean club = clubService.oneClubInfo(club_id);
		model.addAttribute("club", club);

		List<ClubDonationBean> donationList = clubService.getRecentDonations(club_id);
		model.addAttribute("donationList", donationList);

		if (loginMember.getMember_id() != null) {
			ClubMemberBean clubMemberBean = clubMemberService.getMemberInfo(club_id, loginMember.getMember_id());
			if (clubMemberBean != null) {
				String member_role = clubMemberBean.getMember_role();
				model.addAttribute("member_role", member_role);
			}
		}

		List<ClubMemberBean> clubMemberList = clubMemberService.getClubMemberList(club_id);
		model.addAttribute("clubMemberList", clubMemberList);

		List<ClubBoardBean> clubBoardList = clubService.getBoardListByClubId(club_id);
		model.addAttribute("clubBoardList", clubBoardList);

		boolean isMember = clubService.isMemberOfClub(club_id, loginMember.getMember_id());
		model.addAttribute("isMember", isMember);

		List<ReservationBean> clubReservation = reservationService.getReservationsByClubId(club_id);
		model.addAttribute("clubReservation", clubReservation);

		// ✅ 리뷰 존재 여부 맵 추가
		Map<Integer, Boolean> reviewExistsMap = new HashMap<>();
		for (ReservationBean reservation : clubReservation) {
			boolean hasReview = reservationService.checkReviewExists(reservation.getReservation_id());
			reviewExistsMap.put(reservation.getReservation_id(), hasReview);
		}
		model.addAttribute("reviewExistsMap", reviewExistsMap);

		return "club/club_info";
	}


	// ✅ 클럽 가입
	@GetMapping("/club_join")
	public String club_join(@RequestParam int club_id) {
		// 비즈니스 계정으로 로그인한 경우 거부
		if (loginBusiness != null && loginBusiness.isLogin()) {
			return "club/business_not_allowed";
		}

		String member_id = loginMember.getMember_id();
		if (member_id != null) {
			ClubMemberBean clubMemberBean = clubService.getClubMember(club_id, member_id);
			if (clubMemberBean == null) {
				ClubBean club = clubService.oneClubInfo(club_id);
				clubService.join_club(club_id, loginMember.getMember_id());
				String masterId = clubMemberService.getMasterMember(club_id);
				adminNotificationService.sendApprovalNotification(masterId, "MEMBER", "REQUEST3",
						club.getClub_name() + " 클럽 가입 신청", "");
				return "club/club_join_success";
			} else {
				return "club/club_member_fail";
			}
		} else {
			return "club/join_fail";
		}
	}

	// ✅ 클럽 생성 페이지
	@GetMapping("/create")
	public String create(@ModelAttribute ClubBean clubBean, Model model, RedirectAttributes redirectAttributes) {
		// 비즈니스 계정으로 로그인한 경우 클럽 생성 거부
		if (loginBusiness != null && loginBusiness.isLogin()) {
			redirectAttributes.addFlashAttribute("message",
					"클럽 만들기는 일반 회원만 이용 가능합니다. 일반 회원으로 로그인해 주세요.");
			return "redirect:/member/login";
		}

		if (loginMember.getMember_id() != null) {
			List<String> categoryTypes = categoryService.getAllCategoryType();
			model.addAttribute("categoryType", categoryTypes);
			model.addAttribute("clubBean", clubBean);
			return "club/create";
		} else {
			return "club/join_fail";
		}
	}

	// ✅ AJAX로 소분류 카테고리 목록 가져오기
	@GetMapping("/get_sub_categories")
	@ResponseBody
	public List<CategoryBean> getSubCategories(@RequestParam("category_type") String categoryType) {
		return categoryService.categoryInfo(categoryType);
	}

	// ✅ 클럽 생성 처리 + 대표 이미지 저장
	@PostMapping("/create_pro")
	public String create_pro(@Valid ClubBean clubBean,
							 BindingResult result,
							 @RequestParam(value = "clubImage", required = false) MultipartFile clubImage,
							 Model model,
							 RedirectAttributes redirectAttributes) {
		// 비즈니스 계정으로 로그인한 경우 클럽 생성 거부
		if (loginBusiness != null && loginBusiness.isLogin()) {
			redirectAttributes.addFlashAttribute("message",
					"클럽 만들기는 일반 회원만 이용 가능합니다. 일반 회원으로 로그인해 주세요.");
			return "redirect:/member/login";
		}

		if (result.hasErrors()) {
			List<String> categoryTypes = categoryService.getAllCategoryType();
			model.addAttribute("categoryType", categoryTypes);

			if (clubBean.getClub_category() != null && !clubBean.getClub_category().isEmpty()) {
				CategoryBean category = categoryService.getCategoryByName(clubBean.getClub_category());
				if (category != null) {
					List<CategoryBean> subCategoryList = categoryService.categoryInfo(category.getCategory_type());
					model.addAttribute("subCategoryList", subCategoryList);
				}
			}
			return "club/create";
		}

		// ✅ 디렉토리 생성
		File uploadDir = new File(UPLOAD_DIR);
		if (!uploadDir.exists()) {
			boolean dirCreated = uploadDir.mkdirs();
			System.out.println("📁 클럽 프로필 폴더 생성됨: " + dirCreated);
		}

		// ✅ 기본 프로필 이미지 설정 (NULL 방지)
		String profileFileName = null;

		// ✅ 업로드된 파일이 있는 경우 저장
		if (clubImage != null && !clubImage.isEmpty()) {
			try {
				String originalFilename = clubImage.getOriginalFilename();
				if (originalFilename != null && !originalFilename.isEmpty()) {
					String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
					List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif");

					if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
						System.out.println("🚨 허용되지 않은 파일 확장자: " + fileExtension);
						return "redirect:/club/create?error=invalid_file_type";
					}

					// ✅ 파일명 생성
					profileFileName = UUID.randomUUID().toString() + "_" + originalFilename;
					File destFile = new File(UPLOAD_DIR + profileFileName);
					clubImage.transferTo(destFile);

					// ✅ 파일이 정상적으로 저장되었는지 확인
					if (destFile.exists()) {
						System.out.println("📌 클럽 프로필 저장 완료: " + profileFileName);
					} else {
						System.out.println("🚨 파일 저장 실패: " + profileFileName);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("🚨 클럽 프로필 저장 중 오류 발생: " + e.getMessage());
			}
		}

		// ✅ 클럽 생성 + 프로필 이미지 저장
		clubBean.setClub_profile(profileFileName);
		clubService.create_club(clubBean);

		// ✅ 생성된 클럽의 ID로 회장 자동 가입
		ClubBean newClubBean = clubService.searchClubName(clubBean.getClub_name());
		clubService.create_join_club(newClubBean.getClub_id(), loginMember.getMember_id());

		// ✅ 관리자에게 알림 전송
		adminNotificationService.sendApprovalNotification("admin", "ADMIN", "REQUEST2",
				"클럽 " + clubBean.getClub_name(), "");



		return "redirect:/category/category_info";
	}

	// ✅ 게시글 작성 페이지
	@GetMapping("/board_write")
	public String board_write(@RequestParam("club_id") int club_id, Model model) {
		// 비즈니스 계정으로 로그인한 경우 접근 거부
		if (loginBusiness != null && loginBusiness.isLogin()) {
			return "redirect:/club/club_info?club_id=" + club_id;
		}

		// 현재 사용자가 해당 클럽의 회원인지 확인
		boolean isMember = clubService.isMemberOfClub(club_id, loginMember.getMember_id());
		if (!isMember) {
			return "redirect:/club/club_info?club_id=" + club_id;
		}

		// 새 게시글 객체 생성 및 모델에 추가
		ClubBoardBean clubBoardBean = new ClubBoardBean();
		clubBoardBean.setClub_id(club_id);
		model.addAttribute("clubBoardBean", clubBoardBean);

		return "club/board_write"; // 게시글 작성 페이지로 이동
	}

	// ✅ 게시글 작성 처리
	@PostMapping("/board_write_pro")
	public String board_write_pro(@ModelAttribute ClubBoardBean clubBoardBean,
								  @RequestParam(value = "image", required = false) MultipartFile image) {
		// 비즈니스 계정으로 로그인한 경우 접근 거부
		if (loginBusiness != null && loginBusiness.isLogin()) {
			return "redirect:/club/club_info?club_id=" + clubBoardBean.getClub_id();
		}

		// 현재 사용자가 해당 클럽의 회원인지 확인
		boolean isMember = clubService.isMemberOfClub(clubBoardBean.getClub_id(), loginMember.getMember_id());
		if (!isMember) {
			return "redirect:/club/club_info?club_id=" + clubBoardBean.getClub_id();
		}

		// ✅ 작성자 ID 및 작성 날짜 설정 (닉네임은 JOIN을 통해 자동으로 가져오므로 제거)
		clubBoardBean.setBoard_writer_id(loginMember.getMember_id());
		clubBoardBean.setCreate_date(java.time.LocalDateTime.now());

		// ✅ 이미지 업로드 처리 (선택 사항)
		if (image != null && !image.isEmpty()) {
			try {
				String originalFilename = image.getOriginalFilename();
				if (originalFilename != null && !originalFilename.isEmpty()) {
					String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
					List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif");

					if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
						System.out.println("🚨 허용되지 않은 파일 확장자: " + fileExtension);
						return "redirect:/club/board_write?club_id=" + clubBoardBean.getClub_id() + "&error=invalid_file_type";
					}

					// ✅ 동호회명 가져오기
					ClubBean club = clubService.oneClubInfo(clubBoardBean.getClub_id());
					String clubName = club.getClub_name().replaceAll("[^a-zA-Z0-9가-힣]", "_"); // 특수문자 제거

					// ✅ 저장 경로 생성
					String uploadPath = "C:/upload/image/clubBoardImg/" + clubName + "/";
					File uploadDir = new File(uploadPath);
					if (!uploadDir.exists()) {
						uploadDir.mkdirs();
					}

					// ✅ 파일명 생성 및 저장
					String imageFileName = UUID.randomUUID().toString() + "_" + originalFilename;
					File destFile = new File(uploadPath, imageFileName);
					image.transferTo(destFile);

					// ✅ 파일이 정상적으로 저장되었는지 확인
					if (destFile.exists()) {
						System.out.println("📌 게시글 이미지 저장 완료: " + uploadPath + imageFileName);

						// ✅ 게시글 객체에 이미지 경로 설정 (DB 저장용)
						clubBoardBean.setBoard_img("upload/image/clubBoardImg/" + clubName + "/" + imageFileName);
					} else {
						System.out.println("🚨 파일 저장 실패: " + imageFileName);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("🚨 게시글 이미지 저장 중 오류 발생: " + e.getMessage());
			}
		}

		// ✅ 게시글 저장 (board_img 포함)
		clubService.createBoard(clubBoardBean);

		return "redirect:/club/club_info?club_id=" + clubBoardBean.getClub_id();
	}

	//동호회 수정페이지
	@GetMapping("edit")
	public String edit(@ModelAttribute ClubBean clubBean, @RequestParam("club_id") int club_id, Model model) {
		// 비즈니스 계정으로 로그인한 경우 접근 거부
		if (loginBusiness != null && loginBusiness.isLogin()) {
			return "redirect:/club/club_info?club_id=" + club_id;
		}

		// 현재 사용자가 해당 클럽의 관리자인지 확인
		ClubMemberBean clubMember = clubMemberService.getMemberInfo(club_id, loginMember.getMember_id());
		if (clubMember == null || !"master".equals(clubMember.getMember_role())) {
			return "redirect:/club/club_info?club_id=" + club_id;
		}

		clubBean = clubService.oneClubInfo(club_id);
		model.addAttribute("clubBean", clubBean);

		return "club/club_edit";
	}

	//동호회 수정
	@PostMapping("edit_pro")
	public String edit_pro(@ModelAttribute ClubBean clubBean,
						   @RequestParam(value = "clubImage", required = false) MultipartFile clubImage) {
		// 비즈니스 계정으로 로그인한 경우 접근 거부
		if (loginBusiness != null && loginBusiness.isLogin()) {
			return "redirect:/club/club_info?club_id=" + clubBean.getClub_id();
		}

		// 현재 사용자가 해당 클럽의 관리자인지 확인
		ClubMemberBean clubMember = clubMemberService.getMemberInfo(clubBean.getClub_id(), loginMember.getMember_id());
		if (clubMember == null || !"master".equals(clubMember.getMember_role())) {
			return "redirect:/club/club_info?club_id=" + clubBean.getClub_id();
		}

		// 기존 클럽 정보 가져오기 (기존 프로필 이미지 정보 유지를 위해)
		ClubBean existingClub = clubService.oneClubInfo(clubBean.getClub_id());

		// 새 이미지가 업로드된 경우에만 처리
		if (clubImage != null && !clubImage.isEmpty()) {
			try {
				String originalFilename = clubImage.getOriginalFilename();
				if (originalFilename != null && !originalFilename.isEmpty()) {
					String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
					List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif");

					if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
						System.out.println("🚨 허용되지 않은 파일 확장자: " + fileExtension);
						return "redirect:/club/edit?club_id=" + clubBean.getClub_id() + "&error=invalid_file_type";
					}

					// 디렉토리 생성
					File uploadDir = new File(UPLOAD_DIR);
					if (!uploadDir.exists()) {
						boolean dirCreated = uploadDir.mkdirs();
						System.out.println("📁 클럽 프로필 폴더 생성됨: " + dirCreated);
					}

					// 기존 파일 삭제 (있는 경우)
					if (existingClub.getClub_profile() != null) {
						File oldFile = new File(UPLOAD_DIR + existingClub.getClub_profile());
						if (oldFile.exists()) {
							boolean deleted = oldFile.delete();
							System.out.println("🗑️ 기존 프로필 이미지 삭제: " + deleted);
						}
					}

					// 새 파일명 생성 및 저장
					String profileFileName = UUID.randomUUID().toString() + "_" + originalFilename;
					File destFile = new File(UPLOAD_DIR + profileFileName);
					clubImage.transferTo(destFile);

					// 파일이 정상적으로 저장되었는지 확인
					if (destFile.exists()) {
						System.out.println("📌 클럽 프로필 저장 완료: " + profileFileName);
						clubBean.setClub_profile(profileFileName);
					} else {
						System.out.println("🚨 파일 저장 실패: " + profileFileName);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("🚨 클럽 프로필 저장 중 오류 발생: " + e.getMessage());
			}
		} else {
			// 이미지가 업로드되지 않았으면 기존 이미지 정보 유지
			clubBean.setClub_profile(existingClub.getClub_profile());
		}

		System.out.println("club_id : " + clubBean.getClub_id());
		System.out.println("club_profile : " + clubBean.getClub_profile());

		// 클럽 정보 업데이트
		clubService.editClub(clubBean);

		return "redirect:/club/club_info?club_id=" + clubBean.getClub_id();
	}

	// ✅ 게시글 삭제 처리
	@PostMapping("/board_delete")
	public String deleteBoard(@RequestParam("board_id") int boardId,
							  @RequestParam("club_id") int clubId) {
		// 비즈니스 계정으로 로그인한 경우 접근 거부
		if (loginBusiness != null && loginBusiness.isLogin()) {
			return "redirect:/club/club_info?club_id=" + clubId;
		}

		// 1️⃣ 게시글 정보 가져오기 (작성자 ID와 이미지 경로 확인)
		ClubBoardBean board = clubService.getBoardById(boardId);

		if (board == null) {
			System.out.println("🚨 삭제 실패: 게시글을 찾을 수 없음 (board_id: " + boardId + ")");
			return "redirect:/club/club_info?club_id=" + clubId;
		}

		// 2️⃣ 현재 로그인한 사용자의 역할 확인
		ClubMemberBean clubMember = clubMemberService.getMemberInfo(clubId, loginMember.getMember_id());

		if (clubMember == null) {
			System.out.println("🚨 삭제 실패: 해당 동호회의 회원이 아님 (member_id: " + loginMember.getMember_id() + ")");
			return "redirect:/club/club_info?club_id=" + clubId;
		}

		String memberRole = clubMember.getMember_role();

		// 3️⃣ 삭제 권한 확인 (관리자는 모든 게시글 삭제 가능, 일반 회원은 본인 게시글만 삭제 가능)
		if (!"master".equals(memberRole) && !board.getBoard_writer_id().equals(loginMember.getMember_id())) {
			System.out.println("🚨 삭제 실패: 권한 없음 (board_id: " + boardId + ", 작성자: " + board.getBoard_writer_id() + ")");
			return "redirect:/club/club_info?club_id=" + clubId;
		}

		// 4️⃣ 게시글에 첨부된 이미지가 있는 경우 삭제
		if (board.getBoard_img() != null) {
			String imagePath = "C:/upload/" + board.getBoard_img();
			File imageFile = new File(imagePath);

			if (imageFile.exists()) {
				if (imageFile.delete()) {
					System.out.println("🗑 게시글 이미지 삭제 완료: " + imagePath);
				} else {
					System.out.println("🚨 이미지 삭제 실패: " + imagePath);
				}
			}
		}

		// 5️⃣ DB에서 게시글 삭제
		clubService.deleteBoard(boardId);
		System.out.println("🗑 게시글 삭제 완료 (board_id: " + boardId + ")");

		// 6️⃣ 삭제 후 동호회 상세 페이지로 이동
		return "redirect:/club/club_info?club_id=" + clubId;
	}

	@GetMapping("/donate")
	public String donate(@RequestParam("clubId") int clubId, Model model) {
		// 동호회 정보 불러오기
		ClubBean clubBean = clubService.oneClubInfo(clubId);

		// 로그인된 회원의 포인트 정보 불러오기
		int memberPoint = loginMember.getMember_point();

		model.addAttribute("club", clubBean);
		model.addAttribute("memberPoint", memberPoint);

		return "club/club_donate";
	}

	@PostMapping("/donate_pro")
	public String donate_pro(@RequestParam("club_id") int clubId,
							 @RequestParam("donation_point") int donationPoint,
							 RedirectAttributes redirectAttributes) {

		// 로그인된 회원 정보
		String memberId = loginMember.getMember_id();
		int currentPoint = loginMember.getMember_point();

		// 포인트 부족한 경우 예외 처리
		if (currentPoint < donationPoint) {
			redirectAttributes.addFlashAttribute("error", "포인트가 부족합니다.");
			return "redirect:/club/donate?clubId=" + clubId;
		}

		// 1️⃣ 클럽 포인트 증가 + 회원 포인트 차감
		clubService.donateToClub(clubId, memberId, donationPoint);

		// 2️⃣ 세션 반영
		loginMember.setMember_point(currentPoint - donationPoint);

		// 3️⃣ 리다이렉트
		return "redirect:/club/club_info?club_id=" + clubId;
	}

	@PostMapping("/club/cancel_reservation")
	public String cancelReservation(@RequestParam("reservation_id") int reservation_id,
									@RequestParam("club_id") int club_id,
									Model model) {
		// 현재 로그인한 회원이 클럽 회장인지 확인
		ClubMemberBean clubMemberBean = clubMemberService.getMemberInfo(club_id, loginMember.getMember_id());

		if (clubMemberBean != null && "master".equals(clubMemberBean.getMember_role())) {
			// 예약 취소 로직 실행
			reservationService.cancelReservation(reservation_id);

			// 성공 메시지 추가 가능
			// model.addAttribute("message", "예약이 성공적으로 취소되었습니다.");
		} else {
			// 권한 없음 메시지 추가 가능
			// model.addAttribute("message", "예약 취소 권한이 없습니다.");
		}

		// 클럽 정보 페이지로 리다이렉트
		return "redirect:/club/club_info?club_id=" + club_id;
	}



}