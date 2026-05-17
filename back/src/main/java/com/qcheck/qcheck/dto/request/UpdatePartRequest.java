package com.qcheck.qcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 부품 수정 요청 DTO
 *
 * 요청 본문: PUT /api/master/parts/{partSeq}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePartRequest {

    @NotBlank(message = "부품명은 필수입니다")
    private String partNm;

    private String useYn;
}
