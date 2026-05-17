import { Component } from '@angular/core';
import { PartManageComponent } from '../components/part-manage/part-manage.component';

@Component({
  selector: 'app-parts',
  standalone: true,
  imports: [PartManageComponent],
  template: '<app-part-manage></app-part-manage>'
})
export class PartsComponent {}
