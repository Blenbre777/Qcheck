import { Component } from '@angular/core';
import { LineManageComponent } from '../components/line-manage/line-manage.component';

@Component({
  selector: 'app-lines',
  standalone: true,
  imports: [LineManageComponent],
  template: '<app-line-manage></app-line-manage>'
})
export class LinesComponent {}
