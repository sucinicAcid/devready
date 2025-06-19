package project.DevView.cat_service.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import project.DevView.cat_service.user.dto.CustomUserDetails;
import project.DevView.cat_service.user.dto.LoginRequest;
import project.DevView.cat_service.user.dto.LoginResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        super.setFilterProcessesUrl("/api/v1/login");
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {
        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            System.out.println("[LoginFilter] attemptAuthentication -> username=" + username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password, null);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new AuthenticationException("Failed to parse login request") {};
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication
    ) throws IOException {
        System.out.println("[LoginFilter] successfulAuthentication -> user=" + authentication.getName());

        // (1) UserDetails에서 username, role, userId 추출
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        long userId = customUserDetails.getId();
        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        Iterator<? extends GrantedAuthority> it = authorities.iterator();
        String role = "ROLE_USER";
        if (it.hasNext()) {
            role = it.next().getAuthority();
        }

        // (2) JWT 토큰 생성 (10시간)
        long expirationSeconds = 60L * 60L * 10L;
        String token = jwtUtil.createJwt(userId, username, role, expirationSeconds);

        // (3) ResponseCookie 설정
        ResponseCookie jwtCookie = ResponseCookie.from("Auth", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(expirationSeconds)
                .sameSite("Lax")
                .build();

        // (4) 응답 헤더 설정
        response.setHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // (5) JSON 응답 생성
        LoginResponse loginResponse = new LoginResponse(
            true,
            "로그인 성공",
            userId,
            username,
            role
        );

        response.getWriter().write(objectMapper.writeValueAsString(loginResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException {
        System.out.println("[LoginFilter] unsuccessfulAuthentication -> " + failed.getMessage());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        LoginResponse errorResponse = new LoginResponse(
            false,
            "로그인 실패: " + failed.getMessage(),
            null,
            null,
            null
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
