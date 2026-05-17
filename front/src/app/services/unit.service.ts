import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UnitResponse {
  unitSeq: number;
  unitNm: string;
  useYn: string;
  createdAt: string;
  createdEp: string;
  updatedAt: string;
  updatedEp: string;
}

export interface CreateUnitRequest {
  unitNm: string;
}

export interface UpdateUnitRequest {
  unitNm: string;
  useYn?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UnitService {
  private apiUrl = '/api/master/units';

  constructor(private http: HttpClient) {}

  /**
   * 공정 단위 목록 조회
   */
  getUnits(): Observable<UnitResponse[]> {
    return this.http.get<UnitResponse[]>(this.apiUrl);
  }

  /**
   * 공정 단위 상세 조회
   */
  getUnit(unitSeq: number): Observable<UnitResponse> {
    return this.http.get<UnitResponse>(`${this.apiUrl}/${unitSeq}`);
  }

  /**
   * 공정 단위 생성
   */
  createUnit(request: CreateUnitRequest): Observable<UnitResponse> {
    return this.http.post<UnitResponse>(this.apiUrl, request);
  }

  /**
   * 공정 단위 수정
   */
  updateUnit(unitSeq: number, request: UpdateUnitRequest): Observable<UnitResponse> {
    return this.http.put<UnitResponse>(`${this.apiUrl}/${unitSeq}`, request);
  }

  /**
   * 공정 단위 삭제
   */
  deleteUnit(unitSeq: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${unitSeq}`);
  }
}
