import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { DefectService, DefectResponse, DefectCreateRequest, DefectUpdateRequest } from '../services/defect.service';
import { LineService, LineResponse } from '../services/line.service';
import { PartService, PartResponse } from '../services/part.service';
import { UnitService, UnitResponse } from '../services/unit.service';
import { DefectCodeService, DefectCodeResponse } from '../services/defect-code.service';

/**
 * 불량 등록/수정 폼 컴포넌트
 *
 * 책임:
 * - 불량 신규 등록 (/defects/add)
 * - 불량 수정 (/defects/:seq/edit)
 * - 마스터 데이터 드롭다운 로드 (Line, Part, Unit, DefectCode)
 * - 파일 첨부 (이미지 크기/확장자 검증)
 * - 수정 시 기존 데이터 로드 및 이미지 표시
 * - 폼 유효성 검증
 *
 * 상태 관리:
 * - Signal 기반 상태 (isLoading, errorMessage, isEditMode)
 * - Computed signal로 파일 미리보기 관리
 */
@Component({
  selector: 'app-defect-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="defect-form-container">
      <div class="page-header">
        <h2>{{ isEditMode() ? '불량 수정' : '불량 등록' }}</h2>
        <a routerLink="/dashboard" class="btn btn-back">← 대시보드</a>
      </div>

      <!-- 에러 메시지 -->
      <div *ngIf="errorMessage()" class="alert alert-danger">
        {{ errorMessage() }}
      </div>

      <!-- 로딩 상태 -->
      <div *ngIf="isLoading()" class="spinner">로딩 중...</div>

      <form [formGroup]="form" (ngSubmit)="submitForm()" *ngIf="!isLoading()" class="form-section">
        <!-- 발생 날짜 -->
        <div class="form-group">
          <label for="occurDate">발생 날짜 <span class="required">*</span></label>
          <input
            id="occurDate"
            type="date"
            formControlName="occurDate"
            class="form-control"
          />
          <div *ngIf="form.get('occurDate')?.invalid && form.get('occurDate')?.touched" class="text-danger">
            발생 날짜는 필수입니다
          </div>
        </div>

        <!-- 공정 단위 -->
        <div class="form-group">
          <label for="unitSeq">공정 단위 <span class="required">*</span></label>
          <select id="unitSeq" formControlName="unitSeq" class="form-control">
            <option value="">공정 단위를 선택하세요</option>
            <option *ngFor="let unit of units()" [value]="unit.unitSeq">
              {{ unit.unitNm }}
            </option>
          </select>
          <div *ngIf="form.get('unitSeq')?.invalid && form.get('unitSeq')?.touched" class="text-danger">
            공정 단위는 필수입니다
          </div>
        </div>

        <!-- 라인 -->
        <div class="form-group">
          <label for="lineSeq">라인 <span class="required">*</span></label>
          <select id="lineSeq" formControlName="lineSeq" class="form-control">
            <option value="">라인을 선택하세요</option>
            <option *ngFor="let line of lines()" [value]="line.lineSeq">
              {{ line.lineNm }}
            </option>
          </select>
          <div *ngIf="form.get('lineSeq')?.invalid && form.get('lineSeq')?.touched" class="text-danger">
            라인은 필수입니다
          </div>
        </div>

        <!-- 부품 -->
        <div class="form-group">
          <label for="partSeq">부품 <span class="required">*</span></label>
          <select id="partSeq" formControlName="partSeq" class="form-control">
            <option value="">부품을 선택하세요</option>
            <option *ngFor="let part of parts()" [value]="part.partSeq">
              {{ part.partNm }}
            </option>
          </select>
          <div *ngIf="form.get('partSeq')?.invalid && form.get('partSeq')?.touched" class="text-danger">
            부품은 필수입니다
          </div>
        </div>

        <!-- 불량 코드 -->
        <div class="form-group">
          <label for="defectCodeSeq">불량 코드 <span class="required">*</span></label>
          <select id="defectCodeSeq" formControlName="defectCodeSeq" class="form-control">
            <option value="">불량 코드를 선택하세요</option>
            <option *ngFor="let code of defectCodes()" [value]="code.defectCodeSeq">
              {{ code.codeNm }}
            </option>
          </select>
          <div *ngIf="form.get('defectCodeSeq')?.invalid && form.get('defectCodeSeq')?.touched" class="text-danger">
            불량 코드는 필수입니다
          </div>
        </div>

        <!-- 수량 -->
        <div class="form-group">
          <label for="quantity">수량 <span class="required">*</span></label>
          <input
            id="quantity"
            type="number"
            formControlName="quantity"
            class="form-control"
            min="1"
            placeholder="1"
          />
          <div *ngIf="form.get('quantity')?.invalid && form.get('quantity')?.touched" class="text-danger">
            수량은 1 이상 입력해야 합니다
          </div>
        </div>

        <!-- 위치 -->
        <div class="form-group">
          <label for="position">위치</label>
          <input
            id="position"
            type="text"
            formControlName="position"
            class="form-control"
            placeholder="A1, B2 등"
          />
        </div>

        <!-- 비고 -->
        <div class="form-group">
          <label for="remarks">비고</label>
          <textarea
            id="remarks"
            formControlName="remarks"
            class="form-control"
            rows="3"
            placeholder="추가 정보를 입력하세요"
          ></textarea>
        </div>

        <!-- 파일 첨부 -->
        <div class="form-group">
          <label for="file">이미지 파일 (jpg/png/gif/bmp/jpeg, 최대 10MB)</label>
          <input
            id="file"
            type="file"
            (change)="onFileSelected($event)"
            accept=".jpg,.jpeg,.png,.gif,.bmp"
            class="form-control"
          />
          <div *ngIf="fileError()" class="text-danger">
            {{ fileError() }}
          </div>
          <div *ngIf="selectedFile()" class="file-preview">
            파일: {{ selectedFile()?.name }} ({{ (selectedFile()!.size / 1024 / 1024).toFixed(2) }} MB)
          </div>
        </div>

        <!-- 기존 이미지 표시 (수정 모드) -->
        <div *ngIf="isEditMode() && existingImageUrl" class="form-group">
          <label>현재 이미지</label>
          <div class="existing-image">
            <img [src]="existingImageUrl" alt="기존 이미지" />
          </div>
          <p class="text-muted">새로운 파일을 선택하면 기존 이미지가 대체됩니다</p>
        </div>

        <!-- 버튼 -->
        <div class="form-buttons">
          <button
            type="submit"
            [disabled]="form.invalid || isLoading() || !!fileError()"
            class="btn btn-primary"
          >
            {{ isEditMode() ? '수정' : '등록' }}
          </button>

          <button
            type="button"
            (click)="cancel()"
            [disabled]="isLoading()"
            class="btn btn-secondary"
          >
            취소
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .defect-form-container {
      padding: 20px;
      max-width: 600px;
      margin: 0 auto;
    }

    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20px;
    }

    .page-header h2 {
      margin: 0;
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

    .form-section {
      background-color: #f9f9f9;
      padding: 20px;
      border-radius: 4px;
    }

    .form-group {
      margin-bottom: 20px;
      display: flex;
      flex-direction: column;
    }

    .form-group label {
      font-weight: 500;
      margin-bottom: 5px;
    }

    .required {
      color: red;
    }

    .form-control {
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-size: 1em;
      font-family: inherit;
    }

    .form-control:focus {
      outline: none;
      border-color: #007bff;
      box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
    }

    .text-danger {
      color: #dc3545;
      font-size: 0.9em;
      margin-top: 5px;
    }

    .text-muted {
      color: #999;
      font-size: 0.9em;
    }

    .file-preview {
      background-color: #e8f4f8;
      padding: 10px;
      border-radius: 4px;
      margin-top: 5px;
      font-size: 0.9em;
      color: #0066cc;
    }

    .existing-image {
      margin-top: 10px;
      max-width: 200px;
    }

    .existing-image img {
      max-width: 100%;
      height: auto;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .form-buttons {
      display: flex;
      gap: 10px;
      margin-top: 30px;
    }

    .btn {
      padding: 10px 20px;
      border: 1px solid #ddd;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1em;
      font-weight: 500;
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
      flex: 1;
    }

    .btn-primary:hover:not(:disabled) {
      background-color: #0056b3;
    }

    .btn-secondary {
      background-color: #6c757d;
      color: white;
      border-color: #6c757d;
      flex: 1;
    }

    .btn-secondary:hover:not(:disabled) {
      background-color: #545b62;
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

    .spinner {
      text-align: center;
      padding: 40px;
      color: #666;
    }
  `]
})
export class DefectFormComponent implements OnInit {
  // 상태 관리
  isLoading = signal(false);
  errorMessage = signal<string | null>(null);
  isEditMode = signal(false);
  defectSeq = signal<number | null>(null);

  // 마스터 데이터
  lines = signal<LineResponse[]>([]);
  parts = signal<PartResponse[]>([]);
  units = signal<UnitResponse[]>([]);
  defectCodes = signal<DefectCodeResponse[]>([]);

  // 파일 첨부
  selectedFile = signal<File | null>(null);
  fileError = signal<string | null>(null);
  existingImageUrl: string | null = null;

  // 파일 제한
  private readonly MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private readonly ALLOWED_EXTENSIONS = ['jpg', 'jpeg', 'png', 'gif', 'bmp'];

  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private defectService: DefectService,
    private lineService: LineService,
    private partService: PartService,
    private unitService: UnitService,
    private defectCodeService: DefectCodeService
  ) {
    this.form = this.fb.group({
      occurDate: ['', Validators.required],
      unitSeq: ['', Validators.required],
      lineSeq: ['', Validators.required],
      partSeq: ['', Validators.required],
      defectCodeSeq: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      position: [''],
      remarks: ['']
    });
  }

  ngOnInit(): void {
    // 마스터 데이터 로드
    this.loadMasterData();

    // 라우트 파라미터 확인 (수정 모드 판단)
    this.route.paramMap.subscribe(params => {
      const seq = params.get('seq');
      if (seq) {
        this.isEditMode.set(true);
        this.defectSeq.set(Number(seq));
        this.loadDefect(Number(seq));
      }
    });
  }

  /**
   * 마스터 데이터 로드
   */
  private loadMasterData(): void {
    this.isLoading.set(true);

    Promise.all([
      this.loadLines(),
      this.loadParts(),
      this.loadUnits(),
      this.loadDefectCodes()
    ]).finally(() => {
      this.isLoading.set(false);
    });
  }

  /**
   * 라인 로드
   */
  private loadLines(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.lineService.getLines().subscribe({
        next: (data) => {
          this.lines.set(data);
          resolve();
        },
        error: (err) => {
          this.errorMessage.set('라인 로드 실패');
          reject(err);
        }
      });
    });
  }

  /**
   * 부품 로드
   */
  private loadParts(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.partService.getParts().subscribe({
        next: (data) => {
          this.parts.set(data);
          resolve();
        },
        error: (err) => {
          this.errorMessage.set('부품 로드 실패');
          reject(err);
        }
      });
    });
  }

  /**
   * 공정 단위 로드
   */
  private loadUnits(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.unitService.getUnits().subscribe({
        next: (data) => {
          this.units.set(data);
          resolve();
        },
        error: (err) => {
          this.errorMessage.set('공정 단위 로드 실패');
          reject(err);
        }
      });
    });
  }

  /**
   * 불량 코드 로드
   */
  private loadDefectCodes(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.defectCodeService.getDefectCodes().subscribe({
        next: (data) => {
          this.defectCodes.set(data);
          resolve();
        },
        error: (err) => {
          this.errorMessage.set('불량 코드 로드 실패');
          reject(err);
        }
      });
    });
  }

  /**
   * 불량 상세 조회 (수정 모드)
   */
  private loadDefect(seq: number): void {
    this.isLoading.set(true);
    this.defectService.getDefect(seq).subscribe({
      next: (defect) => {
        // 폼 값 설정
        this.form.patchValue({
          occurDate: defect.occurDate,
          quantity: defect.quantity,
          position: defect.position || '',
          remarks: defect.remarks || ''
        });

        // 드롭다운 값 설정 (마스터 데이터 로드 후)
        setTimeout(() => {
          this.form.patchValue({
            unitSeq: this.findIdByName(this.units(), defect.unitNm, 'unitNm'),
            lineSeq: this.findIdByName(this.lines(), defect.lineNm, 'lineNm'),
            partSeq: this.findIdByName(this.parts(), defect.partNm, 'partNm'),
            defectCodeSeq: this.findIdByName(this.defectCodes(), defect.codeNm, 'codeNm')
          });
        }, 100);

        // 이미지 URL 설정
        if (defect.hasImage) {
          this.existingImageUrl = this.defectService.getImageUrl(seq);
        }

        this.isLoading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || '불량 조회 실패');
        this.isLoading.set(false);
      }
    });
  }

  /**
   * 이름으로 ID 찾기 (드롭다운 값 매핑용)
   */
  private findIdByName(items: any[], name: string, nameField: string): number | null {
    if (!items || items.length === 0) return null;
    const item = items.find(i => i[nameField] === name);
    if (!item) return null;

    // 첫 번째 Seq 필드 찾기
    const seqKey = Object.keys(item).find(k => k.endsWith('Seq'));
    return seqKey ? item[seqKey] : null;
  }

  /**
   * 파일 선택
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    this.fileError.set(null);
    this.selectedFile.set(null);

    if (!file) return;

    // 파일 크기 검증
    if (file.size > this.MAX_FILE_SIZE) {
      this.fileError.set('파일 크기가 10MB를 초과합니다');
      input.value = '';
      return;
    }

    // 파일 확장자 검증
    const ext = file.name.split('.').pop()?.toLowerCase();
    if (!ext || !this.ALLOWED_EXTENSIONS.includes(ext)) {
      this.fileError.set('jpg, jpeg, png, gif, bmp 파일만 업로드할 수 있습니다');
      input.value = '';
      return;
    }

    this.selectedFile.set(file);
  }

  /**
   * 폼 제출
   */
  submitForm(): void {
    if (this.form.invalid) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const formData = new FormData();

    // defect JSON
    const defectData: DefectCreateRequest = {
      lineSeq: this.form.value.lineSeq,
      partSeq: this.form.value.partSeq,
      unitSeq: this.form.value.unitSeq,
      defectCodeSeq: this.form.value.defectCodeSeq,
      quantity: this.form.value.quantity,
      position: this.form.value.position || undefined,
      remarks: this.form.value.remarks || undefined,
      occurDate: this.form.value.occurDate
    };

    formData.append('defect', JSON.stringify(defectData));

    // 파일 (선택사항)
    if (this.selectedFile()) {
      formData.append('file', this.selectedFile()!);
    }

    // API 호출
    if (this.isEditMode() && this.defectSeq()) {
      this.defectService.updateDefect(this.defectSeq()!, formData).subscribe({
        next: () => {
          this.router.navigate(['/defects']);
        },
        error: (err) => {
          this.errorMessage.set(err.error?.message || '불량 수정 실패');
          this.isLoading.set(false);
        }
      });
    } else {
      this.defectService.createDefect(formData).subscribe({
        next: () => {
          this.router.navigate(['/defects']);
        },
        error: (err) => {
          this.errorMessage.set(err.error?.message || '불량 등록 실패');
          this.isLoading.set(false);
        }
      });
    }
  }

  /**
   * 취소
   */
  cancel(): void {
    this.router.navigate(['/defects']);
  }
}
