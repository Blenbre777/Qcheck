package com.qcheck.qcheck.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {
    private String message;
    private Long companySeq;
    private Long empSeq;
}
