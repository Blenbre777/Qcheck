package com.qcheck.qcheck.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 에러 응답 DTO
 *
 * 모든 API 에러 응답의 표준 형식
 *
 * 예시:
 * {
 *   "timestamp": "2024-01-01T12:00:00",
 *   "status": 400,
 *   "error": "BAD_REQUEST",
 *   "message": "Invalid input",
 *   "details": {
 *     "email": "Email should be valid",
 *     "password": "Password is required"
 *   },
 *   "path": "/api/auth/login"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * 에러 발생 시간
     */
    private LocalDateTime timestamp;

    /**
     * HTTP 상태 코드
     */
    private int status;

    /**
     * 에러 타입
     * 예: "BAD_REQUEST", "UNAUTHORIZED", "NOT_FOUND", "INTERNAL_SERVER_ERROR"
     */
    private String error;

    /**
     * 에러 메시지
     */
    private String message;

    /**
     * 상세 에러 정보 (선택사항)
     * 유효성 검증 에러 시 필드별 에러 메시지 포함
     */
    private Map<String, String> details;

    /**
     * 요청 경로
     */
    private String path;
}
