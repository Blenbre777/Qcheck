import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    this.registerForm = this.fb.group({
      companyNm: ['', [Validators.required, Validators.maxLength(100)]],
      empId:     ['', [Validators.required, Validators.maxLength(50)]],
      empNm:     ['', [Validators.required, Validators.maxLength(100)]],
      empEmail:  ['', [Validators.required, Validators.email]],
      empPw:     ['', [Validators.required, Validators.minLength(6)]],
      empPwConfirm: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  private passwordMatchValidator(group: AbstractControl) {
    const pw = group.get('empPw')?.value;
    const confirm = group.get('empPwConfirm')?.value;
    return pw === confirm ? null : { passwordMismatch: true };
  }

  get companyNm() { return this.registerForm.get('companyNm'); }
  get empId()     { return this.registerForm.get('empId'); }
  get empNm()     { return this.registerForm.get('empNm'); }
  get empEmail()  { return this.registerForm.get('empEmail'); }
  get empPw()     { return this.registerForm.get('empPw'); }
  get empPwConfirm() { return this.registerForm.get('empPwConfirm'); }

  onSubmit() {
    if (this.registerForm.invalid) {
      Object.keys(this.registerForm.controls).forEach(key => {
        this.registerForm.get(key)?.markAsTouched();
      });
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');
    const { companyNm, empId, empNm, empEmail, empPw } = this.registerForm.value;

    this.authService.register(companyNm, empId, empNm, empEmail, empPw).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set('회원가입이 완료되었습니다. 로그인해주세요.');
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set(err.error?.message || '회원가입 중 오류가 발생했습니다.');
      }
    });
  }

  getError(fieldName: string): string {
    const field = this.registerForm.get(fieldName);
    if (!field?.errors || !field.touched) return '';
    if (field.errors['required']) return '필수 입력 항목입니다.';
    if (field.errors['email']) return '올바른 이메일 형식이 아닙니다.';
    if (field.errors['minlength']) return `최소 ${field.errors['minlength'].requiredLength}자 이상 입력하세요.`;
    if (field.errors['maxlength']) return `최대 ${field.errors['maxlength'].requiredLength}자까지 입력 가능합니다.`;
    return '';
  }
}
