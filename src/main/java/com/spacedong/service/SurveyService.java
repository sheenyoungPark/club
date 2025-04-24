package com.spacedong.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.spacedong.beans.QuestionBean;
import com.spacedong.beans.SpaceType;
import com.spacedong.beans.SpaceTypeData;
import com.spacedong.beans.SurveyData;

@Service
public class SurveyService {

    private static final Logger logger = LoggerFactory.getLogger(SurveyService.class);

    private SurveyData surveyData;
    private SpaceTypeData spaceTypeData;

    // Spring Boot 정적 리소스 경로
    private static final String SURVEY_RESOURCE_PATH = "static/sources/json/space_survey.json";
    private static final String SPACE_TYPE_RESOURCE_PATH = "static/sources/json/space_type.json";

    @PostConstruct
    public void init() {
        loadSurveyData();
        loadSpaceTypeData();
    }

    private void loadSurveyData() {
        try {
            ClassPathResource resource = new ClassPathResource(SURVEY_RESOURCE_PATH);
            if (!resource.exists()) {
                logger.error("리소스 파일을 찾을 수 없습니다: {}", SURVEY_RESOURCE_PATH);
                return;
            }
            logger.info("리소스 파일을 찾았습니다: {}", resource.getFilename());
            try (InputStream inputStream = resource.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                surveyData = mapper.readValue(inputStream, SurveyData.class);
                logger.info("설문 데이터 로드 완료: {} 질문", surveyData.getQuestions().size());
            }
        } catch (IOException e) {
            logger.error("설문 데이터 로드 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    private void loadSpaceTypeData() {
        try {
            ClassPathResource resource = new ClassPathResource(SPACE_TYPE_RESOURCE_PATH);
            if (!resource.exists()) {
                logger.error("리소스 파일을 찾을 수 없습니다: {}", SPACE_TYPE_RESOURCE_PATH);
                return;
            }
            logger.info("리소스 파일을 찾았습니다: {}", resource.getFilename());
            try (InputStream inputStream = resource.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                spaceTypeData = mapper.readValue(inputStream, SpaceTypeData.class);
                logger.info("공간 타입 데이터 로드 완료: {} 타입", spaceTypeData.getSpacetypes().size());
            }
        } catch (IOException e) {
            logger.error("공간 타입 데이터 로드 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public QuestionBean getQuestionByNum(int num) {
        if (surveyData == null || surveyData.getQuestions() == null) {
            logger.error("설문 데이터가 null이거나 질문이 없습니다.");
            return null;
        }
        for (QuestionBean question : surveyData.getQuestions()) {
            if (question.getNum() == num) {
                return question;
            }
        }
        return null; // 해당 번호의 문제가 없을 경우
    }

    public int getTotalQuestions() {
        if (surveyData == null || surveyData.getQuestions() == null) {
            return 0;
        }
        return surveyData.getQuestions().size();
    }

    public boolean hasNextQuestion(int currentNum) {
        return currentNum <= getTotalQuestions();
    }

    public boolean hasPreviousQuestion(int currentNum) {
        return currentNum > 1;
    }

    public String getSurveyTitle() {
        return "우주 성격 유형 테스트: 당신은 어떤 탐험가인가요?";
    }

    public String getSurveyStory() {
        return "우티와 함께 먼 우주의 미지의 행성을 탐사하러 떠나는 여정! \n 이 모험에서 당신은 어떤 선택을 할까요? \n당신의 선택이 곧 당신의 우주 성격 유형을 결정합니다.";
    }

    public Map<String, Integer> calculateResult(Map<Integer, String> memberAnswers) {
        Map<String, Integer> typeCount = new HashMap<>();
        typeCount.put("탐험가", 0);
        typeCount.put("관리자", 0);
        typeCount.put("교류가", 0);
        typeCount.put("분석가", 0);

        Map<String, String> answerMapping = Map.of(
                "A", "탐험가",
                "B", "관리자",
                "C", "교류가",
                "D", "분석가"
        );

        for (String answer : memberAnswers.values()) {
            String type = answerMapping.get(answer);
            if (type != null) {
                typeCount.put(type, typeCount.get(type) + 1);
            }
        }
        return typeCount;
    }

    public List<String> findTopTypes(Map<String, Integer> typeCount) {
        int maxCount = Collections.max(typeCount.values());
        return typeCount.entrySet().stream()
                .filter(entry -> entry.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private String mapAnswerToType(int questionNumber, String answer) {
        // 실제 로직에 맞게 구현 필요
        // 현재는 예시로만 구현
        List<String> allTypes = getAllTypeNames();
        if (allTypes.isEmpty()) {
            return "탐험가"; // 기본값
        }

        // 응답과 질문 번호의 조합에 따라 유형 결정
        // 이 부분은 실제 설문 매핑 로직으로 대체해야 함
        int index = (questionNumber + answer.hashCode()) % allTypes.size();
        if (index < 0) index += allTypes.size();
        return allTypes.get(index);
    }

    public List<String> getAllTypeNames() {
        return getAllSpaceTypes().stream()
                .map(SpaceType::getType)
                .collect(Collectors.toList());
    }

    public List<SpaceType> getAllSpaceTypes() {
        if (spaceTypeData == null || spaceTypeData.getSpacetypes() == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(spaceTypeData.getSpacetypes());
    }

    public SpaceType getSpaceTypeByName(String type) {
        if (spaceTypeData == null || spaceTypeData.getSpacetypes() == null) {
            return null;
        }

        return spaceTypeData.getSpacetypes().stream()
                .filter(st -> st.getType().equals(type))
                .findFirst()
                .orElse(null);
    }

    public SpaceTypeData getSpaceTypeData() {
        return spaceTypeData;
    }

    // 디버깅용 메소드: 실제 리소스 경로 확인
    public String getSurveyResourcePath() {
        try {
            ClassPathResource resource = new ClassPathResource(SURVEY_RESOURCE_PATH);
            if (resource.exists()) {
                return "리소스 파일 존재: " + resource.getURL().toString();
            } else {
                return "리소스 파일을 찾을 수 없습니다: " + SURVEY_RESOURCE_PATH;
            }
        } catch (IOException e) {
            return "오류 발생: " + e.getMessage();
        }
    }

    public String getSpaceTypeResourcePath() {
        try {
            ClassPathResource resource = new ClassPathResource(SPACE_TYPE_RESOURCE_PATH);
            if (resource.exists()) {
                return "리소스 파일 존재: " + resource.getURL().toString();
            } else {
                return "리소스 파일을 찾을 수 없습니다: " + SPACE_TYPE_RESOURCE_PATH;
            }
        } catch (IOException e) {
            return "오류 발생: " + e.getMessage();
        }
    }
}