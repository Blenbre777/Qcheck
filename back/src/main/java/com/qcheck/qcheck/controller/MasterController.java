package com.qcheck.qcheck.controller;

import com.qcheck.qcheck.dto.request.CreateDefectCodeRequest;
import com.qcheck.qcheck.dto.request.CreateLineRequest;
import com.qcheck.qcheck.dto.request.CreatePartRequest;
import com.qcheck.qcheck.dto.request.CreateUnitRequest;
import com.qcheck.qcheck.dto.request.UpdateDefectCodeRequest;
import com.qcheck.qcheck.dto.request.UpdateLineRequest;
import com.qcheck.qcheck.dto.request.UpdatePartRequest;
import com.qcheck.qcheck.dto.request.UpdateUnitRequest;
import com.qcheck.qcheck.dto.response.DefectCodeResponse;
import com.qcheck.qcheck.dto.response.LineResponse;
import com.qcheck.qcheck.dto.response.PartResponse;
import com.qcheck.qcheck.dto.response.UnitResponse;
import com.qcheck.qcheck.service.master.DefectCodeService;
import com.qcheck.qcheck.service.master.LineService;
import com.qcheck.qcheck.service.master.PartService;
import com.qcheck.qcheck.service.master.UnitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 마스터 데이터 컨트롤러
 *
 * 책임:
 * - 라인, 부품, 공정 단위, 불량 코드의 REST 엔드포인트 제공
 * - 회사별 격리 처리 (Authentication에서 companySeq 추출)
 * - 요청 검증 및 응답 형식화
 *
 * 엔드포인트:
 * - GET /api/master/lines - 라인 목록
 * - GET /api/master/lines/{lineSeq} - 라인 상세
 * - POST /api/master/lines - 라인 생성
 * - PUT /api/master/lines/{lineSeq} - 라인 수정
 * - DELETE /api/master/lines/{lineSeq} - 라인 삭제
 * - 부품, 공정 단위, 불량 코드도 동일 패턴
 */
@Slf4j
@RestController
@RequestMapping("/api/master")
public class MasterController {

    @Autowired
    private LineService lineService;

    @Autowired
    private PartService partService;

    @Autowired
    private UnitService unitService;

    @Autowired
    private DefectCodeService defectCodeService;

    // ===== LINE ENDPOINTS =====

    /**
     * 라인 목록 조회
     */
    @GetMapping("/lines")
    public ResponseEntity<List<LineResponse>> getLines(HttpServletRequest request) {
        Long companySeq = getCompanySeqFromRequest(request);
        return ResponseEntity.ok(lineService.getLinesByCompany(companySeq));
    }

    /**
     * 라인 상세 조회
     */
    @GetMapping("/lines/{lineSeq}")
    public ResponseEntity<LineResponse> getLine(@PathVariable Long lineSeq, HttpServletRequest request, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(request);
        return ResponseEntity.ok(lineService.getLine(lineSeq, companySeq));
    }

