package org.springframework.samples.petclinic.featureflag.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

	@Autowired
	private AdminSession adminSession;

	@Override
	public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
		String uri = request.getRequestURI();

		// Allow login page and API endpoints
		if (uri.contains("/admin/login") || uri.startsWith("/api/")) {
			return true;
		}

		// Check if accessing admin pages
		if (uri.contains("/admin/flags")) {
			if (!adminSession.isAuthenticated()) {
				response.sendRedirect("/admin/login");
				return false;
			}
		}

		return true;
	}
}
