import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * 대시보드 통계 조회 응답 인터페이스
 *
 * 필드:
 * - total_defect_count: 불량 총 건수
 * - unique_position_count: 고유 위치 개수
 * - defect_count_by_line: 라인별 불량 수 집계 (내림차순)
 * - defect_count_by_code: 불량 코드별 불량 수 집계 (내림차순)
 */
export interface DashboardResponse {
  total_defect_count: number;
  unique_position_count: number;
  defect_count_by_line: DefectCountByLine[];
  defect_count_by_code: DefectCountByCode[];
}

/**
 * 라인별 불량 수 집계 인터페이스
 */
export interface DefectCountByLine {
  line_seq: number;
  line_nm: string;
  defect_count: number;
}

/**
 * 불량 코드별 불량 수 집계 인터페이스
 */
export interface DefectCountByCode {
  defect_code_seq: number;
  code_nm: string;
  defect_count: number;
}

/**
 * 대시보드 서비스
 *
 * 책임:
 * - 대시보드 통계 API 호출
 * - 라인별, 불량 코드별 불량 수 조회
 * - 대시보드 응답 데이터 제공
 *
 * 특징:
 * - HTTP 클라이언트를 통한 REST API 통신
 * - 날짜 범위 필터링 지원 (from ~ to)
 * - JWT 토큰 자동 처리 (HTTP Interceptor)
 * - Observable 기반 비동기 처리
 */
@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8081/api/dashboard';

  constructor(private http: HttpClient) {}

  /**
   * 대시보드 통계 조회
   *
   * API 요청:
   * GET /api/dashboard?from=yyyy-MM-dd&to=yyyy-MM-dd
   *
   * @param from 시작 날짜 (yyyy-MM-dd)
   * @param to 종료 날짜 (yyyy-MM-dd)
   * @returns 대시보드 통계 응답 (총 건수, 고유 위치, 라인별, 코드별 집계)
   */
  getDashboardStats(from: string, to: string): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.apiUrl}`, {
      params: { from, to }
    });
  }

  /**
   * 라인별 불량 수 조회
   *
   * API 요청:
   * GET /api/dashboard/line?from=yyyy-MM-dd&to=yyyy-MM-dd
   *
   * @param from 시작 날짜 (yyyy-MM-dd)
   * @param to 종료 날짜 (yyyy-MM-dd)
   * @returns 라인별 불량 수 집계 (내림차순)
   */
  getDefectCountByLine(from: string, to: string): Observable<DefectCountByLine[]> {
    return this.http.get<DefectCountByLine[]>(`${this.apiUrl}/line`, {
      params: { from, to }
    });
  }

  /**
   * 불량 코드별 불량 수 조회
   *
   * API 요청:
   * GET /api/dashboard/code?from=yyyy-MM-dd&to=yyyy-MM-dd
   *
   * @param from 시작 날짜 (yyyy-MM-dd)
   * @param to 종료 날짜 (yyyy-MM-dd)
   * @returns 불량 코드별 불량 수 집계 (내림차순)
   */
  getDefectCountByCode(from: string, to: string): Observable<DefectCountByCode[]> {
    return this.http.get<DefectCountByCode[]>(`${this.apiUrl}/code`, {
      params: { from, to }
    });
  }
}
