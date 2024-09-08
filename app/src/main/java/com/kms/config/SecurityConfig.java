package com.kms.config;

import com.kms.utils.jwt.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Value("${cors.allowed.origins}")
  private List<String> allowedOrigins;

  @Value("${cors.allowed.methods}")
  private List<String> allowedMethods;

  @Value("${cors.allowed.headers}")
  private List<String> allowedHeaders;

  @Value("${cors.allow.credentials}")
  private boolean allowCredentials;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(
            cors ->
                cors.configurationSource(
                    corsConfigurationSource())) // Enable CORS with custom configuration
        .csrf(csrf -> csrf.disable()) // Disable CSRF protection
        .authorizeHttpRequests(
            request ->
                request
                    .requestMatchers("/auth/**")
                    .permitAll() // Allow unauthenticated access to /auth endpoints
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()) // Require authentication for any other request
        // .permitAll())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  private UrlBasedCorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration corsConfiguration = new CorsConfiguration();
    corsConfiguration.setAllowedOrigins(allowedOrigins);
    corsConfiguration.setAllowedMethods(allowedMethods);
    corsConfiguration.setAllowedHeaders(allowedHeaders);
    corsConfiguration.setAllowCredentials(allowCredentials);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", corsConfiguration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
