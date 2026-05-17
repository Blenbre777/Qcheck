package com.qcheck.qcheck.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 *
 * 요청 본문:
 * {
 *   "email": "user@example.com",
 *   "password": "password123"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    /**
     * 로그인 이메일
     *
     * - 필수 입력
     * - 유효한 이메일 형식
     * - 데이터베이스의 emp.emp_email과 매칭
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /**
     * 로그인 비밀번호
     *
     * - 필수 입력
     * - BCrypt로 암호화되어 저장됨
     * - 로그인 시 평문으로 전송되어 서버에서 BCrypt로 검증
     */
    @NotBlank(message = "Password is required")
    private String password;
}
