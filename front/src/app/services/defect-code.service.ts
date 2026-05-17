import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DefectCodeResponse {
  defectCodeSeq: number;
  defectCodeNm: string;
  description?: string;
  useYn: string;
  createdAt: string;
  createdEp: string;
  updatedAt: string;
  updatedEp: string;
}

export interface CreateDefectCodeRequest {
  codeNm: string;
  description?: string;
}

export interface UpdateDefectCodeRequest {
  codeNm: string;
  description?: string;
  useYn?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DefectCodeService {
  private apiUrl = 'http://localhost:8081/api/master/defect-codes';

  constructor(private http: HttpClient) {}

  /**
   * 불량 코드 목록 조회
   */
  getDefectCodes(): Observable<DefectCodeResponse[]> {
    return this.http.get<DefectCodeResponse[]>(this.apiUrl);
  }

  /**
   * 불량 코드 상세 조회
   */
  getDefectCode(defectCodeSeq: number): Observable<DefectCodeResponse> {
    return this.http.get<DefectCodeResponse>(`${this.apiUrl}/${defectCodeSeq}`);
  }

  /**
   * 불량 코드 생성
   */
  createDefectCode(request: CreateDefectCodeRequest): Observable<DefectCodeResponse> {
    return this.http.post<DefectCodeResponse>(this.apiUrl, request);
  }

  /**
   * 불량 코드 수정
   */
  updateDefectCode(defectCodeSeq: number, request: UpdateDefectCodeRequest): Observable<DefectCodeResponse> {
    return this.http.put<DefectCodeResponse>(`${this.apiUrl}/${defectCodeSeq}`, request);
  }

  /**
   * 불량 코드 삭제
   */
  deleteDefectCode(defectCodeSeq: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${defectCodeSeq}`);
  }
}
