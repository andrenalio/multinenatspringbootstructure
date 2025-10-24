package com.example.multitenant.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantInterceptor implements HandlerInterceptor {

	public static final String TENANT_HEADER = "X-Tenant-ID";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String tenantId = request.getHeader(TENANT_HEADER);
		if (tenantId == null || tenantId.isBlank()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}
		TenantContext.setCurrentTenant(tenantId.trim());
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		TenantContext.clear();
	}{

	}
}
