package com.labMetricas.LabMetricas.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Permitir todas las peticiones OPTIONS (preflight CORS)
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Endpoints públicos
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers("/api/products/qr/**").permitAll()
                
                // User endpoints for all authenticated users (ADMIN, SUPERVISOR, OPERADOR)
                .requestMatchers("/api/users/**").hasAnyAuthority("ADMIN", "SUPERVISOR", "OPERADOR")
                
                // Customer endpoints for ADMIN, SUPERVISOR, and OPERADOR
                .requestMatchers("/api/customers/**").hasAnyAuthority("ADMIN", "SUPERVISOR", "OPERADOR")
                
                // Admin-only user management endpoints
                .requestMatchers("/api/admin/users/**").hasAuthority("ADMIN")
                
                // Endpoints específicos para SUPERVISOR
                .requestMatchers("/api/supervisor/**").hasAnyAuthority("ADMIN", "SUPERVISOR")
                
                // Endpoints específicos para OPERADOR
                .requestMatchers("/api/operador/**").hasAnyAuthority("ADMIN", "SUPERVISOR", "OPERADOR")
                
                // Maintenance endpoints for SUPERVISOR and ADMIN
                .requestMatchers("/api/maintenance/init-data").hasAnyAuthority("ADMIN", "SUPERVISOR", "OPERADOR")
                .requestMatchers("/api/maintenance/create").hasAnyAuthority("ADMIN", "SUPERVISOR", "OPERADOR")
                .requestMatchers("/api/maintenance/update-status/**").hasAnyAuthority("ADMIN", "SUPERVISOR")
                .requestMatchers("/api/maintenance/list").hasAnyAuthority("ADMIN", "SUPERVISOR")
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Obtener la URL del frontend desde la configuración
        String frontendOrigin = frontendUrl.endsWith("/") 
            ? frontendUrl.substring(0, frontendUrl.length() - 1) 
            : frontendUrl;
        
        // Lista de orígenes permitidos (incluyendo todas las URLs de Vercel)
        List<String> allowedOrigins = Arrays.asList(
            frontendOrigin,
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:8080",
            "http://localhost:8081",
            "https://sgi-front-end-git-main-antonios-projects-8bf8b09e.vercel.app",
            "https://sgi-front-end-gules.vercel.app"
        );
        
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // No se puede usar "*" cuando allowCredentials es true, especificar headers explícitamente
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS configured with allowed origins: {}", allowedOrigins);
        
        return source;
    }
} 