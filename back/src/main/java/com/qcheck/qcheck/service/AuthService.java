package com.qcheck.qcheck.service;

import com.qcheck.qcheck.dto.request.LoginRequest;
import com.qcheck.qcheck.dto.response.LoginResponse;
import com.qcheck.qcheck.entity.Emp;
import com.qcheck.qcheck.repository.EmpRepository;
import com.qcheck.qcheck.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 서비스
 *
 * 책임:
 * - 사용자 로그인 (이메일 + 비밀번호)
 * - JWT AccessToken 및 RefreshToken 생성
 * - 토큰 갱신 (RefreshToken으로 새로운 AccessToken 발급)
 *
 * 로그인 흐름:
 * 1. 이메일으로 사원 정보 조회
 * 2. AuthenticationManager로 비밀번호 검증
 * 3. 검증 성공 시 JWT 토큰 쌍 생성 (AccessToken + RefreshToken)
 * 4. 사원 정보와 함께 LoginResponse 반환
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private EmpRepository empRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 사용자 로그인
     *
     * @param loginRequest 이메일 + 비밀번호
     * @return LoginResponse (AccessToken, RefreshToken, 사원 정보)
     * @throws BadCredentialsException 이메일 또는 비밀번호 불일치
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // 1. 이메일로 사원 정보 조회
        Emp emp = empRepository.findByEmpEmail(email)
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found with email: {}", email);
                    return new BadCredentialsException("Invalid email or password");
                });

        // 2. 사용 중단 상태 확인
        if ("N".equals(emp.getUseYn())) {
            log.warn("Login failed: user account is disabled: {}", email);
            throw new BadCredentialsException("User account is disabled");
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(password, emp.getEmpPw())) {
            log.warn("Login failed: invalid password for user: {}", email);
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", email);

        // 4. JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                emp.getEmpSeq(),
                emp.getCompanySeq(),
                emp.getEmpRole().toString(),
                emp.getEmpEmail()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                emp.getEmpSeq(),
                emp.getCompanySeq(),
                emp.getEmpRole().toString(),
                emp.getEmpEmail()
        );

        // 5. 사원 정보 구성
        LoginResponse.EmpInfo empInfo = LoginResponse.EmpInfo.builder()
                .empSeq(emp.getEmpSeq())
                .email(emp.getEmpEmail())
                .empNm(emp.getEmpNm())
                .companySeq(emp.getCompanySeq())
                .empRole(emp.getEmpRole().toString())
                .build();

        // 6. LoginResponse 구성 및 반환
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400) // 1일 (초 단위)
                .empInfo(empInfo)
                .build();
    }

    /**
     * RefreshToken으로 새로운 AccessToken 발급
     *
     * @param refreshToken 갱신할 RefreshToken
     * @return 새로운 AccessToken
     * @throws IllegalArgumentException 토큰이 유효하지 않음
     */
    @Transactional(readOnly = true)
    public String refreshAccessToken(String refreshToken) {
        // 1. RefreshToken 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Failed to refresh token: invalid token");
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // 2. 토큰에서 정보 추출
        Long empSeq = jwtTokenProvider.getEmpSeqFromToken(refreshToken);
        Long companySeq = jwtTokenProvider.getCompanySeqFromToken(refreshToken);
        String empRole = jwtTokenProvider.getRoleFromToken(refreshToken);
        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // 3. 사원이 여전히 활성인지 확인 (선택사항: 보안 강화)
        Emp emp = empRepository.findByEmpSeqAndCompanySeq(empSeq, companySeq)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if ("N".equals(emp.getUseYn())) {
            log.warn("Failed to refresh token: user account is disabled: {}", email);
            throw new IllegalArgumentException("User account is disabled");
        }

        log.info("Token refreshed successfully for user: {}", email);

        // 4. 새로운 AccessToken 생성
        return jwtTokenProvider.generateAccessToken(empSeq, companySeq, empRole, email);
    }

    /**
     * 로그아웃 (서버 사이드: 추가 처리 없음)
     *
     * JWT는 stateless이므로 서버에서 별도 처리 불필요
     * 클라이언트에서 토큰 삭제
     * (선택: 토큰 블랙리스트 구현 가능)
     */
    public void logout(String email) {
        log.info("User logged out: {}", email);
        // JWT는 stateless이므로 서버에서 추가 처리 불필요
        // 필요시 Redis 토큰 블랙리스트 구현 가능
    }
}
