package com.qcheck.qcheck.service.master;

import com.qcheck.qcheck.dto.request.CreateLineRequest;
import com.qcheck.qcheck.dto.request.UpdateLineRequest;
import com.qcheck.qcheck.dto.response.LineResponse;
import com.qcheck.qcheck.entity.Line;
import com.qcheck.qcheck.repository.LineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 라인 서비스
 *
 * 책임:
 * - 라인 CRUD 비즈니스 로직
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
public class LineService {

    @Autowired
    private LineRepository lineRepository;

    /**
     * 라인 목록 조회
     *
     * @param companySeq 회사 ID
     * @return 라인 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<LineResponse> getLinesByCompany(Long companySeq) {
        return lineRepository.findByCompanySeq(companySeq)
            .stream()
            .map(this::toLineResponse)
            .collect(Collectors.toList());
    }

    /**
     * 활성화된 라인만 조회 (드롭다운)
     *
     * @param companySeq 회사 ID
     * @return 활성화 라인 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<LineResponse> getActiveLinesByCompany(Long companySeq) {
        return lineRepository.findByCompanySeqAndUseYn(companySeq, "Y")
            .stream()
            .map(this::toLineResponse)
            .collect(Collectors.toList());
    }

    /**
     * 라인 상세 조회
     *
     * @param lineSeq 라인 ID
     * @param companySeq 회사 ID
     * @return 라인 응답 DTO
     * @throws RuntimeException 라인을 찾을 수 없으면 예외 발생
     */
    @Transactional(readOnly = true)
    public LineResponse getLine(Long lineSeq, Long companySeq) {
        Line line = lineRepository.findByLineSeqAndCompanySeq(lineSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("라인을 찾을 수 없습니다"));
        return toLineResponse(line);
    }

    /**
     * 라인 생성
     *
     * @param request 라인 생성 요청 DTO
     * @param companySeq 회사 ID
     * @param empEmail 생성자 이메일
     * @return 생성된 라인 응답 DTO
     * @throws RuntimeException 라인명이 이미 존재하면 예외 발생
     */
    public LineResponse createLine(CreateLineRequest request, Long companySeq, String empEmail) {
        // 중복 검사
        if (lineRepository.findByCompanySeqAndLineNm(companySeq, request.getLineNm()).isPresent()) {
            throw new RuntimeException("이미 존재하는 라인입니다");
        }

        Line line = Line.builder()
            .companySeq(companySeq)
            .lineNm(request.getLineNm())
            .useYn("Y")
            .createdEp(empEmail)
            .build();

        line = lineRepository.save(line);
        log.info("Line created: lineSeq={}, companySeq={}", line.getLineSeq(), companySeq);
        return toLineResponse(line);
    }

    /**
     * 라인 수정
     *
     * @param lineSeq 라인 ID
     * @param request 라인 수정 요청 DTO
     * @param companySeq 회사 ID
     * @param empEmail 수정자 이메일
     * @return 수정된 라인 응답 DTO
     * @throws RuntimeException 라인을 찾을 수 없으면 예외 발생
     */
    public LineResponse updateLine(Long lineSeq, UpdateLineRequest request, Long companySeq, String empEmail) {
        Line line = lineRepository.findByLineSeqAndCompanySeq(lineSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("라인을 찾을 수 없습니다"));

        // 중복 검사 (자신 제외)
        lineRepository.findByCompanySeqAndLineNm(companySeq, request.getLineNm())
            .ifPresent(existing -> {
                if (!existing.getLineSeq().equals(lineSeq)) {
                    throw new RuntimeException("이미 존재하는 라인입니다");
                }
            });

        line.setLineNm(request.getLineNm());
        if (request.getUseYn() != null) {
            line.setUseYn(request.getUseYn());
        }
        line.setUpdatedEp(empEmail);

        line = lineRepository.save(line);
        log.info("Line updated: lineSeq={}, companySeq={}", lineSeq, companySeq);
        return toLineResponse(line);
    }

    /**
     * 라인 삭제 (Soft Delete)
     *
     * @param lineSeq 라인 ID
     * @param companySeq 회사 ID
     * @param empEmail 삭제자 이메일
     * @throws RuntimeException 라인을 찾을 수 없으면 예외 발생
     */
    public void deleteLine(Long lineSeq, Long companySeq, String empEmail) {
        Line line = lineRepository.findByLineSeqAndCompanySeq(lineSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("라인을 찾을 수 없습니다"));

        line.setUseYn("N");
        line.setUpdatedEp(empEmail);
        lineRepository.save(line);
        log.info("Line deleted (soft): lineSeq={}, companySeq={}", lineSeq, companySeq);
    }

    /**
     * Line 엔티티를 LineResponse DTO로 변환
     *
     * @param line Line 엔티티
     * @return LineResponse DTO
     */
    private LineResponse toLineResponse(Line line) {
        return LineResponse.builder()
            .lineSeq(line.getLineSeq())
            .lineNm(line.getLineNm())
            .useYn(line.getUseYn())
            .createdAt(line.getCreatedAt())
            .createdEp(line.getCreatedEp())
            .updatedAt(line.getUpdatedAt())
            .updatedEp(line.getUpdatedEp())
            .build();
    }
}
