package com.wecredit.auth.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        try {
            http
                    .csrf().disable()
                    .authorizeHttpRequests()
                    // Allow public access ONLY to these auth endpoints
                    .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/login/request",
                            "/api/auth/login/verify",
                            "/api/auth/resend-otp"
                    ).permitAll()
                    // Secure /api/auth/me and any other endpoints
                    .requestMatchers("/api/auth/me").authenticated()
                    .anyRequest().authenticated()
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

            // Add JWT filter to validate tokens on protected endpoints
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            log.info("Security filter chain configured successfully");

            return http.build();

        } catch (Exception e) {
            log.error("Error configuring security filter chain: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        try {
            AuthenticationManager manager = config.getAuthenticationManager();
            log.info("AuthenticationManager bean created successfully");
            return manager;
        } catch (Exception e) {
            log.error("Error creating AuthenticationManager bean: {}", e.getMessage(), e);
            throw e;
        }
    }
}
