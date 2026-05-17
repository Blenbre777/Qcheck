package com.qcheck.qcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 라인 생성 요청 DTO
 *
 * 요청 본문: POST /api/master/lines
 *
 * 필드:
 * - lineNm: 라인명 (필수, 공백 불가)
 *
 * 주의사항:
 * - companySeq는 JWT 토큰에서 추출 (클라이언트 입력 안 함)
 * - 동일 회사 내 lineNm 중복 불가 (데이터베이스 제약)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLineRequest {

    /**
     * 라인명
     * 예: L1, L2, Assembly Line 등
     */
    @NotBlank(message = "라인명은 필수입니다")
    private String lineNm;
}
