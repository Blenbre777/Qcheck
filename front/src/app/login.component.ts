import { Component, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

/**
 * 로그인 폼 데이터 인터페이스
 *
 * 목적: 로그인 폼에서 사용되는 데이터 구조를 정의
 * 사용처: onSubmit 메서드에서 폼 데이터 타입 검증
 *
 * 속성:
 * - email: 사용자 이메일 주소 (string, 필수)
 * - password: 사용자 비밀번호 (string, 필수)
 */
interface LoginForm {
  email: string;
  password: string;
}

/**
 * 로그인 컴포넌트 - 사용자 인증을 위한 로그인 페이지
 *
 * 목적: 사용자의 이메일과 비밀번호를 입력받아 로그인 처리
 * 주요 기능:
 * - 반응형 폼을 통한 사용자 입력 처리
 * - 입력값 유효성 검증 (이메일 형식, 비밀번호 최소 길이)
 * - 로딩 상태 관리 및 에러 메시지 표시
 * - 비밀번호 찾기 기능 (향후 구현 예정)
 *
 * 사용 기술:
 * - Angular 20 standalone 컴포넌트
 * - Reactive Forms (FormBuilder, Validators)
 * - Angular Signals (상태 관리)
 *
 * 템플릿: login.component.html
 * 스타일: login.component.scss
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  /**
   * 로그인 폼 그룹
   *
   * 자료형: FormGroup
   * 포함 필드:
   * - email: 이메일 주소 (필수, 이메일 형식 검증)
   * - password: 비밀번호 (필수, 최소 6자 이상)
   *
   * 사용 목적: 사용자 입력값 관리 및 유효성 검증
   */
  loginForm: FormGroup;

  /**
   * 로딩 상태 시그널
   *
   * 자료형: WritableSignal<boolean>
   * 기본값: false
   *
   * 사용 목적:
   * - API 호출 중 로딩 스피너 표시
   * - 중복 제출 방지
   */
  isLoading = signal(false);

  /**
   * 에러 메시지 시그널
   *
   * 자료형: WritableSignal<string>
   * 기본값: 빈 문자열
   *
   * 사용 목적:
   * - 로그인 실패 시 에러 메시지 표시
   * - 네트워크 오류 등 예외 상황 안내
   */
  errorMessage = signal('');

  /**
   * 로그인 컴포넌트 생성자
   *
   * 의존성 주입:
   * - FormBuilder: 반응형 폼 생성을 위한 Angular 서비스
   * - Router: 로그인 성공 후 페이지 이동을 위한 라우팅 서비스
   *
   * 초기화 작업:
   * - 로그인 폼 그룹 생성 및 유효성 검증 규칙 설정
   */
  constructor(
    private fb: FormBuilder,
    private router: Router
  ) {
    // 로그인 폼 초기화 - 이메일과 비밀번호 필드 생성
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  /**
   * 이메일 폼 컨트롤 접근자
   *
   * 반환값: AbstractControl | null
   * 목적: 템플릿에서 이메일 필드의 상태와 값에 쉽게 접근
   *
   * 사용 예시:
   * - 유효성 검증 상태 확인 (this.email?.valid)
   * - 에러 메시지 표시 (this.email?.errors)
   */
  get email() {
    return this.loginForm.get('email');
  }

  /**
   * 비밀번호 폼 컨트롤 접근자
   *
   * 반환값: AbstractControl | null
   * 목적: 템플릿에서 비밀번호 필드의 상태와 값에 쉽게 접근
   *
   * 사용 예시:
   * - 유효성 검증 상태 확인 (this.password?.valid)
   * - 에러 메시지 표시 (this.password?.errors)
   */
  get password() {
    return this.loginForm.get('password');
  }

  /**
   * 로그인 폼 제출 처리 메서드
   *
   * 목적: 사용자가 로그인 버튼을 클릭했을 때 호출되는 핸들러
   * 처리 흐름:
   * 1. 폼 유효성 검증
   * 2. 로딩 상태 활성화
   * 3. API 호출 (현재는 시뮬레이션)
   * 4. 성공 시 대시보드로 이동 (향후 구현)
   * 5. 실패 시 에러 메시지 표시
   *
   * 입력값: 없음 (폼 데이터는 this.loginForm.value에서 추출)
   * 반환값: void
   *
   * 주의사항:
   * - 현재는 실제 API 호출 대신 setTimeout으로 시뮬레이션
   * - 실제 구현 시 HTTP 클라이언트와 인증 서비스 필요
   */
  onSubmit() {
    if (this.loginForm.valid) {
      // 로딩 상태 활성화 및 에러 메시지 초기화
      this.isLoading.set(true);
      this.errorMessage.set('');

      // 폼 데이터 추출 (타입 안전성 보장)
      const loginData: LoginForm = this.loginForm.value;

      // TODO: Implement actual login API call
      console.log('Login attempt:', loginData);

      // API 호출 시뮬레이션 (1초 지연)
      setTimeout(() => {
        this.isLoading.set(false);
        // For now, just show success (implement actual auth later)
        console.log('Login successful');
        // this.router.navigate(['/dashboard']);
      }, 1000);
    } else {
      // 폼이 유효하지 않은 경우 모든 필드를 touched 상태로 변경하여 에러 메시지 표시
      this.markFormGroupTouched();
    }
  }

  /**
   * 비밀번호 찾기 버튼 클릭 처리 메서드
   *
   * 목적: 사용자가 비밀번호를 분실했을 때 재설정 프로세스 시작
   * 향후 구현 예정 기능:
   * - 이메일 입력 모달 표시
   * - 비밀번호 재설정 이메일 발송 API 호출
   * - 성공/실패 메시지 표시
   *
   * 입력값: 없음
   * 반환값: void
   *
   * 주의사항: 현재는 콘솔 로그만 출력 (개발 중)
   */
  onForgotPassword() {
    // TODO: Implement forgot password functionality
    console.log('Forgot password clicked');
  }

  /**
   * 폼 그룹의 모든 컨트롤을 touched 상태로 변경하는 헬퍼 메서드
   *
   * 목적: 사용자가 유효하지 않은 폼을 제출했을 때 모든 필드의 에러 메시지를 표시
   * 동작 방식:
   * 1. 폼 그룹의 모든 컨트롤 키를 순회
   * 2. 각 컨트롤을 touched 상태로 변경
   * 3. Angular의 유효성 검증 시스템이 에러 메시지 표시
   *
   * 입력값: 없음 (this.loginForm을 직접 참조)
   * 반환값: void
   *
   * 사용 시점: onSubmit 메서드에서 폼이 유효하지 않을 때 호출
   */
  private markFormGroupTouched() {
    Object.keys(this.loginForm.controls).forEach(key => {
      const control = this.loginForm.get(key);
      control?.markAsTouched();
    });
  }

  /**
   * 특정 필드의 에러 메시지를 반환하는 메서드
   *
   * 목적: 폼 필드별로 적절한 에러 메시지를 생성하여 사용자에게 표시
   * 지원하는 에러 타입:
   * - required: 필수 입력 필드가 비어있을 때
   * - email: 이메일 형식이 올바르지 않을 때
   * - minlength: 비밀번호가 최소 길이 미만일 때
   *
   * 입력값:
   * - fieldName: 에러 메시지를 확인할 필드명 (string)
   *   가능한 값: 'email', 'password'
   *
   * 반환값: string
   * - 에러가 있고 필드가 touched 상태인 경우: 해당 에러 메시지
   * - 에러가 없거나 필드가 untouched 상태인 경우: 빈 문자열
   *
   * 사용 예시:
   * - 템플릿에서 {{ getFieldErrorMessage('email') }}
   * - 조건부 에러 메시지 표시에 활용
   *
   * 주의사항: 필드가 touched 상태여야만 에러 메시지를 반환
   */
  getFieldErrorMessage(fieldName: string): string {
    const field = this.loginForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} is required`;
      }
      if (field.errors['email']) {
        return 'Please enter a valid email address';
      }
      if (field.errors['minlength']) {
        return 'Password must be at least 6 characters long';
      }
    }
    return '';
  }
}