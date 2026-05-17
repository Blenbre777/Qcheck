package com.qcheck.qcheck.repository;

import com.qcheck.qcheck.entity.Line;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 라인 저장소 (JPA Repository)
 *
 * 책임:
 * - 라인(line) 테이블 CRUD 작업
 * - 회사별, 활성화 상태별 라인 조회
 *
 * 쿼리 메서드:
 * - findByCompanySeq: 회사별 모든 라인 조회
 * - findByCompanySeqAndUseYn: 회사별 활성화 라인만 조회 (드롭다운)
 * - findByCompanySeqAndLineNm: 회사별 라인명 중복 확인
 * - findByCompanySeqAndLineSeq: 라인 상세 조회 (권한 확인용)
 */
@Repository
public interface LineRepository extends JpaRepository<Line, Long> {

    /**
     * 회사별 모든 라인 조회
     *
     * @param companySeq 회사 ID
     * @return 라인 목록 (삭제된 항목 포함)
     */
    List<Line> findByCompanySeq(Long companySeq);

    /**
     * 회사별 활성화된 라인만 조회
     *
     * 용도: 불량 등록 폼의 라인 드롭다운
     *
     * @param companySeq 회사 ID
     * @param useYn 사용 여부 (Y)
     * @return 활성화 라인 목록
     */
    List<Line> findByCompanySeqAndUseYn(Long companySeq, String useYn);

    /**
     * 회사별 라인명 중복 확인
     *
     * @param companySeq 회사 ID
     * @param lineNm 라인명
     * @return Optional<Line>
     */
    Optional<Line> findByCompanySeqAndLineNm(Long companySeq, String lineNm);

    /**
     * 라인 상세 조회 (권한 확인용)
     *
     * 라인 수정/삭제 전 접근 권한 확인 시 사용
     *
     * @param lineSeq 라인 ID
     * @param companySeq 회사 ID
     * @return Optional<Line>
     */
    Optional<Line> findByLineSeqAndCompanySeq(Long lineSeq, Long companySeq);
}
