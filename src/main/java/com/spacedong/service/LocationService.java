package com.spacedong.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.spacedong.beans.LocationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;

@Service
public class LocationService {

	private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

	// Spring Boot 정적 리소스 경로
	private static final String RESOURCE_PATH = "static/sources/json/regionData.json";

	// 캐싱된 위치 데이터
	private List<LocationBean> cachedLocations;

	@PostConstruct
	public void init() {
		// 애플리케이션 시작 시 데이터 로드
		cachedLocations = loadLocationData();
		logger.info("애플리케이션 시작 시 지역 데이터 {} 개 로드 완료", cachedLocations.size());
	}

	private List<LocationBean> loadLocationData() {
		List<LocationBean> locations = new ArrayList<>();
		Gson gson = new Gson();
		try {
			ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);
			if (!resource.exists()) {
				logger.error("리소스 파일을 찾을 수 없습니다: {}", RESOURCE_PATH);
				return locations;
			}
			logger.info("리소스 파일을 찾았습니다: {}", resource.getFilename());

			try (InputStream inputStream = resource.getInputStream();
				 Reader reader = new InputStreamReader(inputStream)) {
				Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
				Map<String, List<String>> regionMap = gson.fromJson(reader, type);
				for (Map.Entry<String, List<String>> entry : regionMap.entrySet()) {
					String city = entry.getKey();
					for (String district : entry.getValue()) {
						locations.add(new LocationBean(city, district));
					}
				}
			}
		} catch (IOException e) {
			logger.error("JSON 파일을 읽는 중 오류 발생: {}", e.getMessage(), e);
		}
		return locations;
	}

	public List<LocationBean> getLocationData() {
		// 캐싱된 데이터 반환
		return cachedLocations;
	}

	// 디버깅용 메소드: 실제 리소스 경로 확인
	public String getResourcePath() {
		try {
			ClassPathResource resource = new ClassPathResource(RESOURCE_PATH);
			if (resource.exists()) {
				return "리소스 파일 존재: " + resource.getURL().toString();
			} else {
				return "리소스 파일을 찾을 수 없습니다: " + RESOURCE_PATH;
			}
		} catch (IOException e) {
			return "오류 발생: " + e.getMessage();
		}
	}
}