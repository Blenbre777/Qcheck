package com.qcheck.qcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공정 단위 생성 요청 DTO
 *
 * 요청 본문: POST /api/master/units
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUnitRequest {

    @NotBlank(message = "공정 단위명은 필수입니다")
    private String unitNm;
}
