package com.trove.ticket_trove.config.security;


import com.trove.ticket_trove.config.jwt.JwtAuthenticationFilter;
import com.trove.ticket_trove.config.jwt.JwtAuthenticationFilterException;
import com.trove.ticket_trove.model.user.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationFilterException jwtAuthenticationFilterException;

    public WebSecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationFilterException jwtAuthenticationFilterException) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationFilterException = jwtAuthenticationFilterException;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(
                List.of("http://127.0.0.1:3000","http://localhost:3000"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setAllowedMethods(List.of("GET","POST","PATCH","DELETE"));
        corsConfiguration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/*/**", corsConfiguration);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests ->
                    requests.requestMatchers(HttpMethod.POST,
                            "/**",
                            "api/v1/authentication/login",
                            "api/v1/authentication/signup",
                            "api/v1/authentication/admin-signup")
                            .permitAll()
                            .requestMatchers(HttpMethod.GET,
                                    "api/v1/concert",
                                    "api/v1/concert/**")
                            .permitAll()
                            .requestMatchers(
                                    "api/v1/concert",
                                    "api/v1/concert/**",
                                    "api/v1/reservation/concert-tickets/**")
                            .hasAnyAuthority(Role.ADMIN.name())
                            .anyRequest()
                            .authenticated())
                .cors(Customizer.withDefaults())
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilterException,
                        JwtAuthenticationFilter.class)
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(HttpBasicConfigurer::disable);

        return http.build();

    }
}
