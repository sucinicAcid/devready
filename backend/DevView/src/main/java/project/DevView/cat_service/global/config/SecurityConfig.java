package project.DevView.cat_service.global.config;


import project.DevView.cat_service.global.jwt.JWTFilter;
import project.DevView.cat_service.global.jwt.JWTUtil;
import project.DevView.cat_service.global.jwt.LoginFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    //AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    //AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",        // 개발 환경
            "http://13.239.22.1:8000",      // 실제 서비스 IP (HTTP + 포트)
            "https://13.239.22.1:8000",     // 실제 서비스 IP (HTTPS + 포트)
            "http://13.239.22.1",           // 포트 없는 버전도 허용
            "https://13.239.22.1",          // 포트 없는 버전도 허용
            "http://devview.site:8080",     // devview.site 도메인 (HTTP + 8080 포트)
            "https://devview.site:8080",    // devview.site 도메인 (HTTPS + 8080 포트)
            "http://devview.site",          // devview.site 도메인 (HTTP)
            "https://devview.site"          // devview.site 도메인 (HTTPS)
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf((auth) -> auth.disable());

        http
                .formLogin((auth) -> auth.disable());

        http
                .httpBasic((auth) -> auth.disable());

        // Chrome DevTools 관련 경로는 보안 필터 체인에서 제외
        http
                .securityMatcher(request -> {
                    String path = request.getServletPath();
                    return !path.startsWith("/.well-known/");
                });

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers( "/api/v1/img/**", "/",
                                "/api/v1/join", "/api/v1/login", "/login",
                                "/interview", "/interview-mode", "/joinPage", "/questions/all", 
                                "/error", "/error/**",
                                "/css/**", "/js/**", "/favicon.ico", "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**").permitAll()
                        .requestMatchers("/login", "/joinPage").anonymous()
                        .requestMatchers("/interview-mode").permitAll()
                        .anyRequest().authenticated());

        // 세션을 사용하지 않음 (JWT stateless 방식)
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        //필터 추가 LoginFilter()는 인자를 받음 (AuthenticationManager() 메소드에 authenticationConfiguration 객체를 넣어야 함) 따라서 등록 필요
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        http
                .addFilterAfter(new JWTFilter(jwtUtil), LoginFilter.class);

        return http.build();
    }
}