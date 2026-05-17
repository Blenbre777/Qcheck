import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-defects-register',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="defects-register-container">
      <h2>불량 등록</h2>
      <p>불량 등록 기능 (Phase 3에서 구현)</p>
    </div>
  `,
  styles: [`
    .defects-register-container {
      padding: 20px;
    }
  `]
})
export class DefectsRegisterComponent {}
