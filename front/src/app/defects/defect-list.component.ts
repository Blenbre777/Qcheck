import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DefectService, DefectResponse, PagedDefectResponse } from '../services/defect.service';

/**
 * 불량 목록 컴포넌트
 *
 * 책임:
 * - 불량 목록 조회 및 표시
 * - 날짜 범위 필터 (기본: 최근 7일)
 * - 키워드 검색
 * - 페이지네이션
 * - 이미지 표시 및 다운로드
 * - 불량 삭제 (확인 다이얼로그)
 * - 수정 페이지 이동
 *
 * 상태 관리:
 * - Signal 기반 상태 (data, loading, error, currentPage)
 */
@Component({
  selector: 'app-defect-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="defect-list-container">
      <div class="header">
        <h2>불량 관리</h2>
        <div class="header-actions">
          <a routerLink="/dashboard" class="btn btn-back">← 대시보드</a>
          <a routerLink="/defects/add" class="btn btn-primary">불량 등록</a>
        </div>
      </div>

      <!-- 에러 메시지 -->
      <div *ngIf="error()" class="alert alert-danger">
        {{ error() }}
      </div>

      <!-- 필터 섹션 -->
      <div class="filter-section">
        <div class="filter-group">
          <label for="fromDate">시작 날짜</label>
          <input
            id="fromDate"
            type="date"
            [value]="fromDate()"
            (change)="onFromDateChange($event)"
            class="form-control"
          />
        </div>

        <div class="filter-group">
          <label for="toDate">종료 날짜</label>
          <input
            id="toDate"
            type="date"
            [value]="toDate()"
            (change)="onToDateChange($event)"
            class="form-control"
          />
        </div>

        <div class="filter-group">
          <label for="keyword">키워드 검색</label>
          <input
            id="keyword"
            type="text"
            [value]="keyword()"
            (change)="onKeywordChange($event)"
            placeholder="위치, 비고 검색"
            class="form-control"
          />
        </div>

        <button (click)="search()" [disabled]="loading()" class="btn btn-info">
          검색
        </button>
      </div>

      <!-- 로딩 상태 -->
      <div *ngIf="loading()" class="spinner">로딩 중...</div>

      <!-- 불량 목록 테이블 -->
      <table class="table table-striped" *ngIf="!loading()">
        <thead>
          <tr>
            <th>발생일</th>
            <th>공정</th>
            <th>라인</th>
            <th>부품</th>
            <th>불량코드</th>
            <th>위치</th>
            <th>수량</th>
            <th>이미지</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let defect of data()?.content">
            <td>{{ defect.occurDate }}</td>
            <td>{{ defect.unitNm }}</td>
            <td>{{ defect.lineNm }}</td>
            <td>{{ defect.partNm }}</td>
            <td>{{ defect.codeNm }}</td>
            <td>{{ defect.position || '-' }}</td>
            <td>{{ defect.quantity }}</td>
            <td class="text-center">
              <a
                *ngIf="defect.hasImage"
                [href]="getImageUrl(defect.defectSeq)"
                target="_blank"
                rel="noopener noreferrer"
                class="image-link"
                title="이미지 보기"
              >
                🖼
              </a>
              <span *ngIf="!defect.hasImage" class="text-muted">-</span>
            </td>
            <td>
              <a [routerLink]="['/defects', defect.defectSeq, 'edit']" class="btn btn-sm btn-info">
                수정
              </a>
              <button
                (click)="confirmDelete(defect)"
                class="btn btn-sm btn-danger"
                [disabled]="loading()"
              >
                삭제
              </button>
            </td>
          </tr>
          <tr *ngIf="!data() || data()!.content.length === 0">
            <td colspan="9" class="text-center">불량이 없습니다</td>
          </tr>
        </tbody>
      </table>

      <!-- 페이지네이션 -->
      <div class="pagination-section" *ngIf="data() && data()!.totalPages > 1">
        <button
          (click)="previousPage()"
          [disabled]="currentPage() === 0 || loading()"
          class="btn btn-secondary"
        >
          이전
        </button>

        <span class="page-info">
          {{ currentPage() + 1 }} / {{ data()!.totalPages }} 페이지
        </span>

        <button
          (click)="nextPage()"
          [disabled]="currentPage() >= data()!.totalPages - 1 || loading()"
          class="btn btn-secondary"
        >
          다음
        </button>
      </div>
    </div>
  `,
  styles: [`
    .defect-list-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .header-actions {
      display: flex;
      gap: 10px;
      align-items: center;
    }

    .btn-back {
      background-color: #f8f9fa;
      color: #495057;
      border: 1px solid #dee2e6;
      padding: 6px 14px;
      border-radius: 4px;
      text-decoration: none;
      font-size: 0.9em;
    }

    .btn-back:hover {
      background-color: #e9ecef;
    }

    .filter-section {
      display: flex;
      gap: 10px;
      margin-bottom: 20px;
      padding: 15px;
      background-color: #f8f9fa;
      border-radius: 4px;
      flex-wrap: wrap;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 5px;
      min-width: 150px;
    }

    .filter-group label {
      font-weight: 500;
      font-size: 0.9em;
    }

    .filter-group input {
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 0.9em;
    }

    .form-control {
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .spinner {
      text-align: center;
      padding: 40px;
      color: #666;
    }

    .table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
    }

    .table thead {
      background-color: #f8f9fa;
    }

    .table th, .table td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
      font-size: 0.9em;
    }

    .table th {
      font-weight: 600;
    }

    .table tbody tr:hover {
      background-color: #f5f5f5;
    }

    .text-center {
      text-align: center;
    }

    .image-link {
      color: #0066cc;
      text-decoration: none;
      font-size: 1.2em;
    }

    .image-link:hover {
      text-decoration: underline;
    }

    .text-muted {
      color: #999;
    }

    .btn {
      padding: 8px 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.85em;
      text-decoration: none;
      display: inline-block;
    }

    .btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-primary {
      background-color: #007bff;
      color: white;
      border-color: #007bff;
    }

    .btn-primary:hover:not(:disabled) {
      background-color: #0056b3;
    }

    .btn-secondary {
      background-color: #6c757d;
      color: white;
      border-color: #6c757d;
    }

    .btn-secondary:hover:not(:disabled) {
      background-color: #545b62;
    }

    .btn-info {
      background-color: #17a2b8;
      color: white;
      border-color: #17a2b8;
      margin-right: 5px;
    }

    .btn-info:hover:not(:disabled) {
      background-color: #138496;
    }

    .btn-danger {
      background-color: #dc3545;
      color: white;
      border-color: #dc3545;
    }

    .btn-danger:hover:not(:disabled) {
      background-color: #c82333;
    }

    .btn-sm {
      padding: 5px 8px;
      font-size: 0.8em;
    }

    .alert {
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 20px;
    }

    .alert-danger {
      background-color: #f8d7da;
      color: #721c24;
      border: 1px solid #f5c6cb;
    }

    .pagination-section {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 10px;
      margin-top: 30px;
      padding: 20px;
    }

    .page-info {
      padding: 0 20px;
      font-weight: 500;
    }
  `]
})
export class DefectListComponent implements OnInit {
  // 상태 관리
  data = signal<PagedDefectResponse | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);
  currentPage = signal(0);

  // 필터 상태
  fromDate = signal<string>('');
  toDate = signal<string>('');
  keyword = signal<string>('');

  private pageSize = 10;

  constructor(private defectService: DefectService) {}

  ngOnInit(): void {
    // 기본값: 최근 7일
    const today = new Date();
    const sevenDaysAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);

    this.fromDate.set(this.formatDate(sevenDaysAgo));
    this.toDate.set(this.formatDate(today));

    // 초기 로드
    this.loadDefects();
  }

  /**
   * 불량 목록 로드
   */
  loadDefects(): void {
    this.loading.set(true);
    this.error.set(null);

    const from = this.fromDate();
    const to = this.toDate();
    const keyword = this.keyword() || null;

    this.defectService.getDefects(from, to, keyword, this.currentPage(), this.pageSize)
      .subscribe({
        next: (response) => {
          this.data.set(response);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(err.error?.message || '불량 목록 조회 실패');
          this.loading.set(false);
        }
      });
  }

  /**
   * 검색
   */
  search(): void {
    this.currentPage.set(0);
    this.loadDefects();
  }

  /**
   * 이전 페이지
   */
  previousPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      this.loadDefects();
    }
  }

  /**
   * 다음 페이지
   */
  nextPage(): void {
    if (this.data() && this.currentPage() < this.data()!.totalPages - 1) {
      this.currentPage.update(p => p + 1);
      this.loadDefects();
    }
  }

  /**
   * 삭제 확인 다이얼로그
   */
  confirmDelete(defect: DefectResponse): void {
    if (confirm(`불량 ID ${defect.defectSeq}을 삭제하시겠습니까?`)) {
      this.deleteDefect(defect.defectSeq);
    }
  }

  /**
   * 불량 삭제
   */
  deleteDefect(seq: number): void {
    this.loading.set(true);
    this.defectService.deleteDefect(seq).subscribe({
      next: () => {
        this.currentPage.set(0);
        this.loadDefects();
      },
      error: (err) => {
        this.error.set(err.error?.message || '불량 삭제 실패');
        this.loading.set(false);
      }
    });
  }

  /**
   * 이미지 URL 생성
   */
  getImageUrl(seq: number): string {
    return this.defectService.getImageUrl(seq);
  }

  /**
   * 시작 날짜 변경
   */
  onFromDateChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.fromDate.set(input.value);
  }

  /**
   * 종료 날짜 변경
   */
  onToDateChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.toDate.set(input.value);
  }

  /**
   * 키워드 변경
   */
  onKeywordChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.keyword.set(input.value);
  }

  /**
   * 날짜를 yyyy-MM-dd 형식으로 변환
   */
  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
