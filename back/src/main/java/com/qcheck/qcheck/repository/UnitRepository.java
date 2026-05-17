package com.qcheck.qcheck.repository;

import com.qcheck.qcheck.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 공정 단위 저장소 (JPA Repository)
 *
 * 책임:
 * - 공정 단위(unit) 테이블 CRUD 작업
 * - 회사별, 활성화 상태별 공정 단위 조회
 *
 * 쿼리 메서드:
 * - findByCompanySeq: 회사별 모든 공정 단위 조회
 * - findByCompanySeqAndUseYn: 회사별 활성화 공정 단위만 조회 (드롭다운)
 * - findByCompanySeqAndUnitNm: 회사별 공정 단위명 중복 확인
 * - findByCompanySeqAndUnitSeq: 공정 단위 상세 조회 (권한 확인용)
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    /**
     * 회사별 모든 공정 단위 조회
     *
     * @param companySeq 회사 ID
     * @return 공정 단위 목록 (삭제된 항목 포함)
     */
    List<Unit> findByCompanySeq(Long companySeq);

    /**
     * 회사별 활성화된 공정 단위만 조회
     *
     * 용도: 불량 등록 폼의 공정 단위 드롭다운
     *
     * @param companySeq 회사 ID
     * @param useYn 사용 여부 (Y)
     * @return 활성화 공정 단위 목록
     */
    List<Unit> findByCompanySeqAndUseYn(Long companySeq, String useYn);

    /**
     * 회사별 공정 단위명 중복 확인
     *
     * @param companySeq 회사 ID
     * @param unitNm 공정 단위명
     * @return Optional<Unit>
     */
    Optional<Unit> findByCompanySeqAndUnitNm(Long companySeq, String unitNm);

    /**
     * 공정 단위 상세 조회 (권한 확인용)
     *
     * 공정 단위 수정/삭제 전 접근 권한 확인 시 사용
     *
     * @param unitSeq 공정 단위 ID
     * @param companySeq 회사 ID
     * @return Optional<Unit>
     */
    Optional<Unit> findByUnitSeqAndCompanySeq(Long unitSeq, Long companySeq);
}
