import { Component } from '@angular/core';
import { DefectCodeManageComponent } from '../components/defect-code-manage/defect-code-manage.component';

@Component({
  selector: 'app-defect-codes',
  standalone: true,
  imports: [DefectCodeManageComponent],
  template: '<app-defect-code-manage></app-defect-code-manage>'
})
export class DefectCodesComponent {}
