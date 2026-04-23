package com.ProyectoPOO.ProyectoPOO.security;

import com.ProyectoPOO.ProyectoPOO.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ApiSecurityInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        boolean isPublic = method.getMethod().isAnnotationPresent(PublicEndpoint.class)
                || method.getBeanType().isAnnotationPresent(PublicEndpoint.class);

        if (isPublic) {
            return true;
        }

        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            return true;
        }

        try {
            userService.validateSecurityHeaders(request.getHeader("Authorization"), request.getHeader(API_KEY_HEADER));
            return true;
        } catch (IllegalArgumentException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + ex.getMessage() + "\"}");
            return false;
        }
    }
}

