package com.pedrosouzza.JBLsystem.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class JobTokenInterceptor implements HandlerInterceptor {
    private final String token;

    public JobTokenInterceptor(@Value("${jobs.fipe-precos.token:${FIPE_PRECOS_TOKEN:}}") String token) {
        this.token = token;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");
        String prefix = "Bearer ";
        if (token.isBlank() || authorization == null || !authorization.startsWith(prefix)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        byte[] expected = token.getBytes(StandardCharsets.UTF_8);
        byte[] supplied = authorization.substring(prefix.length()).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, supplied)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
