package com.spacedong.controller;

import java.io.File;
import java.util.*;

import com.spacedong.beans.*;
import com.spacedong.service.*;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private ClubService clubService;

    @Resource(name = "loginAdmin")
    private AdminBean loginAdmin;

    @Autowired
    private BusinessService businessService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BankService bankService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ClubMemberService clubMemberService;

    @Autowired
    private AdminNotificationService adminNotificationService;

    @Autowired
    private ChatService chatService;
    @Autowired
    private SessionService sessionService;

    @GetMapping("/init")
    public String admininit() {
        return "admin/init";
    }

    @GetMapping("/login")
    public String login(@ModelAttribute("tempLoginAdmin") AdminBean tempLoginAdmin,
                        Model model,
                        @ModelAttribute("adminId") String adminId) {
        // 리다이렉트된 관리자 ID가 있으면 폼에 설정
        if (adminId != null && !adminId.isEmpty()) {
            tempLoginAdmin.setAdmin_id(adminId);
            model.addAttribute("redirectedFromMember", true);
        }

        // 인터셉터에서 이미 로그인 상태를 확인하고 리다이렉트하므로 여기서는 체크하지 않음
        return "admin/login";
    }

    @PostMapping("/loginproc")
    public String loginPro(@Valid @ModelAttribute("tempLoginAdmin") AdminBean tempLoginAdmin,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return "admin/login";
        }

        // adminService.getLoginAdmin 메서드는 loginAdmin 객체를 설정함
        if (adminService.getLoginAdmin(tempLoginAdmin)) {
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("loginfail", true);
            return "admin/login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // 전체 회원 수 (회원 + 사업자) 가져오기
            int totalUsers = adminService.getTotalUsersCount();
            System.out.println("총 회원 수: " + totalUsers);
            model.addAttribute("totalMembers", totalUsers);

            // 회원 성장률
            double memberGrowthRate = adminService.getMemberGrowthRate();
            model.addAttribute("memberGrowthRate", memberGrowthRate);

            // 전체 동호회 수
            int totalClubs = adminService.getTotalClubsCount();
            model.addAttribute("totalClubs", totalClubs);

            // 동호회 성장률
            double clubGrowthRate = adminService.getClubGrowthRate();
            model.addAttribute("clubGrowthRate", clubGrowthRate);

            // 월별 회원 수 데이터 추가
            List<Map<String, Object>> monthlyMemberCounts = adminService.getMonthlyMemberCounts();
            model.addAttribute("monthlyMemberCounts", monthlyMemberCounts);

        } catch (Exception e) {
            System.err.println("대시보드 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("totalMembers", 0);
            model.addAttribute("memberGrowthRate", 0.0);
            model.addAttribute("totalClubs", 0);
            model.addAttribute("clubGrowthRate", 0.0);
            model.addAttribute("monthlyMemberCounts", new ArrayList<>());
        }
        return "admin/dashboard";
    }
    @GetMapping("/monthly_member_counts")
    @ResponseBody
    public Map<String, Object> getMonthlyMemberCounts() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Map<String, Object>> monthlyData = adminService.getMonthlyMemberCounts();
            response.put("success", true);
            response.put("data", monthlyData);
        } catch (Exception e) {
            System.err.println("월별 회원 수 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 동호회 관리 페이지
    @GetMapping("/club_management")
    public String clubManagement(Model model) {
        // 모든 동호회 정보 가져오기 (관리자용 메소드 사용)
        List<ClubBean> clubList = clubService.getAllClubForAdmin();
        model.addAttribute("clubList", clubList);

        return "admin/club_management";
    }

    // 동호회 검색 (AJAX) - GET과 POST 모두 지원
    @RequestMapping(value = "/searchClub", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> searchClub(@RequestParam("searchType") String searchType,
                                          @RequestParam("keyword") String keyword) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ClubBean> searchResults = clubService.searchClubs(searchType, keyword);
            response.put("success", true);
            response.put("clubList", searchResults);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 동호회 상세 정보
    @GetMapping("/club_detail")
    @ResponseBody
    public ClubBean getClubDetail(@RequestParam("club_id") int club_id, Model model) {
        System.out.println(club_id);
        return clubService.oneClubInfo(club_id);
    }

    // 동호회 승인
    @PostMapping("/approve_club")
    @ResponseBody
    public Map<String, Object> approveClub(@RequestParam("clubId") int club_id) {
        Map<String, Object> response = new HashMap<>();

        try {
            clubService.updateClubStatus(club_id, "PASS");
            response.put("success", true);
            List<ClubMemberBean> clubmembers =  clubMemberService.getClubMemberList(club_id);

            // 동호회 승인 알림 전송
            for(ClubMemberBean cmb : clubmembers) {
                adminNotificationService.sendApprovalNotification(cmb.getMember_id(), "MEMBER", "APPROVED", "동호회 생성", "");
            }
            //동호회 채팅방 생성
            ClubBean selectedclub = clubService.oneClubInfo(club_id);
            String masterId = clubMemberService.getMasterMember(club_id);
            chatService.getOrCreateClubChatRoom(club_id, masterId, "MEMBER");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }



        return response;
    }

    // 동호회 상태 변경
    @PostMapping("/update_club_status")
    @ResponseBody
    public Map<String, Object> updateClubStatus(@RequestParam("clubId") int club_id,
                                                @RequestParam("status") String status) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 상태가 public이면 PASS로, private이면 WAIT로 변환
            String dbStatus = "PASS";
            if (status.equals("private")) {
                dbStatus = "WAIT";
            }

            clubService.updateClubStatus(club_id, dbStatus);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 동호회 카테고리 분포 데이터 조회
    @GetMapping("/club_category_distribution")
    @ResponseBody
    public Map<String, Object> getClubCategoryDistribution() {
        Map<String, Object> response = new HashMap<>();

        try {
            // CategoryMapper의 categoryTypeCount 메서드를 사용하여 카테고리 분포 데이터 가져오기
            List<CategoryBean> categoryDistribution = categoryService.getCategoryTypeCount();
            response.put("success", true);
            response.put("data", categoryDistribution);
        } catch (Exception e) {
            System.err.println("카테고리 분포 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 판매자 목록 조회 메서드 (기존 메서드를 보완)
    @GetMapping("/business_management")
    public String businessManagement(Model model) {
        // 모든 판매자 정보 가져오기 (WAIT와 PASS 모두 포함)
        List<BusinessBean> businessList = businessService.getAllBusiness();
        model.addAttribute("businessList", businessList);

        return "admin/business_management";
    }

    @GetMapping("/board_management")
    public String boardManagement() {
        return "admin/board_management";
    }

    // 게시판 특정 유형 목록 조회 함수에 board_type 명시적 설정 추가
    @GetMapping("/board_list")
    @ResponseBody
    public Map<String, Object> getBoardList(@RequestParam(value = "boardType", required = false) String boardType) {
        Map<String, Object> response = new HashMap<>();
        List<BoardBean> boardList;

        try {
            // 특정 게시판 유형을 요청한 경우
            if (boardType != null && !boardType.equals("all")) {
                int page = 1; // 페이지 기본값
                int pageSize = 1000; // 충분히 큰 값으로 설정

                if (boardType.equals("admin")) {
                    boardList = boardService.getAdminBoardList(page, pageSize);

                    // 게시글 유형 명시적 설정
                    for (BoardBean board : boardList) {
                        board.setBoard_type("admin");
                    }
                } else if (boardType.equals("member")) {
                    boardList = boardService.getMemberBoardList(page, pageSize);

                    // 게시글 유형 명시적 설정
                    for (BoardBean board : boardList) {
                        board.setBoard_type("member");
                    }
                } else if (boardType.equals("business")) {
                    boardList = boardService.getBusinessBoardList(page, pageSize);

                    // 게시글 유형 명시적 설정
                    for (BoardBean board : boardList) {
                        board.setBoard_type("business");
                    }
                } else {
                    boardList = new ArrayList<>();
                }
            } else {
                // 모든 게시판의 게시글을 가져옴 (이미 board_type 필드가 설정되어 있음)
                boardList = boardService.getAllBoardList(1, 1000);
            }

            response.put("success", true);
            response.put("boardList", boardList);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "게시글 목록을 불러오는데 실패했습니다: " + e.getMessage());
        }

        return response;
    }

    // 게시글 검색 메서드
    @GetMapping("/searchBoard")
    @ResponseBody
    public Map<String, Object> searchBoard(@RequestParam("searchType") String searchType,
                                           @RequestParam("keyword") String keyword) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 검색 로직 (BoardService의 검색 메서드 호출)
            List<BoardBean> searchResults = boardService.searchBoards(searchType, keyword);

            // 검색 결과에 board_type이 없을 경우 설정
            for (BoardBean board : searchResults) {
                if (board.getBoard_type() == null || board.getBoard_type().isEmpty()) {
                    // 게시글 ID로 타입 확인
                    String detectedType = boardService.findBoardType(board.getBoard_id());
                    if (detectedType != null && !detectedType.isEmpty()) {
                        board.setBoard_type(detectedType);
                    }
                }
            }

            response.put("success", true);
            response.put("boardList", searchResults);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 게시글 상세 정보
    @GetMapping("/board_detail")
    @ResponseBody
    public Map<String, Object> getBoardDetail(@RequestParam("boardId") int boardId,
                                              @RequestParam("boardType") String boardType) {
        Map<String, Object> response = new HashMap<>();

        try {
            // boardType이 null이거나 유효하지 않은 경우 처리
            if (boardType == null || boardType.isEmpty() ||
                    !(boardType.equals("member") || boardType.equals("business") || boardType.equals("admin"))) {
                // 게시글 ID로 올바른 게시판 유형 찾기
                boardType = boardService.findBoardType(boardId);

                if (boardType == null) {
                    response.put("success", false);
                    response.put("message", "유효한 게시판 유형을 찾을 수 없습니다.");
                    return response;
                }
            }

            // 게시글 정보 가져오기
            BoardBean board = boardService.getBoardDetail(boardType, boardId);

            if (board == null) {
                response.put("success", false);
                response.put("message", "게시글을 찾을 수 없습니다.");
                return response;
            }

            // board_type이 없으면 설정
            if (board.getBoard_type() == null || board.getBoard_type().isEmpty()) {
                board.setBoard_type(boardType);
            }

            // 기본 게시글 정보 저장
            response.put("success", true);
            response.put("board_id", board.getBoard_id());
            response.put("board_title", board.getBoard_title());
            response.put("board_text", board.getBoard_text());
            response.put("board_writer_id", board.getBoard_writer_id());
            response.put("board_view", board.getBoard_view());
            response.put("board_like", board.getBoard_like());
            response.put("create_date", board.getCreate_date());
            response.put("update_date", board.getUpdate_date());
            response.put("writer_name", board.getWriter_name());
            response.put("board_type", boardType);

            // 이미지 가져오기
            List<String> images = boardService.getBoardImages(boardType, boardId);
            response.put("images", images);

            // 댓글 가져오기
            try {
                List<BoardCommentBean> commentList = boardService.getCommentHierarchy(boardType, boardId);
                // BoardCommentBean 객체를 Map으로 변환 (JSON 직렬화를 돕기 위함)
                List<Map<String, Object>> commentsForJson = new ArrayList<>();

                if (commentList != null) {
                    for (BoardCommentBean comment : commentList) {
                        Map<String, Object> commentMap = new HashMap<>();
                        commentMap.put("comment_id", comment.getComment_id());
                        commentMap.put("board_id", comment.getBoard_id());
                        commentMap.put("comment_writer_id", comment.getComment_writer_id());
                        commentMap.put("comment_writer_name", comment.getComment_writer_name());
                        commentMap.put("comment_text", comment.getComment_text());
                        commentMap.put("parent_comment_id", comment.getParent_comment_id());
                        commentMap.put("create_date", comment.getCreate_date());

                        commentsForJson.add(commentMap);
                    }
                }

                response.put("comments", commentsForJson);
                System.out.println("댓글 수: " + (commentList != null ? commentList.size() : 0));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("댓글 불러오기 실패: " + e.getMessage());
                response.put("comments", new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "게시글 상세 정보를 불러오는데 실패했습니다: " + e.getMessage());
        }

        return response;
    }

    // 게시글 표시/숨김 토글
    @PostMapping("/toggle_board_visibility")
    @ResponseBody
    public Map<String, Object> toggleBoardVisibility(@RequestParam("boardId") int boardId,
                                                     @RequestParam("boardType") String boardType,
                                                     @RequestParam("action") String action) {
        Map<String, Object> response = new HashMap<>();

        try {
            // TODO: 게시글 표시/숨김 상태 변경 로직 구현 필요
            // 현재는 데이터베이스에 status 컬럼이 없으므로 추가 작업 필요
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 게시글 삭제
    @PostMapping("/delete_board")
    @ResponseBody
    public Map<String, Object> deleteBoard(@RequestParam("boardId") int boardId,
                                           @RequestParam("boardType") String boardType) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 게시글 삭제 (BoardService의 삭제 메소드는 이미 이미지 파일까지 처리함)
            boardService.deleteBoard(boardType, boardId);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 공지사항 작성
    @PostMapping("/write_notice")
    @ResponseBody
    public Map<String, Object> writeNotice(@RequestParam("title") String title,
                                           @RequestParam("content") String content,
                                           @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 게시글 데이터 생성
            BoardBean notice = new BoardBean();
            notice.setBoard_title(title);
            notice.setBoard_text(content);
            notice.setBoard_writer_id(loginAdmin.getAdmin_id()); // 관리자 ID로 설정

            // 게시글 저장
            int boardId = boardService.writeBoard("admin", notice);

            // 파일이 있으면 저장
            if (files != null && !files.isEmpty()) {
                String uploadDir = "C:/upload/image/adminBoardImg/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }

                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        // 파일명 생성 (중복 방지)
                        String originalFilename = file.getOriginalFilename();
                        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

                        // 파일 저장
                        File destFile = new File(uploadDir + fileName);
                        file.transferTo(destFile);

                        // DB에 이미지 정보 저장
                        boardService.saveBoardImage("admin", boardId, fileName);
                    }
                }
            }

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 판매자 승인 메서드
    @PostMapping("/approve_business")
    @ResponseBody
    public Map<String, Object> approveBusiness(@RequestParam("businessId") String businessId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 판매자 상태를 'PASS'로 변경
            businessService.updateBusinessStatus(businessId, "PASS");
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 판매자 상태 업데이트 메서드
    @PostMapping("/update_business_status")
    @ResponseBody
    public Map<String, Object> updateBusinessStatus(@RequestParam("businessId") String businessId,
                                                    @RequestParam("status") String status) {
        Map<String, Object> response = new HashMap<>();

        try {
            businessService.updateBusinessStatus(businessId, status);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 판매자 삭제 메서드
    @PostMapping("/delete_business")
    @ResponseBody
    public Map<String, Object> deleteBusiness(@RequestParam("businessId") String businessId) {
        Map<String, Object> response = new HashMap<>();

        try {
            businessService.deleteBusiness(businessId);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 회원 관리 페이지
    @GetMapping("/member_management")
    public String memberManagement() {
        return "admin/member_management";
    }

    // 회원 목록 데이터 (JSON 형식으로 반환)
    @GetMapping("/member_list")
    @ResponseBody
    public Map<String, Object> getMemberList() {
        Map<String, Object> response = new HashMap<>();
        List<MemberBean> memberList = memberService.getAllMembers();
        response.put("success", true);
        response.put("memberList", memberList);
        return response;
    }

    // 회원 검색 (AJAX) - GET과 POST 모두 지원
    @RequestMapping(value = "/searchMember", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> searchMember(@RequestParam("searchType") String searchType,
                                            @RequestParam("keyword") String keyword) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<MemberBean> searchResults = memberService.searchMembers(searchType, keyword);
            response.put("success", true);
            response.put("memberList", searchResults);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 회원 상세 정보
    @GetMapping("/member_detail")
    @ResponseBody
    public MemberBean getMemberDetail(@RequestParam("member_id") String member_id) {
        return memberService.getMemberById(member_id);
    }

    // 회원 삭제
    @PostMapping("/delete_member")
    @ResponseBody
    public Map<String, Object> deleteMember(@RequestParam("memberId") String member_id) {
        Map<String, Object> response = new HashMap<>();

        try {
            memberService.deleteMember(member_id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    @GetMapping("/logout")
    public String adminlogout() {
        loginAdmin.setAdmin_login(false);
        loginAdmin.setAdmin_id(null);
        loginAdmin.setAdmin_name(null);

        return "admin/logout";
    }

    // 판매자(비즈니스) 검색 (AJAX) - GET과 POST 모두 지원
    @RequestMapping(value = "/searchBusiness", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> searchBusiness(@RequestParam("searchType") String searchType,
                                              @RequestParam("keyword") String keyword) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<BusinessBean> searchResults = businessService.searchBusiness(searchType, keyword);
            response.put("success", true);
            response.put("businessList", searchResults);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 판매자(비즈니스) 상세 정보
    @GetMapping("/business_detail")
    @ResponseBody
    public BusinessBean getBusinessDetail(@RequestParam("business_id") String business_id) {
        return businessService.getBusinessDetailInfo(business_id);
    }

    // 동호회 목록 데이터 (JSON 형식으로 반환)
    @GetMapping("/club_list")
    @ResponseBody
    public Map<String, Object> getClubList() {
        Map<String, Object> response = new HashMap<>();
        // getAllClubForAdmin() 메소드를 사용하도록 수정
        List<ClubBean> clubList = clubService.getAllClubForAdmin();
        response.put("clubList", clubList);
        return response;
    }

    // 환전 관리 페이지
    @GetMapping("/exchange_management")
    public String exchangeManagement(Model model) {
        try {
            // 모든 환전 요청 목록 가져오기
            List<BankBean> exchangeRequests = bankService.getAllExchangeRequests();
            model.addAttribute("exchangeRequests", exchangeRequests);
        } catch (Exception e) {
            model.addAttribute("error", "환전 요청 목록을 불러오는데 실패했습니다: " + e.getMessage());
        }

        return "admin/exchange_management";
    }

    // 환전 요청 상세 정보
    @GetMapping("/exchange_detail")
    @ResponseBody
    public BankBean getExchangeDetail(@RequestParam("bank_id") int bankId) {
        return bankService.getExchangeRequestById(bankId);
    }

    // 환전 요청 승인
    @PostMapping("/approve_exchange")
    @ResponseBody
    public Map<String, Object> approveExchange(@RequestParam("bankId") int bankId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 환전 요청 승인 처리
            bankService.approveExchangeRequest(bankId, loginAdmin.getAdmin_id());
            String business_id = bankService.getBusinessIdByBankId(bankId);
            adminNotificationService.sendApprovalNotification(business_id, "BUSINESS", "APPROVED", "환전", "");
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    // 환전 요청 거부
    @PostMapping("/reject_exchange")
    @ResponseBody
    public Map<String, Object> rejectExchange(@RequestParam("bankId") int bankId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 환전 요청 거부 처리
            String business_id = bankService.getBusinessIdByBankId(bankId);
            bankService.deleteExchangeRequest(bankId);
            adminNotificationService.sendApprovalNotification(business_id, "BUSINESS", "REJECTED1", "환전", "관리자 취소");
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
    // 대기 중인 판매자 목록 조회
    @GetMapping("/pending_business")
    @ResponseBody
    public Map<String, Object> getPendingBusiness() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 승인 대기 중인 판매자만 필터링
            List<BusinessBean> businessList = businessService.getAllBusiness().stream()
                    .filter(b -> "WAIT".equals(b.getBusiness_public()))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("businessList", businessList);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
    /**
     * 대기 중인 클럽 목록 조회
     */
    @GetMapping("/pending_clubs")
    @ResponseBody
    public Map<String, Object> getPendingClubs() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 승인 대기 중인 클럽만 필터링 (club_public = 'WAIT')
            List<ClubBean> clubList = clubService.getAllClubForAdmin().stream()
                    .filter(c -> "WAIT".equals(c.getClub_public()))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("clubList", clubList);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }
    /**
     * 승인 대기 건수 API
     * - 전체 승인 대기 건수
     * - 오늘 신규 승인 대기 건수
     */
    @GetMapping("/pending_counts")
    @ResponseBody
    public Map<String, Object> getPendingCounts() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 현재 날짜 생성 (오늘 00:00:00)
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date today = calendar.getTime();

            // 1. 클럽 데이터 가져오기
            List<ClubBean> allClubs = clubService.getAllClubForAdmin();

            // 2. 승인 대기 중인 클럽 필터링
            List<ClubBean> pendingClubs = allClubs.stream()
                    .filter(c -> "WAIT".equals(c.getClub_public()))
                    .collect(Collectors.toList());

            // 3. 오늘 생성된 승인 대기 클럽 필터링
            List<ClubBean> todayNewClubs = pendingClubs.stream()
                    .filter(c -> c.getClub_joindate().after(today))
                    .collect(Collectors.toList());

            // 4. 판매자 데이터 가져오기
            List<BusinessBean> allBusinesses = businessService.getAllBusiness();

            // 5. 승인 대기 중인 판매자 필터링
            List<BusinessBean> pendingBusinesses = allBusinesses.stream()
                    .filter(b -> "WAIT".equals(b.getBusiness_public()))
                    .collect(Collectors.toList());

            // 6. 오늘 생성된 승인 대기 판매자 필터링
            List<BusinessBean> todayNewBusinesses = pendingBusinesses.stream()
                    .filter(b -> b.getBusiness_joindate().after(today))
                    .collect(Collectors.toList());

            // 7. 전체 승인 대기 건수 계산 (클럽 + 판매자)
            int totalPending = pendingClubs.size() + pendingBusinesses.size();

            // 8. 오늘 신규 승인 대기 건수 계산 (클럽 + 판매자)
            int todayNew = todayNewClubs.size() + todayNewBusinesses.size();

            response.put("success", true);
            response.put("totalPending", totalPending);
            response.put("todayNew", todayNew);
            response.put("pendingClubs", pendingClubs.size());
            response.put("pendingBusinesses", pendingBusinesses.size());
            response.put("todayNewClubs", todayNewClubs.size());
            response.put("todayNewBusinesses", todayNewBusinesses.size());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

}