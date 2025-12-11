package com.ahana.botmonitoring.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;
        private final JwtAuthEntryPoint jwtAuthEntryPoint;

        public SecurityConfig(JwtAuthFilter jwtAuthFilter, JwtAuthEntryPoint jwtAuthEntryPoint) {
                this.jwtAuthFilter = jwtAuthFilter;
                this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))
                                .authorizeHttpRequests(auth -> auth
                                                // Public endpoints - no authentication required
                                                .requestMatchers(
                                                                "/userLogin",
                                                                "/login",
                                                                "/api/login",
                                                                "/logout",
                                                                "/api/session/validate",
                                                                "/api/session/check-admin",
                                                                "/findAllBots",
                                                                "/findAllBots/**",
                                                                "/requestAllBotUtilisation",
                                                                "/requestAllBotUtilisationNew",
                                                                "/requestAllBotDetailUtilisation",
                                                                "/requestBotUtilisationNew",
                                                                "/filterLogs",
                                                                "/filterLogsNew",
                                                                "/getAllLog",
                                                                "/getAllLog/**",
                                                                "/getAllChecksumData",
                                                                "/getAllChecksumData/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-resources/**",
                                                                "/webjars/**",
                                                                "/actuator/**",
                                                                // Bot endpoints - now public
                                                                "/addBot",
                                                                "/updateBot",
                                                                "/deleteBot/**",
                                                                "/getBotStatus/**",
                                                                "/getDetailsLogByDateRange/**",
                                                                // Process endpoints - now public
                                                                "/addProcess",
                                                                "/uploadXamlFiles/**",
                                                                "/findAllProcess",
                                                                "/updateProcess",
                                                                "/getProcessForRegisteredBots",
                                                                "/getUniqueBotNames",
                                                                "/deleteProcess/**",
                                                                "/listProcess/**",
                                                                // User management endpoints - now public
                                                                "/addApplicationUser",
                                                                "/findAllApplicationUsers",
                                                                "/updateApplicationUser")
                                                .permitAll()

                                                // All other endpoints require authentication
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // Allow all origins - you can restrict this to specific origins in production
                configuration.setAllowedOriginPatterns(Arrays.asList("*"));
                // Allow common HTTP methods
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                // Allow common headers including Authorization for JWT
                configuration.setAllowedHeaders(Arrays.asList("*"));
                // Allow credentials (cookies, authorization headers, etc.)
                configuration.setAllowCredentials(true);
                // Expose headers that the client can access
                configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
                // Cache preflight response for 1 hour
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                // Apply CORS configuration to all endpoints
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}
