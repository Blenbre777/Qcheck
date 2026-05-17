package com.qcheck.qcheck.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 *
 * 응답 본문:
 * {
 *   "accessToken": "eyJhbGc...",
 *   "refreshToken": "eyJhbGc...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 86400,
 *   "empInfo": {
 *     "empSeq": 1,
 *     "email": "user@example.com",
 *     "empNm": "John Doe",
 *     "companySeq": 1,
 *     "empRole": "USER"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    /**
     * Access Token (JWT)
     *
     * - 모든 API 요청 시 Authorization: Bearer <token> 형태로 사용
     * - 유효 기간: 1일 (application.properties jwt.expiration)
     */
    private String accessToken;

    /**
     * Refresh Token (JWT)
     *
     * - AccessToken 만료 시 새로운 AccessToken 발급에 사용
     * - 유효 기간: 7일 (application.properties jwt.refresh-expiration)
     */
    private String refreshToken;

    /**
     * 토큰 타입
     *
     * 값: "Bearer"
     */
    private String tokenType;

    /**
     * AccessToken 유효 기간 (초)
     *
     * 프론트엔드에서 토큰 만료 시간 계산에 사용
     */
    private long expiresIn;

    /**
     * 로그인한 사원 정보
     */
    private EmpInfo empInfo;

    /**
     * 사원 정보 중첩 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmpInfo {
        private Long empSeq;
        private String email;
        private String empNm;
        private Long companySeq;
        private String empRole;
    }
}
