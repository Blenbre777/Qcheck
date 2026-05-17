package com.qcheck.qcheck.controller;

import com.qcheck.qcheck.dto.request.DefectCreateRequest;
import com.qcheck.qcheck.dto.request.DefectUpdateRequest;
import com.qcheck.qcheck.dto.response.DefectResponse;
import com.qcheck.qcheck.dto.response.PagedDefectResponse;
import com.qcheck.qcheck.service.FileStorageService;
import com.qcheck.qcheck.service.defect.DefectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

import java.time.LocalDate;

/**
 * 불량 관리 REST API 컨트롤러
 *
 * 책임:
 * - 불량 관련 HTTP 요청 처리
 * - JWT에서 companySeq 추출 (회사 격리)
 * - 요청 파라미터 검증 및 서비스 호출
 * - 응답 직렬화
 *
 * 엔드포인트:
 * - GET /api/defects: 불량 목록 조회 (페이지네이션, 날짜 범위 필터, 키워드 검색)
 * - GET /api/defects/{defect_seq}: 불량 상세 조회
 * - POST /api/defects: 불량 등록 (multipart/form-data)
 * - PUT /api/defects/{defect_seq}: 불량 수정 (multipart/form-data)
 * - DELETE /api/defects/{defect_seq}: 불량 삭제
 * - GET /api/defects/{defect_seq}/image: 불량 이미지 다운로드
 *
 * 회사 격리:
 * - JWT의 companySeq claim에서 자동 추출
 * - 모든 작업에 강제 적용
 *
 * 파일 업로드:
 * - 허용 확장자: jpg, jpeg, png, gif, bmp
 * - 최대 크기: 10MB
 * - 선택 사항 (필수 아님)
 */
@Slf4j
@RestController
@RequestMapping("/api/defects")
public class DefectController {

    @Autowired
    private DefectService defectService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 불량 목록 조회
     *
     * 쿼리 파라미터:
     * - from: 시작 날짜 (yyyy-MM-dd, 필수)
     * - to: 종료 날짜 (yyyy-MM-dd, 필수)
     * - keyword: 검색 키워드 (선택, position/remarks 검색)
     * - page: 페이지 번호 (기본: 0)
     * - size: 페이지당 레코드 수 (기본: 10, 최대: 100)
     *
     * @param from 시작 날짜
     * @param to 종료 날짜
     * @param keyword 검색 키워드
     * @param page 페이지 번호
     * @param size 페이지당 레코드 수
     * @param companySeq 회사 ID (RequestAttribute에서 자동 주입)
     * @return 페이지네이션된 불량 목록
     */
    @GetMapping
    public ResponseEntity<PagedDefectResponse> getDefects(
        @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size,
        @RequestAttribute("companySeq") Long companySeq
    ) {
        // 페이지 크기 최대값 제한 (100)
        int validatedSize = Math.min(size, 100);

        // 정렬: occurDate desc (최신순)
        Pageable pageable = PageRequest.of(page, validatedSize, Sort.by(Sort.Direction.DESC, "occurDate"));

        PagedDefectResponse response = defectService.getDefects(companySeq, from, to, keyword, pageable);
        log.info("Get defects: companySeq={}, from={}, to={}, keyword={}, page={}, size={}",
            companySeq, from, to, keyword, page, validatedSize);
        return ResponseEntity.ok(response);
    }

    /**
     * 불량 상세 조회
     *
     * @param defectSeq 불량 ID
     * @param auth 인증 정보 (JWT에서 companySeq 추출용)
     * @return 불량 상세 정보
     */
    @GetMapping("/{defect_seq}")
    public ResponseEntity<DefectResponse> getDefect(
        @PathVariable(name = "defect_seq") Long defectSeq,
        Authentication auth
    ) {
        Long companySeq = extractCompanySeqFromToken(auth);
        DefectResponse response = defectService.getDefect(defectSeq, companySeq);
        log.info("Get defect: defectSeq={}, companySeq={}", defectSeq, companySeq);
        return ResponseEntity.ok(response);
    }

