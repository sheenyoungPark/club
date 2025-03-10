package com.spacedong.interceptor;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import com.spacedong.beans.LocationBean;
import com.spacedong.service.LocationService;

public class LocationInterceptor implements HandlerInterceptor{
	
	private LocationService locationService;
	
	public LocationInterceptor(LocationService locationService) {
		this.locationService = locationService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		List<LocationBean> locatelist = locationService.getLocationData();
		request.setAttribute("locations", locatelist);
		return true;
	}

	
	
	
}
