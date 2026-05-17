package com.qcheck.qcheck.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 부품 응답 DTO
 *
 * 용도: GET /api/master/parts, POST/PUT /api/master/parts/{partSeq}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartResponse {

    private Long partSeq;

    private String partNm;

    private String useYn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private String createdEp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String updatedEp;
}
