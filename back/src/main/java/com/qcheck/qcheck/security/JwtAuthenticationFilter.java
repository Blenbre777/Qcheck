package com.qcheck.qcheck.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 인증 필터
 *
 * 책임:
 * - 요청 헤더에서 JWT 토큰 추출 (Authorization: Bearer <token>)
 * - 토큰 유효성 검증
 * - 유효한 토큰이면 SecurityContext에 Authentication 설정
 * - 모든 요청에 대해 한 번만 실행 (OncePerRequestFilter)
 *
 * 동작 순서:
 * 1. 요청에서 JWT 토큰 추출
 * 2. 토큰 유효성 검증 (JwtTokenProvider.validateToken)
 * 3. 토큰에서 empSeq 추출
 * 4. UserDetailsService로 UserDetails 조회
 * 5. SecurityContext에 Authentication 설정
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = extractJwtFromRequest(request);

            // 2. 토큰 유효성 검증 및 Authentication 설정
            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 3. 토큰에서 empSeq, companySeq, email 추출
                Long empSeq = jwtTokenProvider.getEmpSeqFromToken(token);
                Long companySeq = jwtTokenProvider.getCompanySeqFromToken(token);
                String email = jwtTokenProvider.getEmailFromToken(token);

                if (empSeq != null && companySeq != null && email != null) {
                    // 4. UserDetailsService로 UserDetails 조회
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // 5. Authentication 토큰 생성 및 SecurityContext에 설정
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 6. 요청 속성에 companySeq 저장 (컨트롤러에서 접근 가능)
                    request.setAttribute("companySeq", companySeq);

                    log.debug("JWT authentication successful for user: {}, companySeq: {}", email, companySeq);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage(), e);
        }

        // 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     *
     * 형식: Authorization: Bearer <token>
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (토큰이 없으면 null)
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }
}
