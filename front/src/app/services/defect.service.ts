import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * 불량 응답 DTO (단건 조회, 리스트 아이템)
 */
export interface DefectResponse {
  defectSeq: number;
  occurDate: string;       // yyyy-MM-dd
  unitNm: string;
  lineNm: string;
  partNm: string;
  codeNm: string;
  position: string;
  quantity: number;
  remarks?: string;
  hasImage: boolean;
  createdAt?: string;
  createdEp?: string;
  updatedAt?: string;
  updatedEp?: string;
}

/**
 * 페이징된 불량 응답
 */
export interface PagedDefectResponse {
  content: DefectResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/**
 * 불량 등록 요청 DTO
 */
export interface DefectCreateRequest {
  lineSeq: number;
  partSeq: number;
  unitSeq: number;
  defectCodeSeq: number;
  quantity: number;
  position?: string;
  remarks?: string;
  occurDate: string;  // yyyy-MM-dd
}

/**
 * 불량 수정 요청 DTO
 */
export interface DefectUpdateRequest {
  lineSeq: number;
  partSeq: number;
  unitSeq: number;
  defectCodeSeq: number;
  quantity: number;
  position?: string;
  remarks?: string;
  occurDate: string;  // yyyy-MM-dd
}

/**
 * 불량 관리 서비스
 *
 * 책임:
 * - 불량 목록 조회 (페이지네이션, 날짜 필터, 키워드 검색)
 * - 불량 상세 조회
 * - 불량 등록 (이미지 파일 포함)
 * - 불량 수정 (이미지 파일 포함)
 * - 불량 삭제
 * - 불량 이미지 URL 생성
 */
@Injectable({
  providedIn: 'root'
})
export class DefectService {
  private apiUrl = '/api/defects';

  constructor(private http: HttpClient) {}

  /**
   * 불량 목록 조회
   * @param from 시작 날짜 (yyyy-MM-dd)
   * @param to 종료 날짜 (yyyy-MM-dd)
   * @param keyword 검색 키워드 (선택)
   * @param page 페이지 번호 (0부터 시작)
   * @param size 페이지당 개수
   */
  getDefects(from: string, to: string, keyword: string | null, page: number, size: number): Observable<PagedDefectResponse> {
    let url = `${this.apiUrl}?from=${from}&to=${to}&page=${page}&size=${size}`;
    if (keyword) {
      url += `&keyword=${encodeURIComponent(keyword)}`;
    }
    return this.http.get<PagedDefectResponse>(url);
  }

  /**
   * 불량 상세 조회
   * @param seq 불량 ID
   */
  getDefect(seq: number): Observable<DefectResponse> {
    return this.http.get<DefectResponse>(`${this.apiUrl}/${seq}`);
  }

  /**
   * 불량 등록 (이미지 파일 포함)
   * @param formData FormData (defect JSON + file)
   */
  createDefect(formData: FormData): Observable<DefectResponse> {
    return this.http.post<DefectResponse>(this.apiUrl, formData);
  }

  /**
   * 불량 수정 (이미지 파일 포함)
   * @param seq 불량 ID
   * @param formData FormData (defect JSON + file)
   */
  updateDefect(seq: number, formData: FormData): Observable<DefectResponse> {
    return this.http.put<DefectResponse>(`${this.apiUrl}/${seq}`, formData);
  }

  /**
   * 불량 삭제
   * @param seq 불량 ID
   */
  deleteDefect(seq: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${seq}`);
  }

  /**
   * 불량 이미지 URL 생성
   * @param seq 불량 ID
   */
  getImageUrl(seq: number): string {
    return `${this.apiUrl}/${seq}/image`;
  }
}
