package com.qcheck.qcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 라인 수정 요청 DTO
 *
 * 요청 본문: PUT /api/master/lines/{lineSeq}
 *
 * 필드:
 * - lineNm: 라인명 (필수, 공백 불가)
 * - useYn: 사용 여부 (선택, Y/N)
 *
 * 주의사항:
 * - lineSeq는 URL 경로에 포함
 * - companySeq는 JWT 토큰에서 추출
 * - useYn: Y(활성) 또는 N(비활성, soft delete)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLineRequest {

    /**
     * 라인명
     * 예: L1, L2, Assembly Line 등
     */
    @NotBlank(message = "라인명은 필수입니다")
    private String lineNm;

    /**
     * 사용 여부
     * Y: 활성화, N: 비활성화 (soft delete)
     */
    private String useYn;
}
