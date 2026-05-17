package com.qcheck.qcheck.service.defect;

import com.qcheck.qcheck.dto.request.DefectCreateRequest;
import com.qcheck.qcheck.dto.request.DefectUpdateRequest;
import com.qcheck.qcheck.dto.response.DefectResponse;
import com.qcheck.qcheck.dto.response.PagedDefectResponse;
import com.qcheck.qcheck.entity.Defect;
import com.qcheck.qcheck.entity.DefectCode;
import com.qcheck.qcheck.entity.Line;
import com.qcheck.qcheck.entity.Part;
import com.qcheck.qcheck.entity.Unit;
import com.qcheck.qcheck.repository.DefectCodeRepository;
import com.qcheck.qcheck.repository.DefectRepository;
import com.qcheck.qcheck.repository.LineRepository;
import com.qcheck.qcheck.repository.PartRepository;
import com.qcheck.qcheck.repository.UnitRepository;
import com.qcheck.qcheck.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 불량 관리 서비스
 *
 * 책임:
 * - 불량 등록, 수정, 삭제 비즈니스 로직
 * - 회사별 격리 처리 (companySeq 필터링)
 * - 파일 업로드 처리
 * - 마스터 데이터 유효성 검증
 *
 * 특징:
 * - Multi-Tenant: 모든 메서드에서 companySeq 격리 강제
 * - 파일 관리: FileStorageService를 통한 이미지 업로드
 * - 페이지네이션: 불량 목록 조회 시 페이징 지원
 * - 키워드 검색: 위치(position), 비고(remarks) 검색
 */
@Slf4j
@Service
@Transactional
public class DefectService {

    @Autowired
    private DefectRepository defectRepository;

    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private DefectCodeRepository defectCodeRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 불량 목록 조회
     *
     * 검색 조건:
     * - companySeq: 회사별 격리
     * - 날짜 범위: from ~ to (필수)
     * - keyword: 위치 또는 비고에서 부분 일치 (선택)
     *
     * @param companySeq 회사 ID
     * @param from 시작 날짜
     * @param to 종료 날짜
     * @param keyword 검색 키워드 (선택)
     * @param pageable 페이지 정보
     * @return 페이지네이션된 불량 목록
     */
    @Transactional(readOnly = true)
    public PagedDefectResponse getDefects(Long companySeq, LocalDate from, LocalDate to, String keyword, Pageable pageable) {
        Page<Defect> page;

        if (StringUtils.hasText(keyword)) {
            page = defectRepository.findByCompanySeqAndOccurDateBetweenAndKeyword(
                companySeq, from, to, keyword, pageable
            );
        } else {
            page = defectRepository.findByCompanySeqAndOccurDateBetween(
                companySeq, from, to, pageable
            );
        }

        List<DefectResponse> content = page.getContent()
            .stream()
            .map(this::toDefectResponse)
            .collect(Collectors.toList());

        return PagedDefectResponse.of(page, content);
    }

    /**
     * 불량 상세 조회
     *
     * @param defectSeq 불량 ID
     * @param companySeq 회사 ID
     * @return 불량 응답 DTO
     * @throws RuntimeException 불량을 찾을 수 없으면 예외 발생
     */
    @Transactional(readOnly = true)
    public DefectResponse getDefect(Long defectSeq, Long companySeq) {
        Defect defect = defectRepository.findByDefectSeqAndCompanySeq(defectSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("불량을 찾을 수 없습니다"));
        return toDefectResponse(defect);
    }

    /**
     * 불량 이미지 경로 조회
     *
     * @param defectSeq 불량 ID
     * @param companySeq 회사 ID
     * @return 이미지 경로 (예: /files/defects/1/2026-05/abc123.jpg)
     * @throws RuntimeException 불량을 찾을 수 없으면 예외 발생
     */
    @Transactional(readOnly = true)
    public String getDefectImagePath(Long defectSeq, Long companySeq) {
        Defect defect = defectRepository.findByDefectSeqAndCompanySeq(defectSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("불량을 찾을 수 없습니다"));
        return defect.getImagePath();
    }

