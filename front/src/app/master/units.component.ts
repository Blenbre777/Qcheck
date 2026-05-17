import { Component } from '@angular/core';
import { UnitManageComponent } from '../components/unit-manage/unit-manage.component';

@Component({
  selector: 'app-units',
  standalone: true,
  imports: [UnitManageComponent],
  template: '<app-unit-manage></app-unit-manage>'
})
export class UnitsComponent {}
