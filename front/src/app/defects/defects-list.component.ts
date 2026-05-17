import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-defects-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="defects-list-container">
      <h2>불량 목록</h2>
      <p>불량 목록 조회 기능 (Phase 3에서 구현)</p>
    </div>
  `,
  styles: [`
    .defects-list-container {
      padding: 20px;
    }
  `]
})
export class DefectsListComponent {}
