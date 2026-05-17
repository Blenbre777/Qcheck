package com.qcheck.qcheck.config;

import com.qcheck.qcheck.security.JwtAuthenticationFilter;
import com.qcheck.qcheck.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 *
 * 책임:
 * - JWT 기반 stateless 인증 설정
 * - 권한 기반 접근 제어 (권한별 엔드포인트 제한)
 * - CORS 설정
 * - 비밀번호 암호화 (BCrypt)
 *
 * 설정 항목:
 * 1. 비밀번호 인코더: BCryptPasswordEncoder
 * 2. 세션 정책: STATELESS (JWT 기반이므로 서버 세션 불필요)
 * 3. 인증 필터: JwtAuthenticationFilter (JWT 검증)
 * 4. 공개 엔드포인트: /api/auth/login, /api/auth/refresh 등
 * 5. 보호 엔드포인트: /api/dashboard, /api/defects, /api/master 등
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 비밀번호 인코더 Bean
     *
     * BCrypt: 해시 기반 단방향 암호화, salting 자동 처리
     * 비용 인수(strength): 기본값 10 (높을수록 느림, 보안성 강화)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 인증 매니저 Bean
     *
     * 책임: 사용자 인증 수행
     * 사용처:
     * - AuthController.login에서 UsernamePasswordAuthenticationToken 인증
     * - DaoAuthenticationProvider를 통해 실제 인증 처리
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .authenticationProvider(daoAuthenticationProvider());
        return authenticationManagerBuilder.build();
    }

    /**
     * DAO 인증 제공자 Bean
     *
     * 책임:
     * - CustomUserDetailsService로부터 사용자 정보 조회
     * - BCryptPasswordEncoder로 비밀번호 검증
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * JWT 인증 필터 Bean
     *
     * 책임:
     * - 모든 요청에서 Authorization 헤더에서 JWT 토큰 추출
     * - 토큰 유효성 검증
     * - SecurityContext에 Authentication 설정
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * Spring Security 필터 체인 설정
     *
     * 설정 순서:
     * 1. CORS 활성화
     * 2. CSRF 비활성화 (JWT 사용이므로 불필요)
     * 3. 세션 정책: STATELESS (JWT 기반 stateless 인증)
     * 4. 공개 엔드포인트 설정 (누구나 접근 가능)
     * 5. 권한별 보호 엔드포인트 설정
     * 6. JWT 필터 추가 (UsernamePasswordAuthenticationFilter 전에)
     * 7. 인증 예외 처리
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 (WebMvcConfig에서 처리)
                .cors(cors -> cors.disable()) // 별도 WebMvcConfig에서 처리

                // CSRF 비활성화 (JWT 사용, stateless)
                .csrf(csrf -> csrf.disable())

                // 세션 정책: STATELESS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 공개 엔드포인트 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 엔드포인트: 누구나 접근 가능
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()

                        // 헬스체크: 누구나 접근 가능
                        .requestMatchers("/actuator/**").permitAll()

                        // 나머지 모든 요청: 인증 필수
                        .anyRequest().authenticated()
                )

                // JWT 인증 필터 추가
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
