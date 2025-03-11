package com.spacedong.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spacedong.beans.QuestionBean;
import com.spacedong.beans.SpaceType;
import com.spacedong.service.SurveyService;

@Controller
@RequestMapping("/survey")
public class SurveyController {

    @Autowired
    private SurveyService surveyService;

    @GetMapping("/start")
    public String startSurvey(Model model, HttpSession session) {
        // 설문 시작 - 첫 번째 문제 표시
        session.setAttribute("currentQuestionNum", 1);
        session.setAttribute("memberAnswers", new HashMap<Integer, String>());

        QuestionBean question = surveyService.getQuestionByNum(1);
        model.addAttribute("question", question);
        model.addAttribute("title", surveyService.getSurveyTitle());
        model.addAttribute("story", surveyService.getSurveyStory());
        model.addAttribute("totalQuestions", surveyService.getTotalQuestions());

        return "survey/test";
    }


    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submitAnswer(
            @RequestParam("questionNum") int questionNum,
            @RequestParam("answer") String answer,
            HttpSession session,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        // 사용자 답변 저장
        Map<Integer, String> memberAnswers = (Map<Integer, String>) session.getAttribute("memberAnswers");
        if (memberAnswers == null) {
            memberAnswers = new HashMap<>();
        }
        memberAnswers.put(questionNum, answer);
        session.setAttribute("memberAnswers", memberAnswers);

        // 다음 문제 번호 계산
        int nextQuestionNum = questionNum + 1;
        session.setAttribute("currentQuestionNum", nextQuestionNum);

        if (surveyService.hasNextQuestion(nextQuestionNum)) {
            // 다음 문제가 있는 경우
            QuestionBean nextQuestion = surveyService.getQuestionByNum(nextQuestionNum);

            // JSON 응답에 다음 문제 정보 추가
            response.put("hasNext", true);
            response.put("nextQuestion", nextQuestion);
        } else {
            // 모든 문제를 다 푼 경우
            response.put("hasNext", false);
            response.put("redirectUrl", request.getContextPath() + "/survey/result");
        }

        return response;
    }

    /**
     * 이전 질문으로 돌아가기
     */
    @PostMapping("/previous")
    @ResponseBody
    public Map<String, Object> getPreviousQuestion(
            @RequestParam("currentQuestionNum") int currentQuestionNum,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 이전 질문 번호 계산
        int previousQuestionNum = currentQuestionNum - 1;
        if (previousQuestionNum < 1) {
            previousQuestionNum = 1; // 1보다 작으면 첫 번째 질문으로
        }

        // 세션에 현재 문제 번호 업데이트
        session.setAttribute("currentQuestionNum", previousQuestionNum);

        // 이전 질문 가져오기
        QuestionBean previousQuestion = surveyService.getQuestionByNum(previousQuestionNum);
        response.put("previousQuestion", previousQuestion);

        return response;
    }

    @GetMapping("/getAnswers")
    @ResponseBody
    public Map<String, Object> getMemberAnswers(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Map<Integer, String> memberAnswers = (Map<Integer, String>) session.getAttribute("memberAnswers");
        if (memberAnswers == null) {
            memberAnswers = new HashMap<>();
        }

        response.put("answers", memberAnswers);
        return response;
    }

    @GetMapping("/result")
    public String showResult(Model model, HttpSession session) {
        Map<Integer, String> memberAnswers = (Map<Integer, String>) session.getAttribute("memberAnswers");
        model.addAttribute("memberAnswers", memberAnswers);

        if (memberAnswers == null || memberAnswers.isEmpty()) {
            model.addAttribute("message", "응답이 없습니다. 설문을 완료해주세요.");
            return "survey/result";
        }

        // 결과 계산 로직
        Map<String, Integer> typeCount = surveyService.calculateResult(memberAnswers);
        model.addAttribute("typeCount", typeCount);

        // 가장 높은 점수의 유형 찾기
        List<String> topTypes = surveyService.findTopTypes(typeCount);

        if (topTypes.size() == 1) {
            String resultType = topTypes.get(0);
            model.addAttribute("topResultType", resultType);

            // 결과 유형에 해당하는 SpaceType 객체 찾기
            SpaceType resultSpaceType = surveyService.getSpaceTypeByName(resultType);

            if (resultSpaceType != null) {
                // 결과 유형의 상세 정보를 모델에 추가
                model.addAttribute("resultPlanet", resultSpaceType.getPlanet());
                model.addAttribute("resultDescription", resultSpaceType.getDescription());
                model.addAttribute("resultCategories", resultSpaceType.getCategories());
                model.addAttribute("resultActivities", resultSpaceType.getActivities());
                model.addAttribute("resultRoles", resultSpaceType.getRoles());
                model.addAttribute("resultGoodCompanion", resultSpaceType.getGoodcompanion());

                // 좋은 동반자 유형에 대한 정보도 함께 제공
                SpaceType companionSpaceType = surveyService.getSpaceTypeByName(
                        resultSpaceType.getGoodcompanion().split(" ")[0]);
                if (companionSpaceType != null) {
                    model.addAttribute("companionType", companionSpaceType.getType());
                    model.addAttribute("companionPlanet", companionSpaceType.getPlanet());
                    model.addAttribute("companionDescription", companionSpaceType.getDescription());
                }
            }
        } else {
            // 동률 시 선택지 제공을 위한 정보 모델에 추가
            model.addAttribute("tieTypes", topTypes);
            model.addAttribute("tieCount", topTypes.size());

            // 각 타입별 정보 제공
            for (String type : topTypes) {
                SpaceType spaceType = surveyService.getSpaceTypeByName(type);
                if (spaceType != null) {
                    model.addAttribute(type + "Planet", spaceType.getPlanet());
                    model.addAttribute(type + "Description", spaceType.getDescription());
                }
            }

            return "survey/tiebreaker"; // 동률 시 사용자 선택을 위한 JSP 페이지
        }

        return "survey/result";
    }

    @PostMapping("/tiebreakerproc")
    public String submitTieBreaker(@RequestParam("selectedType") String selectedType, Model model, HttpSession session) {
        model.addAttribute("topResultType", selectedType);
        Map<Integer, String> memberAnswers = (Map<Integer, String>) session.getAttribute("memberAnswers");
        Map<String, Integer> typeCount = surveyService.calculateResult(memberAnswers);
        model.addAttribute("typeCount", typeCount);

        // 선택한 유형에 해당하는 SpaceType 객체 찾기
        SpaceType resultSpaceType = surveyService.getSpaceTypeByName(selectedType);

        if (resultSpaceType != null) {
            // 결과 유형의 상세 정보를 모델에 추가
            model.addAttribute("resultPlanet", resultSpaceType.getPlanet());
            model.addAttribute("resultDescription", resultSpaceType.getDescription());
            model.addAttribute("resultCategories", resultSpaceType.getCategories());
            model.addAttribute("resultActivities", resultSpaceType.getActivities());
            model.addAttribute("resultRoles", resultSpaceType.getRoles());
            model.addAttribute("resultGoodCompanion", resultSpaceType.getGoodcompanion());

            // 좋은 동반자 유형에 대한 정보도 함께 제공
            SpaceType companionSpaceType = surveyService.getSpaceTypeByName(
                    resultSpaceType.getGoodcompanion().split(" ")[0]);
            if (companionSpaceType != null) {
                model.addAttribute("companionType", companionSpaceType.getType());
                model.addAttribute("companionPlanet", companionSpaceType.getPlanet());
                model.addAttribute("companionDescription", companionSpaceType.getDescription());
            }
        }

        return "survey/result";  // 최종 결과 페이지
    }
}