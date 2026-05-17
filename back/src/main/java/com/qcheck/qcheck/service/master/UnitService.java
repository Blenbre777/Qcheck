package com.qcheck.qcheck.service.master;

import com.qcheck.qcheck.dto.request.CreateUnitRequest;
import com.qcheck.qcheck.dto.request.UpdateUnitRequest;
import com.qcheck.qcheck.dto.response.UnitResponse;
import com.qcheck.qcheck.entity.Unit;
import com.qcheck.qcheck.repository.UnitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 공정 단위 서비스
 *
 * 책임:
 * - 공정 단위 CRUD 비즈니스 로직
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
public class UnitService {

    @Autowired
    private UnitRepository unitRepository;

    /**
     * 공정 단위 목록 조회
     *
     * @param companySeq 회사 ID
     * @return 공정 단위 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<UnitResponse> getUnitsByCompany(Long companySeq) {
        return unitRepository.findByCompanySeq(companySeq)
            .stream()
            .map(this::toUnitResponse)
            .collect(Collectors.toList());
    }

    /**
     * 활성화된 공정 단위만 조회 (드롭다운)
     *
     * @param companySeq 회사 ID
     * @return 활성화 공정 단위 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<UnitResponse> getActiveUnitsByCompany(Long companySeq) {
        return unitRepository.findByCompanySeqAndUseYn(companySeq, "Y")
            .stream()
            .map(this::toUnitResponse)
            .collect(Collectors.toList());
    }

    /**
     * 공정 단위 상세 조회
     *
     * @param unitSeq 공정 단위 ID
     * @param companySeq 회사 ID
     * @return 공정 단위 응답 DTO
     * @throws RuntimeException 공정 단위를 찾을 수 없으면 예외 발생
     */
    @Transactional(readOnly = true)
    public UnitResponse getUnit(Long unitSeq, Long companySeq) {
        Unit unit = unitRepository.findByUnitSeqAndCompanySeq(unitSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("공정 단위를 찾을 수 없습니다"));
        return toUnitResponse(unit);
    }

    /**
     * 공정 단위 생성
     *
     * @param request 공정 단위 생성 요청 DTO
     * @param companySeq 회사 ID
     * @param empEmail 생성자 이메일
     * @return 생성된 공정 단위 응답 DTO
     * @throws RuntimeException 공정 단위명이 이미 존재하면 예외 발생
     */
    public UnitResponse createUnit(CreateUnitRequest request, Long companySeq, String empEmail) {
        // 중복 검사
        if (unitRepository.findByCompanySeqAndUnitNm(companySeq, request.getUnitNm()).isPresent()) {
            throw new RuntimeException("이미 존재하는 공정 단위입니다");
        }

        Unit unit = Unit.builder()
            .companySeq(companySeq)
            .unitNm(request.getUnitNm())
            .useYn("Y")
            .createdEp(empEmail)
            .build();

        unit = unitRepository.save(unit);
        log.info("Unit created: unitSeq={}, companySeq={}", unit.getUnitSeq(), companySeq);
        return toUnitResponse(unit);
    }

    /**
     * 공정 단위 수정
     *
     * @param unitSeq 공정 단위 ID
     * @param request 공정 단위 수정 요청 DTO
     * @param companySeq 회사 ID
     * @param empEmail 수정자 이메일
     * @return 수정된 공정 단위 응답 DTO
     * @throws RuntimeException 공정 단위를 찾을 수 없으면 예외 발생
     */
    public UnitResponse updateUnit(Long unitSeq, UpdateUnitRequest request, Long companySeq, String empEmail) {
        Unit unit = unitRepository.findByUnitSeqAndCompanySeq(unitSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("공정 단위를 찾을 수 없습니다"));

        // 중복 검사 (자신 제외)
        unitRepository.findByCompanySeqAndUnitNm(companySeq, request.getUnitNm())
            .ifPresent(existing -> {
                if (!existing.getUnitSeq().equals(unitSeq)) {
                    throw new RuntimeException("이미 존재하는 공정 단위입니다");
                }
            });

        unit.setUnitNm(request.getUnitNm());
        if (request.getUseYn() != null) {
            unit.setUseYn(request.getUseYn());
        }
        unit.setUpdatedEp(empEmail);

        unit = unitRepository.save(unit);
        log.info("Unit updated: unitSeq={}, companySeq={}", unitSeq, companySeq);
        return toUnitResponse(unit);
    }

    /**
     * 공정 단위 삭제 (Soft Delete)
     *
     * @param unitSeq 공정 단위 ID
     * @param companySeq 회사 ID
     * @param empEmail 삭제자 이메일
     * @throws RuntimeException 공정 단위를 찾을 수 없으면 예외 발생
     */
    public void deleteUnit(Long unitSeq, Long companySeq, String empEmail) {
        Unit unit = unitRepository.findByUnitSeqAndCompanySeq(unitSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("공정 단위를 찾을 수 없습니다"));

        unit.setUseYn("N");
        unit.setUpdatedEp(empEmail);
        unitRepository.save(unit);
        log.info("Unit deleted (soft): unitSeq={}, companySeq={}", unitSeq, companySeq);
    }

    /**
     * Unit 엔티티를 UnitResponse DTO로 변환
     *
     * @param unit Unit 엔티티
     * @return UnitResponse DTO
     */
    private UnitResponse toUnitResponse(Unit unit) {
        return UnitResponse.builder()
            .unitSeq(unit.getUnitSeq())
            .unitNm(unit.getUnitNm())
            .useYn(unit.getUseYn())
            .createdAt(unit.getCreatedAt())
            .createdEp(unit.getCreatedEp())
            .updatedAt(unit.getUpdatedAt())
            .updatedEp(unit.getUpdatedEp())
            .build();
    }
}
