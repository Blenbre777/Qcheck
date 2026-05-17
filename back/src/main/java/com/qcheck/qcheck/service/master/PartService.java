package com.qcheck.qcheck.service.master;

import com.qcheck.qcheck.dto.request.CreatePartRequest;
import com.qcheck.qcheck.dto.request.UpdatePartRequest;
import com.qcheck.qcheck.dto.response.PartResponse;
import com.qcheck.qcheck.entity.Part;
import com.qcheck.qcheck.repository.PartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 부품 서비스
 *
 * 책임:
 * - 부품 CRUD 비즈니스 로직
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
public class PartService {

    @Autowired
    private PartRepository partRepository;

    /**
     * 부품 목록 조회
     *
     * @param companySeq 회사 ID
     * @return 부품 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<PartResponse> getPartsByCompany(Long companySeq) {
        return partRepository.findByCompanySeq(companySeq)
            .stream()
            .map(this::toPartResponse)
            .collect(Collectors.toList());
    }

    /**
     * 활성화된 부품만 조회 (드롭다운)
     *
     * @param companySeq 회사 ID
     * @return 활성화 부품 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<PartResponse> getActivePartsByCompany(Long companySeq) {
        return partRepository.findByCompanySeqAndUseYn(companySeq, "Y")
            .stream()
            .map(this::toPartResponse)
            .collect(Collectors.toList());
    }

    /**
     * 부품 상세 조회
     *
     * @param partSeq 부품 ID
     * @param companySeq 회사 ID
     * @return 부품 응답 DTO
     * @throws RuntimeException 부품을 찾을 수 없으면 예외 발생
     */
    @Transactional(readOnly = true)
    public PartResponse getPart(Long partSeq, Long companySeq) {
        Part part = partRepository.findByPartSeqAndCompanySeq(partSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("부품을 찾을 수 없습니다"));
        return toPartResponse(part);
    }

    /**
     * 부품 생성
     *
     * @param request 부품 생성 요청 DTO
     * @param companySeq 회사 ID
     * @param empEmail 생성자 이메일
     * @return 생성된 부품 응답 DTO
     * @throws RuntimeException 부품명이 이미 존재하면 예외 발생
     */
    public PartResponse createPart(CreatePartRequest request, Long companySeq, String empEmail) {
        // 중복 검사
        if (partRepository.findByCompanySeqAndPartNm(companySeq, request.getPartNm()).isPresent()) {
            throw new RuntimeException("이미 존재하는 부품입니다");
        }

        Part part = Part.builder()
            .companySeq(companySeq)
            .partNm(request.getPartNm())
            .useYn("Y")
            .createdEp(empEmail)
            .build();

        part = partRepository.save(part);
        log.info("Part created: partSeq={}, companySeq={}", part.getPartSeq(), companySeq);
        return toPartResponse(part);
    }

    /**
     * 부품 수정
     *
     * @param partSeq 부품 ID
     * @param request 부품 수정 요청 DTO
     * @param companySeq 회사 ID
     * @param empEmail 수정자 이메일
     * @return 수정된 부품 응답 DTO
     * @throws RuntimeException 부품을 찾을 수 없으면 예외 발생
     */
    public PartResponse updatePart(Long partSeq, UpdatePartRequest request, Long companySeq, String empEmail) {
        Part part = partRepository.findByPartSeqAndCompanySeq(partSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("부품을 찾을 수 없습니다"));

        // 중복 검사 (자신 제외)
        partRepository.findByCompanySeqAndPartNm(companySeq, request.getPartNm())
            .ifPresent(existing -> {
                if (!existing.getPartSeq().equals(partSeq)) {
                    throw new RuntimeException("이미 존재하는 부품입니다");
                }
            });

        part.setPartNm(request.getPartNm());
        if (request.getUseYn() != null) {
            part.setUseYn(request.getUseYn());
        }
        part.setUpdatedEp(empEmail);

        part = partRepository.save(part);
        log.info("Part updated: partSeq={}, companySeq={}", partSeq, companySeq);
        return toPartResponse(part);
    }

    /**
     * 부품 삭제 (Soft Delete)
     *
     * @param partSeq 부품 ID
     * @param companySeq 회사 ID
     * @param empEmail 삭제자 이메일
     * @throws RuntimeException 부품을 찾을 수 없으면 예외 발생
     */
    public void deletePart(Long partSeq, Long companySeq, String empEmail) {
        Part part = partRepository.findByPartSeqAndCompanySeq(partSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("부품을 찾을 수 없습니다"));

        part.setUseYn("N");
        part.setUpdatedEp(empEmail);
        partRepository.save(part);
        log.info("Part deleted (soft): partSeq={}, companySeq={}", partSeq, companySeq);
    }

    /**
     * Part 엔티티를 PartResponse DTO로 변환
     *
     * @param part Part 엔티티
     * @return PartResponse DTO
     */
    private PartResponse toPartResponse(Part part) {
        return PartResponse.builder()
            .partSeq(part.getPartSeq())
            .partNm(part.getPartNm())
            .useYn(part.getUseYn())
            .createdAt(part.getCreatedAt())
            .createdEp(part.getCreatedEp())
            .updatedAt(part.getUpdatedAt())
            .updatedEp(part.getUpdatedEp())
            .build();
    }
}
