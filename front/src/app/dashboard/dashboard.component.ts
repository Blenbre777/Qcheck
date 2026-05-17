import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService, DashboardResponse, DefectCountByLine, DefectCountByCode } from '../services/dashboard.service';

/**
 * 대시보드 컴포넌트
 *
 * 책임:
 * - 대시보드 페이지 전체 구성 및 표시
 * - 불량 통계 요약 (총 건수, 고유 위치 수)
 * - 라인별 불량 수 시각화 (테이블)
 * - 불량 코드별 불량 수 시각화 (테이블)
 * - 날짜 범위 필터 (기본: 최근 30일)
 *
 * 상태 관리:
 * - Signal 기반 상태 (data, loading, error, fromDate, toDate)
 * - 라인별 집계 데이터, 코드별 집계 데이터
 *
 * 레이아웃:
 * 1. 헤더: 제목 및 날짜 필터 입력
 * 2. 요약 통계: 4개 카드 (총 건수, 고유 위치)
 * 3. 라인별 분포: 테이블 형식
 * 4. 코드별 분포: 테이블 형식
 *
 * 참고:
 * - Chart.js는 필요시 나중에 추가 가능 (현재는 테이블 기반)
 * - 모든 데이터는 회사별 격리 (JWT companySeq에서 추출)
 * - 날짜 범위: from ~ to (yyyy-MM-dd 형식)
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="dashboard-container">
      <!-- 헤더 -->
      <div class="header">
        <h2>대시보드</h2>
      </div>

      <!-- 빠른 메뉴 -->
      <div class="quick-menu">
        <h3 class="quick-menu-title">빠른 메뉴</h3>
        <div class="quick-menu-grid">
          <a routerLink="/defects/add" class="quick-card quick-card-primary">
            <span class="quick-card-icon">＋</span>
            <span class="quick-card-label">불량 등록</span>
          </a>
          <a routerLink="/defects" class="quick-card quick-card-secondary">
            <span class="quick-card-icon">☰</span>
            <span class="quick-card-label">불량 목록</span>
          </a>
          <a routerLink="/master/lines" class="quick-card quick-card-master">
            <span class="quick-card-icon">≡</span>
            <span class="quick-card-label">라인 관리</span>
          </a>
          <a routerLink="/master/parts" class="quick-card quick-card-master">
            <span class="quick-card-icon">⧉</span>
            <span class="quick-card-label">파트 관리</span>
          </a>
          <a routerLink="/master/units" class="quick-card quick-card-master">
            <span class="quick-card-icon">◫</span>
            <span class="quick-card-label">단위 관리</span>
          </a>
          <a routerLink="/master/defect-codes" class="quick-card quick-card-master">
            <span class="quick-card-icon">⊞</span>
            <span class="quick-card-label">불량 코드 관리</span>
          </a>
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

        <button (click)="search()" [disabled]="loading()" class="btn btn-info">
          조회
        </button>
      </div>

      <!-- 로딩 상태 -->
      <div *ngIf="loading()" class="spinner">로딩 중...</div>

      <!-- 요약 통계 카드 -->
      <div *ngIf="!loading() && data()" class="summary-cards">
        <div class="card">
          <div class="card-title">불량 총 건수</div>
          <div class="card-value">{{ data()!.total_defect_count }}</div>
        </div>

        <div class="card">
          <div class="card-title">고유 위치 수</div>
          <div class="card-value">{{ data()!.unique_position_count }}</div>
        </div>
      </div>

      <!-- 라인별 불량 분포 -->
      <div *ngIf="!loading() && data()" class="distribution-section">
        <h3>라인별 불량 분포</h3>

        <div *ngIf="data()!.defect_count_by_line.length > 0" class="table-wrapper">
          <table class="table table-striped">
            <thead>
              <tr>
                <th>라인</th>
                <th>불량 수</th>
                <th>비율</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let line of data()!.defect_count_by_line">
                <td>{{ line.line_nm }}</td>
                <td>{{ line.defect_count }}</td>
                <td>
                  <div class="progress-bar-container">
                    <div
                      class="progress-bar"
                      [style.width.%]="getLinePercentage(line.defect_count)"
                    ></div>
                    <span class="percentage-text">{{ getLinePercentage(line.defect_count) | number: '1.1-1' }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div *ngIf="data()!.defect_count_by_line.length === 0" class="no-data">
          라인별 불량 데이터가 없습니다
        </div>
      </div>

      <!-- 불량 코드별 분포 -->
      <div *ngIf="!loading() && data()" class="distribution-section">
        <h3>불량 코드별 분포</h3>

        <div *ngIf="data()!.defect_count_by_code.length > 0" class="table-wrapper">
          <table class="table table-striped">
            <thead>
              <tr>
                <th>불량 코드</th>
                <th>불량 수</th>
                <th>비율</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let code of data()!.defect_count_by_code">
                <td>{{ code.code_nm }}</td>
                <td>{{ code.defect_count }}</td>
                <td>
                  <div class="progress-bar-container">
                    <div
                      class="progress-bar"
                      [style.width.%]="getCodePercentage(code.defect_count)"
                    ></div>
                    <span class="percentage-text">{{ getCodePercentage(code.defect_count) | number: '1.1-1' }}%</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div *ngIf="data()!.defect_count_by_code.length === 0" class="no-data">
          불량 코드별 데이터가 없습니다
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 20px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .header {
      margin-bottom: 30px;
    }

    .header h2 {
      font-size: 28px;
      font-weight: 600;
      margin: 0;
      color: #333;
    }

    .filter-section {
      display: flex;
      gap: 10px;
      margin-bottom: 30px;
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

    .btn {
      padding: 8px 16px;
      border: 1px solid #ddd;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.9em;
      text-decoration: none;
      display: inline-block;
      align-self: flex-end;
    }

    .btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .btn-info {
      background-color: #17a2b8;
      color: white;
      border-color: #17a2b8;
    }

    .btn-info:hover:not(:disabled) {
      background-color: #138496;
    }

    .spinner {
      text-align: center;
      padding: 40px;
      color: #666;
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

    .summary-cards {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
      margin-bottom: 40px;
    }

    .card {
      background: white;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .card-title {
      font-size: 0.9em;
      color: #666;
      font-weight: 500;
      margin-bottom: 10px;
    }

    .card-value {
      font-size: 32px;
      font-weight: 700;
      color: #007bff;
    }

    .distribution-section {
      margin-bottom: 40px;
    }

    .distribution-section h3 {
      font-size: 18px;
      font-weight: 600;
      margin-bottom: 20px;
      color: #333;
    }

    .table-wrapper {
      background: white;
      border-radius: 4px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      overflow: hidden;
    }

    .table {
      width: 100%;
      border-collapse: collapse;
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
      color: #333;
    }

    .table tbody tr:hover {
      background-color: #f5f5f5;
    }

    .progress-bar-container {
      display: flex;
      align-items: center;
      gap: 10px;
      position: relative;
      height: 30px;
    }

    .progress-bar {
      background-color: #007bff;
      height: 24px;
      border-radius: 4px;
      min-width: 0;
      transition: width 0.3s ease;
    }

    .percentage-text {
      font-weight: 500;
      color: #333;
      min-width: 50px;
    }

    .no-data {
      padding: 40px;
      text-align: center;
      color: #666;
      background: white;
      border-radius: 4px;
      border: 1px solid #e0e0e0;
    }

    .quick-menu {
      margin-bottom: 30px;
    }

    .quick-menu-title {
      font-size: 16px;
      font-weight: 600;
      color: #555;
      margin-bottom: 12px;
    }

    .quick-menu-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
      gap: 12px;
    }

    .quick-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 16px 12px;
      border-radius: 8px;
      text-decoration: none;
      font-weight: 500;
      font-size: 0.9em;
      transition: transform 0.1s ease, box-shadow 0.1s ease;
      cursor: pointer;
    }

    .quick-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }

    .quick-card-icon {
      font-size: 22px;
      line-height: 1;
    }

    .quick-card-label {
      text-align: center;
      line-height: 1.3;
    }

    .quick-card-primary {
      background-color: #007bff;
      color: white;
      border: 1px solid #0069d9;
    }

    .quick-card-primary:hover {
      background-color: #0069d9;
      color: white;
    }

    .quick-card-secondary {
      background-color: #6c757d;
      color: white;
      border: 1px solid #5a6268;
    }

    .quick-card-secondary:hover {
      background-color: #5a6268;
      color: white;
    }

    .quick-card-master {
      background-color: white;
      color: #495057;
      border: 1px solid #dee2e6;
    }

    .quick-card-master:hover {
      background-color: #f8f9fa;
      color: #212529;
    }
  `]
})
export class DashboardComponent implements OnInit {
  // 상태 관리
  data = signal<DashboardResponse | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);

  // 필터 상태
  fromDate = signal<string>('');
  toDate = signal<string>('');

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    // 기본값: 최근 30일
    const today = new Date();
    const thirtyDaysAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);

    this.fromDate.set(this.formatDate(thirtyDaysAgo));
    this.toDate.set(this.formatDate(today));

    // 초기 로드
    this.loadDashboard();
  }

  /**
   * 대시보드 데이터 로드
   */
  loadDashboard(): void {
    this.loading.set(true);
    this.error.set(null);

    const from = this.fromDate();
    const to = this.toDate();

    this.dashboardService.getDashboardStats(from, to)
      .subscribe({
        next: (response) => {
          this.data.set(response);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(err.error?.message || '대시보드 조회 실패');
          this.loading.set(false);
        }
      });
  }

  /**
   * 조회
   */
  search(): void {
    this.loadDashboard();
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
   * 라인별 비율 계산
   */
  getLinePercentage(count: number): number {
    const totalDefectCount = this.data()?.total_defect_count || 0;
    if (totalDefectCount === 0) return 0;
    return (count / totalDefectCount) * 100;
  }

  /**
   * 코드별 비율 계산
   */
  getCodePercentage(count: number): number {
    const totalDefectCount = this.data()?.total_defect_count || 0;
    if (totalDefectCount === 0) return 0;
    return (count / totalDefectCount) * 100;
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