    /**
     * 불량 등록
     *
     * 요청 형식: multipart/form-data
     *
     * 폼 데이터:
     * - defect (JSON): 불량 정보
     *   - lineSeq: 생산 라인 ID
     *   - partSeq: 부품 ID
     *   - unitSeq: 공정 단위 ID
     *   - defectCodeSeq: 불량 코드 ID
     *   - quantity: 불량 수량
     *   - position: 불량 위치 (선택)
     *   - remarks: 비고 (선택)
     *   - occurDate: 불량 발생 일자 (yyyy-MM-dd)
     * - file: 이미지 파일 (선택)
     *
     * @param request 불량 등록 요청 DTO
     * @param file 이미지 파일 (선택)
     * @param auth 인증 정보 (JWT에서 companySeq 추출용)
     * @return 생성된 불량 정보
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DefectResponse> createDefect(
        @RequestPart(name = "defect") @Valid DefectCreateRequest request,
        @RequestPart(name = "file", required = false) MultipartFile file,
        Authentication auth
    ) {
        Long companySeq = extractCompanySeqFromToken(auth);
        String empEmail = auth.getName();

        DefectResponse response = defectService.createDefect(request, file, companySeq, empEmail);
        log.info("Create defect: defectSeq={}, companySeq={}, empEmail={}", response.getDefectSeq(), companySeq, empEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 불량 수정
     *
     * 요청 형식: multipart/form-data
     *
     * 폼 데이터:
     * - defect (JSON): 불량 정보
     *   - lineSeq: 생산 라인 ID
     *   - partSeq: 부품 ID
     *   - unitSeq: 공정 단위 ID
     *   - defectCodeSeq: 불량 코드 ID
     *   - quantity: 불량 수량
     *   - position: 불량 위치 (선택)
     *   - remarks: 비고 (선택)
     *   - occurDate: 불량 발생 일자 (yyyy-MM-dd)
     * - file: 이미지 파일 (선택, 있으면 기존 이미지 대체)
     *
     * @param defectSeq 불량 ID
     * @param request 불량 수정 요청 DTO
     * @param file 이미지 파일 (선택)
     * @param auth 인증 정보 (JWT에서 companySeq 추출용)
     * @return 수정된 불량 정보
     */
    @PutMapping(value = "/{defect_seq}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DefectResponse> updateDefect(
        @PathVariable(name = "defect_seq") Long defectSeq,
        @RequestPart(name = "defect") @Valid DefectUpdateRequest request,
        @RequestPart(name = "file", required = false) MultipartFile file,
        Authentication auth
    ) {
        Long companySeq = extractCompanySeqFromToken(auth);
        String empEmail = auth.getName();

        DefectResponse response = defectService.updateDefect(defectSeq, request, file, companySeq, empEmail);
        log.info("Update defect: defectSeq={}, companySeq={}, empEmail={}", defectSeq, companySeq, empEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * 불량 삭제
     *
     * @param defectSeq 불량 ID
     * @param auth 인증 정보 (JWT에서 companySeq 추출용)
     * @return 204 No Content
     */
    @DeleteMapping("/{defect_seq}")
    public ResponseEntity<Void> deleteDefect(
        @PathVariable(name = "defect_seq") Long defectSeq,
        Authentication auth
    ) {
        Long companySeq = extractCompanySeqFromToken(auth);
        defectService.deleteDefect(defectSeq, companySeq);
        log.info("Delete defect: defectSeq={}, companySeq={}", defectSeq, companySeq);
        return ResponseEntity.noContent().build();
    }

    /**
     * 불량 이미지 다운로드
     *
     * @param defectSeq 불량 ID
     * @param auth 인증 정보 (JWT에서 companySeq 추출용)
     * @return 이미지 파일 (Content-Type은 자동으로 감지)
     */
    @GetMapping("/{defect_seq}/image")
    public ResponseEntity<Resource> getImage(
        @PathVariable(name = "defect_seq") Long defectSeq,
        Authentication auth
    ) {
        Long companySeq = extractCompanySeqFromToken(auth);

        // 불량 조회
        DefectResponse defect = defectService.getDefect(defectSeq, companySeq);
        if (defect.getHasImage() == null || !defect.getHasImage()) {
            return ResponseEntity.notFound().build();
        }

        // 이미지 로드 및 반환
        String imagePath = defectService.getDefectImagePath(defectSeq, companySeq);
        Resource resource = fileStorageService.loadFileAsResource(imagePath);

        log.info("Download image: defectSeq={}, companySeq={}", defectSeq, companySeq);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
            .body(resource);
    }

    /**
     * JWT 토큰에서 companySeq 추출
     *
     * 구현 방식:
     * - Spring Security의 Authentication 객체에서 authorities 확인
     * - "company_<companySeq>" 형식의 authority 검색
     * - 없으면 예외 발생 (회사 정보가 없음)
     *
     * @param auth 인증 정보
     * @return 회사 ID
     * @throws RuntimeException 회사 정보를 찾을 수 없으면 예외 발생
     */
    private Long extractCompanySeqFromToken(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("인증 정보가 없습니다");
        }

        return auth.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .filter(authority -> authority.startsWith("company_"))
            .findFirst()
            .map(authority -> Long.parseLong(authority.substring(8))) // "company_" 제거
            .orElseThrow(() -> new RuntimeException("회사 정보를 찾을 수 없습니다"));
    }
}
