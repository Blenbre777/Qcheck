package com.qcheck.qcheck.controller;

import com.qcheck.qcheck.dto.request.LoginRequest;
import com.qcheck.qcheck.dto.request.RegisterRequest;
import com.qcheck.qcheck.dto.response.LoginResponse;
import com.qcheck.qcheck.dto.response.RegisterResponse;
import com.qcheck.qcheck.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 *
 * 엔드포인트:
 * - POST /api/auth/login — 로그인
 * - POST /api/auth/refresh — AccessToken 갱신 (RefreshToken 사용)
 * - POST /api/auth/logout — 로그아웃
 *
 * 모든 엔드포인트는 SecurityConfig에서 공개(permitAll)로 설정
 * (RefreshToken 갱신은 클라이언트가 토큰을 본문에 포함하여 전송)
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 사용자 로그인
     *
     * 요청:
     * POST /api/auth/login
     * Content-Type: application/json
     * {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     *
     * 응답 (200 OK):
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
     *
     * @param loginRequest 로그인 요청 (이메일 + 비밀번호)
     * @return LoginResponse
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register attempt for email: {}", request.getEmpEmail());
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        LoginResponse response = authService.login(loginRequest);
        log.info("Login successful for email: {}", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    /**
     * AccessToken 갱신
     *
     * 요청:
     * POST /api/auth/refresh
     * Content-Type: application/json
     * {
     *   "refreshToken": "eyJhbGc..."
     * }
     *
     * 응답 (200 OK):
     * {
     *   "accessToken": "eyJhbGc...",
     *   "tokenType": "Bearer",
     *   "expiresIn": 86400
     * }
     *
     * @param request RefreshToken을 포함하는 요청
     * @return 새로운 AccessToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        log.debug("Refresh token request received");
        String newAccessToken = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(86400)
                .build());
    }

    /**
     * 사용자 로그아웃
     *
     * 요청:
     * POST /api/auth/logout
     * Authorization: Bearer <accessToken>
     *
     * 응답 (200 OK):
     * {
     *   "message": "Logout successful"
     * }
     *
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            log.info("User logged out: {}", authentication.getName());
            authService.logout(authentication.getName());
            SecurityContextHolder.clearContext();
        }
        return ResponseEntity.ok(LogoutResponse.builder()
                .message("Logout successful")
                .build());
    }

    /**
     * RefreshToken 요청 DTO
     */
    public static class RefreshTokenRequest {
        public String refreshToken;

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    /**
     * RefreshToken 응답 DTO
     */
    public static class RefreshTokenResponse {
        private String accessToken;
        private String tokenType;
        private long expiresIn;

        public RefreshTokenResponse(String accessToken, String tokenType, long expiresIn) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
        }

        public RefreshTokenResponse() {}

        public static RefreshTokenResponseBuilder builder() {
            return new RefreshTokenResponseBuilder();
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public static class RefreshTokenResponseBuilder {
            private String accessToken;
            private String tokenType;
            private long expiresIn;

            public RefreshTokenResponseBuilder accessToken(String accessToken) {
                this.accessToken = accessToken;
                return this;
            }

            public RefreshTokenResponseBuilder tokenType(String tokenType) {
                this.tokenType = tokenType;
                return this;
            }

            public RefreshTokenResponseBuilder expiresIn(long expiresIn) {
                this.expiresIn = expiresIn;
                return this;
            }

            public RefreshTokenResponse build() {
                return new RefreshTokenResponse(accessToken, tokenType, expiresIn);
            }
        }
    }

    /**
     * 로그아웃 응답 DTO
     */
    public static class LogoutResponse {
        private String message;

        public LogoutResponse(String message) {
            this.message = message;
        }

        public LogoutResponse() {}

        public static LogoutResponseBuilder builder() {
            return new LogoutResponseBuilder();
        }

        public String getMessage() {
            return message;
        }

        public static class LogoutResponseBuilder {
            private String message;

            public LogoutResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public LogoutResponse build() {
                return new LogoutResponse(message);
            }
        }
    }
}
