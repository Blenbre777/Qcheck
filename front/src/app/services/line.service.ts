import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface LineResponse {
  lineSeq: number;
  lineNm: string;
  useYn: string;
  createdAt: string;
  createdEp: string;
  updatedAt: string;
  updatedEp: string;
}

export interface CreateLineRequest {
  lineNm: string;
}

export interface UpdateLineRequest {
  lineNm: string;
  useYn?: string;
}

@Injectable({
  providedIn: 'root'
})
export class LineService {
  private apiUrl = 'http://localhost:8081/api/master/lines';

  constructor(private http: HttpClient) {}

  /**
   * 라인 목록 조회
   */
  getLines(): Observable<LineResponse[]> {
    return this.http.get<LineResponse[]>(this.apiUrl);
  }

  /**
   * 라인 상세 조회
   */
  getLine(lineSeq: number): Observable<LineResponse> {
    return this.http.get<LineResponse>(`${this.apiUrl}/${lineSeq}`);
  }

  /**
   * 라인 생성
   */
  createLine(request: CreateLineRequest): Observable<LineResponse> {
    return this.http.post<LineResponse>(this.apiUrl, request);
  }

  /**
   * 라인 수정
   */
  updateLine(lineSeq: number, request: UpdateLineRequest): Observable<LineResponse> {
    return this.http.put<LineResponse>(`${this.apiUrl}/${lineSeq}`, request);
  }

  /**
   * 라인 삭제
   */
  deleteLine(lineSeq: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${lineSeq}`);
  }
}