    /**
     * 불량 등록
     *
     * 프로세스:
     * 1. 마스터 데이터(라인, 부품, 공정, 불량코드) 유효성 검증
     * 2. 이미지 파일 업로드 (선택)
     * 3. 불량 엔티티 생성 및 저장
     *
     * @param request 불량 등록 요청 DTO
     * @param file 이미지 파일 (선택)
     * @param companySeq 회사 ID
     * @param empEmail 생성자 이메일
     * @return 생성된 불량 응답 DTO
     * @throws RuntimeException 마스터 데이터 검증 실패 시
     */
    public DefectResponse createDefect(DefectCreateRequest request, MultipartFile file, Long companySeq, String empEmail) {
        // 마스터 데이터 유효성 검증
        validateMasterData(request, companySeq);

        // 파일 업로드 (선택)
        String imagePath = null;
        if (file != null && !file.isEmpty()) {
            imagePath = fileStorageService.storeFile(companySeq, file);
        }

        // 불량 엔티티 생성
        Defect defect = Defect.builder()
            .companySeq(companySeq)
            .lineSeq(request.getLineSeq())
            .partSeq(request.getPartSeq())
            .unitSeq(request.getUnitSeq())
            .defectCodeSeq(request.getDefectCodeSeq())
            .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
            .position(request.getPosition())
            .remarks(request.getRemarks())
            .occurDate(request.getOccurDate())
            .imagePath(imagePath)
            .createdEp(empEmail)
            .build();

        defect = defectRepository.save(defect);
        log.info("Defect created: defectSeq={}, companySeq={}, hasImage={}",
            defect.getDefectSeq(), companySeq, imagePath != null);
        return toDefectResponse(defect);
    }

    /**
     * 불량 수정
     *
     * 트랜잭션 안전 패턴:
     * 1. 새 파일 저장 (실패 시 기존 파일 유지)
     * 2. DB 업데이트 및 트랜잭션 커밋
     * 3. 기존 파일 삭제 (커밋 후 안전하게 실행)
     *
     * 프로세스:
     * 1. 기존 불량 조회 및 회사 격리 확인
     * 2. 마스터 데이터 유효성 검증
     * 3. 이미지 파일 업로드 (선택) - 기존 이미지 대체
     * 4. 불량 정보 업데이트
     *
     * @param defectSeq 불량 ID
     * @param request 불량 수정 요청 DTO
     * @param file 이미지 파일 (선택)
     * @param companySeq 회사 ID
     * @param empEmail 수정자 이메일
     * @return 수정된 불량 응답 DTO
     * @throws RuntimeException 불량을 찾을 수 없거나 마스터 데이터 검증 실패 시
     */
    public DefectResponse updateDefect(Long defectSeq, DefectUpdateRequest request, MultipartFile file, Long companySeq, String empEmail) {
        // 기존 불량 조회
        Defect defect = defectRepository.findByDefectSeqAndCompanySeq(defectSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("불량을 찾을 수 없습니다"));

        // 마스터 데이터 유효성 검증
        validateMasterData(request, companySeq);

        // 이미지 파일 업로드 (선택)
        String oldImagePath = null;
        if (file != null && !file.isEmpty()) {
            // 1. 새 이미지 저장 (실패 시 기존 파일 유지)
            String newImagePath = fileStorageService.storeFile(companySeq, file);

            // 2. 기존 이미지 경로 저장 (나중에 삭제)
            oldImagePath = defect.getImagePath();

            // 3. 새 이미지 경로로 업데이트
            defect.setImagePath(newImagePath);
        }

        // 불량 정보 업데이트
        defect.setLineSeq(request.getLineSeq());
        defect.setPartSeq(request.getPartSeq());
        defect.setUnitSeq(request.getUnitSeq());
        defect.setDefectCodeSeq(request.getDefectCodeSeq());
        defect.setQuantity(request.getQuantity());
        defect.setPosition(request.getPosition());
        defect.setRemarks(request.getRemarks());
        defect.setOccurDate(request.getOccurDate());
        defect.setUpdatedEp(empEmail);

        defect = defectRepository.save(defect);
        log.info("Defect updated: defectSeq={}, companySeq={}", defectSeq, companySeq);

        // 4. 기존 이미지 삭제 (DB 커밋 후 안전하게 실행)
        if (oldImagePath != null) {
            try {
                fileStorageService.deleteFile(oldImagePath);
                log.info("Old image deleted: {}", oldImagePath);
            } catch (Exception e) {
                log.warn("Failed to delete old image: {}", oldImagePath, e);
                // 계속 진행 (파일 삭제 실패는 데이터 무결성 문제 아님)
            }
        }

        return toDefectResponse(defect);
    }

    /**
     * 불량 삭제
     *
     * 트랜잭션 안전 패턴:
     * 1. DB에서 레코드 삭제
     * 2. 트랜잭션 커밋
     * 3. 파일 삭제 (커밋 후 안전하게 실행)
     *
     * 프로세스:
     * 1. 기존 불량 조회 및 회사 격리 확인
     * 2. 불량 레코드 삭제
     * 3. 이미지 파일 삭제 (있으면)
     *
     * @param defectSeq 불량 ID
     * @param companySeq 회사 ID
     * @throws RuntimeException 불량을 찾을 수 없으면 예외 발생
     */
    public void deleteDefect(Long defectSeq, Long companySeq) {
        Defect defect = defectRepository.findByDefectSeqAndCompanySeq(defectSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("불량을 찾을 수 없습니다"));

        // 이미지 경로 저장 (삭제 전에 백업)
        String imagePath = defect.getImagePath();

        // 불량 레코드 삭제
        defectRepository.delete(defect);
        log.info("Defect deleted: defectSeq={}, companySeq={}", defectSeq, companySeq);

        // 이미지 파일 삭제 (DB 커밋 후 안전하게 실행)
        if (imagePath != null) {
            try {
                fileStorageService.deleteFile(imagePath);
                log.info("Image deleted: {}", imagePath);
            } catch (Exception e) {
                log.warn("Failed to delete image: {}", imagePath, e);
                // 계속 진행 (파일 삭제 실패는 데이터 무결성 문제 아님)
            }
        }
    }

