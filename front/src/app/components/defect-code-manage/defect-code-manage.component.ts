import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DefectCodeService, DefectCodeResponse, CreateDefectCodeRequest, UpdateDefectCodeRequest } from '../../services/defect-code.service';

@Component({
  selector: 'app-defect-code-manage',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="defect-code-manage-container">
      <h2>불량 코드 관리</h2>

      <!-- 에러 메시지 -->
      <div *ngIf="error()" class="alert alert-danger">
        {{ error() }}
      </div>

      <!-- 로딩 상태 -->
      <div *ngIf="loading()" class="spinner">로딩 중...</div>

      <!-- 생성/수정 폼 -->
      <form [formGroup]="form" (ngSubmit)="submitForm()" class="form-section">
        <div class="form-group">
          <label for="codeNm">불량 코드명</label>
          <input
            id="codeNm"
            type="text"
            formControlName="codeNm"
            class="form-control"
            placeholder="불량 코드명을 입력하세요"
          />
          <div *ngIf="form.get('codeNm')?.invalid && form.get('codeNm')?.touched" class="text-danger">
            불량 코드명은 필수입니다
          </div>
        </div>

        <div class="form-group">
          <label for="description">설명</label>
          <textarea
            id="description"
            formControlName="description"
            class="form-control"
            placeholder="설명을 입력하세요"
            rows="3"
          ></textarea>
        </div>

        <div class="form-group" *ngIf="editingSeq()">
          <label for="useYn">사용 여부</label>
          <select id="useYn" formControlName="useYn" class="form-control">
            <option value="Y">사용</option>
            <option value="N">미사용</option>
          </select>
        </div>

        <button
          type="submit"
          [disabled]="form.invalid || loading()"
          class="btn btn-primary"
        >
          {{ editingSeq() ? '수정' : '생성' }}
        </button>

        <button
          *ngIf="editingSeq()"
          type="button"
          (click)="cancel()"
          class="btn btn-secondary"
        >
          취소
        </button>
      </form>

      <!-- 불량 코드 목록 -->
      <table class="table table-striped">
        <thead>
          <tr>
            <th>불량 코드명</th>
            <th>설명</th>
            <th>사용 여부</th>
            <th>생성자</th>
            <th>생성일시</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let code of defectCodes()">
            <td>{{ code.codeNm }}</td>
            <td>{{ code.description || '-' }}</td>
            <td>{{ code.useYn === 'Y' ? '사용' : '미사용' }}</td>
            <td>{{ code.createdEp }}</td>
            <td>{{ code.createdAt }}</td>
            <td>
              <button
                (click)="edit(code)"
                class="btn btn-sm btn-info"
              >
                수정
              </button>
              <button
                (click)="delete(code)"
                class="btn btn-sm btn-danger"
                [disabled]="loading()"
              >
                삭제
              </button>
            </td>
          </tr>
          <tr *ngIf="defectCodes().length === 0">
            <td colspan="6" class="text-center">불량 코드가 없습니다</td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .defect-code-manage-container {
      padding: 20px;
    }

    h2 {
      margin-bottom: 20px;
    }

    .form-section {
      background: #f9f9f9;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 30px;
    }

    .form-group {
      margin-bottom: 15px;
    }

    .form-group label {
      display: block;
      margin-bottom: 5px;
      font-weight: 500;
    }

    .form-group input,
    .form-group textarea,
    .form-group select {
      width: 100%;
      padding: 8px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 14px;
      font-family: inherit;
    }

    .text-danger {
      color: #dc3545;
      font-size: 12px;
      margin-top: 5px;
    }

    .alert-danger {
      background: #f8d7da;
      border: 1px solid #f5c6cb;
      color: #721c24;
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 20px;
    }

    .btn {
      padding: 8px 16px;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      margin-right: 10px;
    }

    .btn-primary {
      background: #007bff;
      color: white;
    }

    .btn-primary:disabled {
      background: #ccc;
      cursor: not-allowed;
    }

    .btn-secondary {
      background: #6c757d;
      color: white;
    }

    .btn-info {
      background: #17a2b8;
      color: white;
    }

    .btn-danger {
      background: #dc3545;
      color: white;
    }

    .btn-danger:disabled {
      background: #ccc;
      cursor: not-allowed;
    }

    .table {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
    }

    .table thead {
      background: #f0f0f0;
    }

    .table th,
    .table td {
      padding: 12px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    .table tbody tr:hover {
      background: #f9f9f9;
    }

    .spinner {
      text-align: center;
      padding: 20px;
      color: #666;
    }
  `]
})
export class DefectCodeManageComponent implements OnInit {
  defectCodes = signal<DefectCodeResponse[]>([]);
  loading = signal(false);
  error = signal('');
  editingSeq = signal<number | null>(null);

  form: FormGroup;

  constructor(
    private defectCodeService: DefectCodeService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      codeNm: ['', [Validators.required, Validators.minLength(1)]],
      description: [''],
      useYn: ['Y']
    });
  }

  ngOnInit() {
    this.loadDefectCodes();
  }

  loadDefectCodes() {
    this.loading.set(true);
    this.error.set('');

    this.defectCodeService.getDefectCodes().subscribe({
      next: (data) => {
        this.defectCodes.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('불량 코드 목록 조회에 실패했습니다: ' + err.message);
        this.loading.set(false);
      }
    });
  }

  submitForm() {
    if (this.form.invalid) {
      return;
    }

    this.loading.set(true);
    this.error.set('');

    const { codeNm, description, useYn } = this.form.value;

    if (this.editingSeq()) {
      const updateRequest: UpdateDefectCodeRequest = { codeNm, description, useYn };
      this.defectCodeService.updateDefectCode(this.editingSeq()!, updateRequest).subscribe({
        next: () => {
          this.loadDefectCodes();
          this.form.reset({ useYn: 'Y' });
          this.editingSeq.set(null);
        },
        error: (err) => {
          this.error.set('불량 코드 수정에 실패했습니다: ' + err.message);
          this.loading.set(false);
        }
      });
    } else {
      const createRequest: CreateDefectCodeRequest = { codeNm, description };
      this.defectCodeService.createDefectCode(createRequest).subscribe({
        next: () => {
          this.loadDefectCodes();
          this.form.reset({ useYn: 'Y' });
        },
        error: (err) => {
          this.error.set('불량 코드 생성에 실패했습니다: ' + err.message);
          this.loading.set(false);
        }
      });
    }
  }

  edit(code: DefectCodeResponse) {
    this.editingSeq.set(code.defectCodeSeq);
    this.form.patchValue({
      codeNm: code.codeNm,
      description: code.description,
      useYn: code.useYn
    });
  }

  cancel() {
    this.form.reset({ useYn: 'Y' });
    this.editingSeq.set(null);
    this.error.set('');
  }

  delete(code: DefectCodeResponse) {
    if (confirm(`'${code.codeNm}' 불량 코드를 삭제하시겠습니까?`)) {
      this.loading.set(true);
      this.defectCodeService.deleteDefectCode(code.defectCodeSeq).subscribe({
        next: () => {
          this.loadDefectCodes();
        },
        error: (err) => {
          this.error.set('불량 코드 삭제에 실패했습니다: ' + err.message);
          this.loading.set(false);
        }
      });
    }
  }
}
