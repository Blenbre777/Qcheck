package com.qcheck.qcheck.service.master;

import com.qcheck.qcheck.dto.request.CreateDefectCodeRequest;
import com.qcheck.qcheck.dto.request.UpdateDefectCodeRequest;
import com.qcheck.qcheck.dto.response.DefectCodeResponse;
import com.qcheck.qcheck.entity.DefectCode;
import com.qcheck.qcheck.repository.DefectCodeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 불량 코드 서비스
 *
 * 책임:
 * - 불량 코드 CRUD 비즈니스 로직
 * - 회사별 격리 처리 (companySeq 필터링)
 * - 중복 검사, 유효성 검증
 *
 * 특징:
 * - Soft Delete: DELETE 시 use_yn을 N으로 변경
 * - Multi-Tenant: 모든 조회는 companySeq로 필터링
 * - 드롭다운: findByCompanySeqAndUseYn으로 Y인 항목만 반환
 */
@Slf4j
@Service
@Transactional
public class DefectCodeService {

    @Autowired
    private DefectCodeRepository defectCodeRepository;

    /**
     * 불량 코드 목록 조회
     *
     * @param companySeq 회사 ID
     * @return 불량 코드 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<DefectCodeResponse> getDefectCodesByCompany(Long companySeq) {
        return defectCodeRepository.findByCompanySeq(companySeq)
            .stream()
            .map(this::toDefectCodeResponse)
            .collect(Collectors.toList());
    }

    /**
     * 활성화된 불량 코드만 조회 (드롭다운)
     *
     * @param companySeq 회사 ID
     * @return 활성화 불량 코드 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<DefectCodeResponse> getActiveDefectCodesByCompany(Long companySeq) {
        return defectCodeRepository.findByCompanySeqAndUseYn(companySeq, "Y")
            .stream()
            .map(this::toDefectCodeResponse)
            .collect(Collectors.toList());
    }

    /**
     * 불량 코드 상세 조회
     *
     * @param defectCodeSeq 불량 코드 ID
     * @param companySeq 회사 ID
     * @return 불량 코드 응답 DTO
     * @throws RuntimeException 불량 코드를 찾을 수 없으면 예외 발생
     */
    @Transactional(readOnly = true)
    public DefectCodeResponse getDefectCode(Long defectCodeSeq, Long companySeq) {
        DefectCode defectCode = defectCodeRepository.findByDefectCodeSeqAndCompanySeq(defectCodeSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("불량 코드를 찾을 수 없습니다"));
        return toDefectCodeResponse(defectCode);
    }

    /**
     * 불량 코드 생성
     *
     * @param request 불량 코드 생성 요청 DTO
     * @param companySeq 회사 ID
     * @param empEmail 생성자 이메일
     * @return 생성된 불량 코드 응답 DTO
     * @throws RuntimeException 불량 코드명이 이미 존재하면 예외 발생
     */
    public DefectCodeResponse createDefectCode(CreateDefectCodeRequest request, Long companySeq, String empEmail) {
        // 중복 검사
        if (defectCodeRepository.findByCompanySeqAndCodeNm(companySeq, request.getCodeNm()).isPresent()) {
            throw new RuntimeException("이미 존재하는 불량 코드입니다");
        }

        DefectCode defectCode = DefectCode.builder()
            .companySeq(companySeq)
            .codeNm(request.getCodeNm())
            .description(request.getDescription())
            .useYn("Y")
            .createdEp(empEmail)
            .build();

        defectCode = defectCodeRepository.save(defectCode);
        log.info("DefectCode created: defectCodeSeq={}, companySeq={}", defectCode.getDefectCodeSeq(), companySeq);
        return toDefectCodeResponse(defectCode);
    }

    /**
     * 불량 코드 수정
     *
     * @param defectCodeSeq 불량 코드 ID
     * @param request 불량 코드 수정 요청 DTO
     * @param companySeq 회사 ID
     * @param empEmail 수정자 이메일
     * @return 수정된 불량 코드 응답 DTO
     * @throws RuntimeException 불량 코드를 찾을 수 없으면 예외 발생
     */
    public DefectCodeResponse updateDefectCode(Long defectCodeSeq, UpdateDefectCodeRequest request, Long companySeq, String empEmail) {
        DefectCode defectCode = defectCodeRepository.findByDefectCodeSeqAndCompanySeq(defectCodeSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("불량 코드를 찾을 수 없습니다"));

        // 중복 검사 (자신 제외)
        defectCodeRepository.findByCompanySeqAndCodeNm(companySeq, request.getCodeNm())
            .ifPresent(existing -> {
                if (!existing.getDefectCodeSeq().equals(defectCodeSeq)) {
                    throw new RuntimeException("이미 존재하는 불량 코드입니다");
                }
            });

        defectCode.setCodeNm(request.getCodeNm());
        defectCode.setDescription(request.getDescription());
        if (request.getUseYn() != null) {
            defectCode.setUseYn(request.getUseYn());
        }
        defectCode.setUpdatedEp(empEmail);

        defectCode = defectCodeRepository.save(defectCode);
        log.info("DefectCode updated: defectCodeSeq={}, companySeq={}", defectCodeSeq, companySeq);
        return toDefectCodeResponse(defectCode);
    }

    /**
     * 불량 코드 삭제 (Soft Delete)
     *
     * @param defectCodeSeq 불량 코드 ID
     * @param companySeq 회사 ID
     * @param empEmail 삭제자 이메일
     * @throws RuntimeException 불량 코드를 찾을 수 없으면 예외 발생
     */
    public void deleteDefectCode(Long defectCodeSeq, Long companySeq, String empEmail) {
        DefectCode defectCode = defectCodeRepository.findByDefectCodeSeqAndCompanySeq(defectCodeSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("불량 코드를 찾을 수 없습니다"));

        defectCode.setUseYn("N");
        defectCode.setUpdatedEp(empEmail);
        defectCodeRepository.save(defectCode);
        log.info("DefectCode deleted (soft): defectCodeSeq={}, companySeq={}", defectCodeSeq, companySeq);
    }

    /**
     * DefectCode 엔티티를 DefectCodeResponse DTO로 변환
     *
     * @param defectCode DefectCode 엔티티
     * @return DefectCodeResponse DTO
     */
    private DefectCodeResponse toDefectCodeResponse(DefectCode defectCode) {
        return DefectCodeResponse.builder()
            .defectCodeSeq(defectCode.getDefectCodeSeq())
            .codeNm(defectCode.getCodeNm())
            .description(defectCode.getDescription())
            .useYn(defectCode.getUseYn())
            .createdAt(defectCode.getCreatedAt())
            .createdEp(defectCode.getCreatedEp())
            .updatedAt(defectCode.getUpdatedAt())
            .updatedEp(defectCode.getUpdatedEp())
            .build();
    }
}
