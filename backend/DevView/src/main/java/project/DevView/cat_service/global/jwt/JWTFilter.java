package project.DevView.cat_service.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import project.DevView.cat_service.user.dto.CustomUserDetails;
import project.DevView.cat_service.user.entity.UserEntity;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    private static final RequestMatcher SKIP = new OrRequestMatcher(
            new AntPathRequestMatcher("/css/**"),
            new AntPathRequestMatcher("/js/**"),
            new AntPathRequestMatcher("/images/**"),
            new AntPathRequestMatcher("/favicon.ico"),
            new AntPathRequestMatcher("/api/v1/login"),
            new AntPathRequestMatcher("/api/v1/join")
    );

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 이 경로들은 토큰 검증 자체를 건너뛴다.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean shouldNotFilter = path.equals("/api/v1/login")
                || path.equals("/api/v1/join")
                || path.equals("/joinPage")
                || path.equals("/interview")
                || path.equals("/interview-mode")
                || path.startsWith("/api/v1/img/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/.well-known/");
        
        System.out.println("[JWTFilter] URL 확인: " + path + ", shouldNotFilter: " + shouldNotFilter);
        return shouldNotFilter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Chrome DevTools 요청은 바로 다음 필터로 넘김
        if (request.getServletPath().startsWith("/.well-known/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String fullURL = request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        System.out.println("[JWTFilter] 전체 URL: " + fullURL);
        System.out.println("[JWTFilter] ServletPath: " + request.getServletPath());
        System.out.println("[JWTFilter] ContextPath: " + request.getContextPath());

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            System.out.println("[JWTFilter] 쿠키 목록:");
            String token = null;
            for (Cookie c : cookies) {
                System.out.println("  - " + c.getName() + ": " + (c.getName().equals("Auth") ? "토큰 값 존재" : c.getValue()) + 
                                 " (Path: " + c.getPath() + ", Domain: " + c.getDomain() + ")");
                if ("Auth".equals(c.getName())) {
                    token = c.getValue();
                }
            }

            if (token != null) {
                try {
                    // JWT 토큰 유효성 검증
                    boolean isExpired = jwtUtil.isExpired(token);
                    System.out.println("[JWTFilter] 토큰 만료 여부: " + isExpired);
                    
                    if (!isExpired) {
                        // (1) 토큰 파싱
                        Long userId = jwtUtil.getId(token);
                        String username = jwtUtil.getUsername(token);
                        String role = jwtUtil.getRole(token);

                        System.out.println("[JWTFilter] 유효한 토큰 확인. userId=" + userId
                                + ", username=" + username + ", role=" + role);

                        // (2) SecurityContext 설정
                        UserEntity userEntity = new UserEntity();
                        userEntity.setId(userId);
                        userEntity.setUsername(username);
                        userEntity.setRole(role);

                        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
                        Authentication authToken = new UsernamePasswordAuthenticationToken(
                                customUserDetails,
                                null,
                                customUserDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        System.out.println("[JWTFilter] SecurityContext에 인증 정보 설정 완료");
                    }
                } catch (Exception e) {
                    System.out.println("[JWTFilter] 토큰 검증 중 예외 발생: " + e.getMessage());
                }
            } else {
                System.out.println("[JWTFilter] Auth 쿠키가 없음 -> 익명 사용자");
            }
        } else {
            System.out.println("[JWTFilter] 쿠키가 없음 -> 익명 사용자");
        }

        filterChain.doFilter(request, response);
    }
}
