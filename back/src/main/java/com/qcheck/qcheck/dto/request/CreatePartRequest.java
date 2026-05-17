package com.qcheck.qcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 부품 생성 요청 DTO
 *
 * 요청 본문: POST /api/master/parts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePartRequest {

    @NotBlank(message = "부품명은 필수입니다")
    private String partNm;
}
