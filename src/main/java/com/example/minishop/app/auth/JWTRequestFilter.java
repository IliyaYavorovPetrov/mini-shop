package com.example.minishop.app.auth;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {
    @Value("${app.base-path}")
    private String basePath;
    private final List<String> noAuthEndpoints;
    private final AuthService authService;

    public JWTRequestFilter(AuthService authService) {
        this.noAuthEndpoints = new ArrayList<>();
        this.authService = authService;
    }

    @PostConstruct
    private void initializeNoAuthEndpoints() {
        noAuthEndpoints.add("/health-check");
        noAuthEndpoints.add(basePath + "/sign-up");
        noAuthEndpoints.add(basePath + "/sign-in");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        boolean doesNotNeedAuth = noAuthEndpoints.stream()
                .anyMatch(x -> x.equals(request.getServletPath()));

        if (!doesNotNeedAuth) {
            List<Cookie> cookies = (request.getCookies() != null)
                    ? Arrays.asList(request.getCookies())
                    : List.of();

            boolean containsAuthBothCookies = cookies.stream()
                    .anyMatch(cookie -> cookie.getName().equals(AuthService.COOKIE_AUTH_TOKEN_NAME)) &&
                    cookies.stream().anyMatch(cookie -> cookie.getName().equals(AuthService.COOKIE_AUTH_PROVIDER_NAME));

            if (!containsAuthBothCookies) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access Forbidden");
                return;
            }

            String authToken = cookies.stream()
                    .filter(x -> x.getName().equals(AuthService.COOKIE_AUTH_TOKEN_NAME))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse("");

            AuthProviderType authProvider = AuthMapper.getAuthProviderTypeFromStringOrDefault(
                    cookies.stream()
                            .filter(x -> x.getName().equals(AuthService.COOKIE_AUTH_PROVIDER_NAME))
                            .findFirst()
                            .map(Cookie::getValue)
                            .orElse("")
            );

            if (!authService.verifyIfJWTTokenIsValid(authToken, authProvider)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access Forbidden");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}