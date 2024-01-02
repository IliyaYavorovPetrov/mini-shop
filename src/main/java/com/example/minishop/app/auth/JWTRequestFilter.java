package com.example.minishop.app.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {
    private final JWTUtils jwtUtils;

    public JWTRequestFilter(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request.getRequestURI().equals("/api/v0/sign-up") || request.getRequestURI().equals("/api/v0/sign-in")) {
            filterChain.doFilter(request, response);
        }

        List<Cookie> cookies = (request.getCookies() != null)
                ? Arrays.asList(request.getCookies())
                : List.of();

        boolean containsAuthBothCookies = cookies.stream()
                .anyMatch(cookie -> cookie.getName().equals(JWTUtils.COOKIE_AUTH_TOKEN_NAME)) &&
                cookies.stream().anyMatch(cookie -> cookie.getName().equals(JWTUtils.COOKIE_AUTH_PROVIDER_NAME));

//        if (!containsAuthBothCookies) {
//            throw new ServletException("Needed information is not available");
//        }

        String authToken = cookies.stream()
                .filter(x -> x.getName().equals(JWTUtils.COOKIE_AUTH_TOKEN_NAME))
                .findFirst()
                .map(Cookie::getValue)
                .orElse("");

        AuthProviderType authProvider = AuthMapper.getAuthProviderTypeFromStringOrDefault(
                cookies.stream()
                        .filter(x -> x.getName().equals(JWTUtils.COOKIE_AUTH_PROVIDER_NAME))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse("")
        );

//        if (jwtUtils.verifyAndGetAuthentication(authToken, authProvider) == null) {
//            throw new ServletException("Authentication failed.");
//        }

        filterChain.doFilter(request, response);
    }
}