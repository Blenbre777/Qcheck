package com.qcheck.qcheck.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 불량 코드 생성 요청 DTO
 *
 * 요청 본문: POST /api/master/defect-codes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDefectCodeRequest {

    @NotBlank(message = "불량 코드명은 필수입니다")
    private String codeNm;

    private String description;
}