    /**
     * 라인 생성
     */
    @PostMapping("/lines")
    public ResponseEntity<LineResponse> createLine(@Valid @RequestBody CreateLineRequest request, HttpServletRequest httpRequest, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        String empEmail = authentication.getName();
        LineResponse response = lineService.createLine(request, companySeq, empEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 라인 수정
     */
    @PutMapping("/lines/{lineSeq}")
    public ResponseEntity<LineResponse> updateLine(@PathVariable Long lineSeq, @Valid @RequestBody UpdateLineRequest request, HttpServletRequest httpRequest, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        String empEmail = authentication.getName();
        LineResponse response = lineService.updateLine(lineSeq, request, companySeq, empEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * 라인 삭제
     */
    @DeleteMapping("/lines/{lineSeq}")
    public ResponseEntity<Void> deleteLine(@PathVariable Long lineSeq, HttpServletRequest request, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(request);
        String empEmail = authentication.getName();
        lineService.deleteLine(lineSeq, companySeq, empEmail);
        return ResponseEntity.noContent().build();
    }

    // ===== PART ENDPOINTS =====

    /**
     * 부품 목록 조회
     */
    @GetMapping("/parts")
    public ResponseEntity<List<PartResponse>> getParts(HttpServletRequest request) {
        Long companySeq = getCompanySeqFromRequest(request);
        return ResponseEntity.ok(partService.getPartsByCompany(companySeq));
    }

    /**
     * 부품 상세 조회
     */
    @GetMapping("/parts/{partSeq}")
    public ResponseEntity<PartResponse> getPart(@PathVariable Long partSeq, HttpServletRequest request, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(request);
        return ResponseEntity.ok(partService.getPart(partSeq, companySeq));
    }

    /**
     * 부품 생성
     */
    @PostMapping("/parts")
    public ResponseEntity<PartResponse> createPart(@Valid @RequestBody CreatePartRequest request, HttpServletRequest httpRequest, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        String empEmail = authentication.getName();
        PartResponse response = partService.createPart(request, companySeq, empEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 부품 수정
     */
    @PutMapping("/parts/{partSeq}")
    public ResponseEntity<PartResponse> updatePart(@PathVariable Long partSeq, @Valid @RequestBody UpdatePartRequest request, HttpServletRequest httpRequest, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        String empEmail = authentication.getName();
        PartResponse response = partService.updatePart(partSeq, request, companySeq, empEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * 부품 삭제
     */
    @DeleteMapping("/parts/{partSeq}")
    public ResponseEntity<Void> deletePart(@PathVariable Long partSeq, HttpServletRequest request, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(request);
        String empEmail = authentication.getName();
        partService.deletePart(partSeq, companySeq, empEmail);
        return ResponseEntity.noContent().build();
    }

    // ===== UNIT ENDPOINTS =====

    /**
     * 공정 단위 목록 조회
     */
    @GetMapping("/units")
    public ResponseEntity<List<UnitResponse>> getUnits(HttpServletRequest request) {
        Long companySeq = getCompanySeqFromRequest(request);
        return ResponseEntity.ok(unitService.getUnitsByCompany(companySeq));
    }

    /**
     * 공정 단위 상세 조회
     */
    @GetMapping("/units/{unitSeq}")
    public ResponseEntity<UnitResponse> getUnit(@PathVariable Long unitSeq, HttpServletRequest request, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(request);
        return ResponseEntity.ok(unitService.getUnit(unitSeq, companySeq));
    }

    /**
     * 공정 단위 생성
     */
    @PostMapping("/units")
    public ResponseEntity<UnitResponse> createUnit(@Valid @RequestBody CreateUnitRequest request, HttpServletRequest httpRequest, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        String empEmail = authentication.getName();
        UnitResponse response = unitService.createUnit(request, companySeq, empEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 공정 단위 수정
     */
    @PutMapping("/units/{unitSeq}")
    public ResponseEntity<UnitResponse> updateUnit(@PathVariable Long unitSeq, @Valid @RequestBody UpdateUnitRequest request, HttpServletRequest httpRequest, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        String empEmail = authentication.getName();
        UnitResponse response = unitService.updateUnit(unitSeq, request, companySeq, empEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * 공정 단위 삭제
     */
    @DeleteMapping("/units/{unitSeq}")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long unitSeq, HttpServletRequest request, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(request);
        String empEmail = authentication.getName();
        unitService.deleteUnit(unitSeq, companySeq, empEmail);
        return ResponseEntity.noContent().build();
    }

    // ===== DEFECT CODE ENDPOINTS =====

    /**
     * 불량 코드 목록 조회
     */
    @GetMapping("/defect-codes")
    public ResponseEntity<List<DefectCodeResponse>> getDefectCodes(HttpServletRequest request) {
        Long companySeq = getCompanySeqFromRequest(request);
        return ResponseEntity.ok(defectCodeService.getDefectCodesByCompany(companySeq));
    }

    /**
     * 불량 코드 상세 조회
     */
    @GetMapping("/defect-codes/{defectCodeSeq}")
    public ResponseEntity<DefectCodeResponse> getDefectCode(@PathVariable Long defectCodeSeq, HttpServletRequest request, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(request);
        return ResponseEntity.ok(defectCodeService.getDefectCode(defectCodeSeq, companySeq));
    }

    /**
     * 불량 코드 생성
     */
    @PostMapping("/defect-codes")
    public ResponseEntity<DefectCodeResponse> createDefectCode(@Valid @RequestBody CreateDefectCodeRequest request, HttpServletRequest httpRequest, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        String empEmail = authentication.getName();
        DefectCodeResponse response = defectCodeService.createDefectCode(request, companySeq, empEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 불량 코드 수정
     */
    @PutMapping("/defect-codes/{defectCodeSeq}")
    public ResponseEntity<DefectCodeResponse> updateDefectCode(@PathVariable Long defectCodeSeq, @Valid @RequestBody UpdateDefectCodeRequest request, HttpServletRequest httpRequest, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(httpRequest);
        String empEmail = authentication.getName();
        DefectCodeResponse response = defectCodeService.updateDefectCode(defectCodeSeq, request, companySeq, empEmail);
        return ResponseEntity.ok(response);
    }

    /**
     * 불량 코드 삭제
     */
    @DeleteMapping("/defect-codes/{defectCodeSeq}")
    public ResponseEntity<Void> deleteDefectCode(@PathVariable Long defectCodeSeq, HttpServletRequest request, Authentication authentication) {
        Long companySeq = getCompanySeqFromRequest(request);
        String empEmail = authentication.getName();
        defectCodeService.deleteDefectCode(defectCodeSeq, companySeq, empEmail);
        return ResponseEntity.noContent().build();
    }

    // ===== HELPER METHODS =====

    /**
     * HttpServletRequest에서 companySeq 추출
     * JwtAuthenticationFilter에서 요청 속성에 저장된 companySeq를 반환
     *
     * @param request HTTP 요청
     * @return companySeq
     * @throws RuntimeException companySeq를 찾을 수 없으면 예외 발생
     */
    private Long getCompanySeqFromRequest(HttpServletRequest request) {
        try {
            Object companySeqAttr = request.getAttribute("companySeq");
            if (companySeqAttr instanceof Long) {
                return (Long) companySeqAttr;
            }
            throw new RuntimeException("회사 정보를 찾을 수 없습니다");
        } catch (Exception e) {
            throw new RuntimeException("회사 정보 추출 중 오류 발생: " + e.getMessage());
        }
    }
}
