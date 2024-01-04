package com.example.minishop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${app.client.host}")
    private String clientHost;
    @Value("${app.base-path}")
    private String basePath;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
//                .cors(AbstractHttpConfigurer::disable)
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(request -> request
//                        .anyRequest().permitAll()
//                )
                .cors(cors -> cors
                        .configurationSource(request -> {
                            CorsConfiguration corsConfiguration = new CorsConfiguration();
                            corsConfiguration.setAllowedOrigins(List.of(
                                    clientHost
                            ));
                            corsConfiguration.setAllowedMethods(List.of(
                                    HttpMethod.GET.name(),
                                    HttpMethod.POST.name(),
                                    HttpMethod.PUT.name(),
                                    HttpMethod.PATCH.name(),
                                    HttpMethod.DELETE.name()
                            ));
                            corsConfiguration.setAllowedHeaders(List.of(
                                    "*"
                            ));
                            corsConfiguration.setAllowCredentials(true);
                            return corsConfiguration;
                        })
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(basePath + "/**")
                        .ignoringRequestMatchers("/health-check")
                )
                .authorizeHttpRequests(request -> request
                        .requestMatchers(basePath + "/**").permitAll()
                        .requestMatchers("/health-check").permitAll()
                )
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}