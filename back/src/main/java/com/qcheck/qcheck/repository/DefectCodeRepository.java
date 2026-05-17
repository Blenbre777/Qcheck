package com.qcheck.qcheck.repository;

import com.qcheck.qcheck.entity.DefectCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 불량 코드 저장소 (JPA Repository)
 *
 * 책임:
 * - 불량 코드(defect_code) 테이블 CRUD 작업
 * - 회사별, 활성화 상태별 불량 코드 조회
 *
 * 쿼리 메서드:
 * - findByCompanySeq: 회사별 모든 불량 코드 조회
 * - findByCompanySeqAndUseYn: 회사별 활성화 불량 코드만 조회 (드롭다운)
 * - findByCompanySeqAndCodeNm: 회사별 불량 코드명 중복 확인
 * - findByCompanySeqAndDefectCodeSeq: 불량 코드 상세 조회 (권한 확인용)
 */
@Repository
public interface DefectCodeRepository extends JpaRepository<DefectCode, Long> {

    /**
     * 회사별 모든 불량 코드 조회
     *
     * @param companySeq 회사 ID
     * @return 불량 코드 목록 (삭제된 항목 포함)
     */
    List<DefectCode> findByCompanySeq(Long companySeq);

    /**
     * 회사별 활성화된 불량 코드만 조회
     *
     * 용도: 불량 등록 폼의 불량 코드 드롭다운
     *
     * @param companySeq 회사 ID
     * @param useYn 사용 여부 (Y)
     * @return 활성화 불량 코드 목록
     */
    List<DefectCode> findByCompanySeqAndUseYn(Long companySeq, String useYn);

    /**
     * 회사별 불량 코드명 중복 확인
     *
     * @param companySeq 회사 ID
     * @param codeNm 불량 코드명
     * @return Optional<DefectCode>
     */
    Optional<DefectCode> findByCompanySeqAndCodeNm(Long companySeq, String codeNm);

    /**
     * 불량 코드 상세 조회 (권한 확인용)
     *
     * 불량 코드 수정/삭제 전 접근 권한 확인 시 사용
     *
     * @param defectCodeSeq 불량 코드 ID
     * @param companySeq 회사 ID
     * @return Optional<DefectCode>
     */
    Optional<DefectCode> findByDefectCodeSeqAndCompanySeq(Long defectCodeSeq, Long companySeq);
}
