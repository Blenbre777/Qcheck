import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PartResponse {
  partSeq: number;
  partNm: string;
  useYn: string;
  createdAt: string;
  createdEp: string;
  updatedAt: string;
  updatedEp: string;
}

export interface CreatePartRequest {
  partNm: string;
}

export interface UpdatePartRequest {
  partNm: string;
  useYn?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PartService {
  private apiUrl = '/api/master/parts';

  constructor(private http: HttpClient) {}

  /**
   * 부품 목록 조회
   */
  getParts(): Observable<PartResponse[]> {
    return this.http.get<PartResponse[]>(this.apiUrl);
  }

  /**
   * 부품 상세 조회
   */
  getPart(partSeq: number): Observable<PartResponse> {
    return this.http.get<PartResponse>(`${this.apiUrl}/${partSeq}`);
  }

  /**
   * 부품 생성
   */
  createPart(request: CreatePartRequest): Observable<PartResponse> {
    return this.http.post<PartResponse>(this.apiUrl, request);
  }

  /**
   * 부품 수정
   */
  updatePart(partSeq: number, request: UpdatePartRequest): Observable<PartResponse> {
    return this.http.put<PartResponse>(`${this.apiUrl}/${partSeq}`, request);
  }

  /**
   * 부품 삭제
   */
  deletePart(partSeq: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${partSeq}`);
  }
}