    /**
     * 마스터 데이터 유효성 검증
     *
     * 검증 항목:
     * - lineSeq: Line 존재 여부 및 회사 격리
     * - partSeq: Part 존재 여부 및 회사 격리
     * - unitSeq: Unit 존재 여부 및 회사 격리
     * - defectCodeSeq: DefectCode 존재 여부 및 회사 격리
     *
     * @param request 요청 DTO (DefectCreateRequest 또는 DefectUpdateRequest)
     * @param companySeq 회사 ID
     * @throws RuntimeException 마스터 데이터를 찾을 수 없으면 예외 발생
     */
    private void validateMasterData(Object request, Long companySeq) {
        Long lineSeq = null;
        Long partSeq = null;
        Long unitSeq = null;
        Long defectCodeSeq = null;

        if (request instanceof DefectCreateRequest) {
            DefectCreateRequest req = (DefectCreateRequest) request;
            lineSeq = req.getLineSeq();
            partSeq = req.getPartSeq();
            unitSeq = req.getUnitSeq();
            defectCodeSeq = req.getDefectCodeSeq();
        } else if (request instanceof DefectUpdateRequest) {
            DefectUpdateRequest req = (DefectUpdateRequest) request;
            lineSeq = req.getLineSeq();
            partSeq = req.getPartSeq();
            unitSeq = req.getUnitSeq();
            defectCodeSeq = req.getDefectCodeSeq();
        }

        // 라인 검증
        Line line = lineRepository.findByLineSeqAndCompanySeq(lineSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("생산 라인을 찾을 수 없습니다"));

        // 부품 검증
        Part part = partRepository.findByPartSeqAndCompanySeq(partSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("부품을 찾을 수 없습니다"));

        // 공정 단위 검증
        Unit unit = unitRepository.findByUnitSeqAndCompanySeq(unitSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("공정 단위를 찾을 수 없습니다"));

        // 불량 코드 검증
        DefectCode defectCode = defectCodeRepository.findByDefectCodeSeqAndCompanySeq(defectCodeSeq, companySeq)
            .orElseThrow(() -> new RuntimeException("불량 코드를 찾을 수 없습니다"));

        log.debug("Master data validation passed: lineSeq={}, partSeq={}, unitSeq={}, defectCodeSeq={}",
            lineSeq, partSeq, unitSeq, defectCodeSeq);
    }

    /**
     * Defect 엔티티를 DefectResponse DTO로 변환
     *
     * 마스터 데이터 로드 시 회사 격리 적용 (findByXxxSeqAndCompanySeq)
     *
     * @param defect Defect 엔티티
     * @return DefectResponse DTO
     */
    private DefectResponse toDefectResponse(Defect defect) {
        Long companySeq = defect.getCompanySeq();

        // 마스터 데이터 로드 (필드명 표시용)
        // 회사 격리를 위해 findByXxxSeqAndCompanySeq 사용
        Line line = lineRepository.findByLineSeqAndCompanySeq(defect.getLineSeq(), companySeq).orElse(null);
        Part part = partRepository.findByPartSeqAndCompanySeq(defect.getPartSeq(), companySeq).orElse(null);
        Unit unit = unitRepository.findByUnitSeqAndCompanySeq(defect.getUnitSeq(), companySeq).orElse(null);
        DefectCode defectCode = defectCodeRepository.findByDefectCodeSeqAndCompanySeq(defect.getDefectCodeSeq(), companySeq).orElse(null);

        return DefectResponse.builder()
            .defectSeq(defect.getDefectSeq())
            .occurDate(defect.getOccurDate())
            .unitSeq(defect.getUnitSeq())
            .unitNm(unit != null ? unit.getUnitNm() : "")
            .lineSeq(defect.getLineSeq())
            .lineNm(line != null ? line.getLineNm() : "")
            .partSeq(defect.getPartSeq())
            .partNm(part != null ? part.getPartNm() : "")
            .defectCodeSeq(defect.getDefectCodeSeq())
            .codeNm(defectCode != null ? defectCode.getCodeNm() : "")
            .position(defect.getPosition())
            .quantity(defect.getQuantity())
            .remarks(defect.getRemarks())
            .hasImage(defect.getImagePath() != null)
            .createdAt(defect.getCreatedAt())
            .createdEp(defect.getCreatedEp())
            .updatedAt(defect.getUpdatedAt())
            .updatedEp(defect.getUpdatedEp())
            .build();
    }
}
