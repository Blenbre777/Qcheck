package com.qcheck.qcheck.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 컴포넌트
 *
 * 책임:
 * - AccessToken 생성 (expires 1일)
 * - RefreshToken 생성 (expires 7일)
 * - 토큰 검증 및 Claims 추출
 *
 * JWT Payload 구조:
 * {
 *   "sub": "empSeq",
 *   "companySeq": 1,
 *   "empRole": "USER",
 *   "email": "user@example.com",
 *   "iat": 1234567890,
 *   "exp": 1234567890 (+ 1일 또는 7일)
 * }
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration; // ms (기본값: 86400000 = 1일)

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration; // ms (기본값: 604800000 = 7일)

    /**
     * AccessToken 생성
     *
     * @param empSeq 사원 ID
     * @param companySeq 회사 ID
     * @param empRole 권한 문자열
     * @param empEmail 사원 이메일
     * @return JWT AccessToken
     */
    public String generateAccessToken(Long empSeq, Long companySeq, String empRole, String empEmail) {
        return generateToken(empSeq, companySeq, empRole, empEmail, accessTokenExpiration);
    }

    /**
     * RefreshToken 생성
     *
     * @param empSeq 사원 ID
     * @param companySeq 회사 ID
     * @param empRole 권한 문자열
     * @param empEmail 사원 이메일
     * @return JWT RefreshToken
     */
    public String generateRefreshToken(Long empSeq, Long companySeq, String empRole, String empEmail) {
        return generateToken(empSeq, companySeq, empRole, empEmail, refreshTokenExpiration);
    }

    /**
     * JWT 토큰 생성 (공통 로직)
     *
     * @param empSeq 사원 ID
     * @param companySeq 회사 ID
     * @param empRole 권한
     * @param empEmail 이메일
     * @param expirationTime 만료 시간 (ms)
     * @return JWT 토큰
     */
    private String generateToken(Long empSeq, Long companySeq, String empRole, String empEmail, long expirationTime) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(String.valueOf(empSeq))
                .claim("companySeq", companySeq)
                .claim("empRole", empRole)
                .claim("email", empEmail)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * JWT 토큰에서 empSeq 추출
     *
     * @param token JWT 토큰
     * @return 사원 ID
     */
    public Long getEmpSeqFromToken(String token) {
        try {
            return Long.valueOf(getAllClaimsFromToken(token).getSubject());
        } catch (Exception e) {
            log.error("Failed to extract empSeq from token", e);
            return null;
        }
    }

    /**
     * JWT 토큰에서 companySeq 추출
     *
     * @param token JWT 토큰
     * @return 회사 ID
     */
    public Long getCompanySeqFromToken(String token) {
        try {
            return ((Number) getAllClaimsFromToken(token).get("companySeq")).longValue();
        } catch (Exception e) {
            log.error("Failed to extract companySeq from token", e);
            return null;
        }
    }

    /**
     * JWT 토큰에서 empRole 추출
     *
     * @param token JWT 토큰
     * @return 권한 문자열
     */
    public String getRoleFromToken(String token) {
        try {
            return (String) getAllClaimsFromToken(token).get("empRole");
        } catch (Exception e) {
            log.error("Failed to extract empRole from token", e);
            return null;
        }
    }

    /**
     * JWT 토큰에서 email 추출
     *
     * @param token JWT 토큰
     * @return 이메일
     */
    public String getEmailFromToken(String token) {
        try {
            return (String) getAllClaimsFromToken(token).get("email");
        } catch (Exception e) {
            log.error("Failed to extract email from token", e);
            return null;
        }
    }

    /**
     * JWT 토큰 유효성 검증
     *
     * @param token JWT 토큰
     * @return 유효 여부
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format", e);
        } catch (ExpiredJwtException e) {
            log.error("JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is not supported", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty", e);
        } catch (SignatureException e) {
            log.error("JWT signature validation failed", e);
        }
        return false;
    }

    /**
     * JWT 토큰에서 모든 Claims 추출
     * (private: validateToken 후에만 호출)
     *
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
