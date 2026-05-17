import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * 인증 라우트 가드
 *
 * 목적:
 * - 보호된 라우트(대시보드, 마스터 데이터 등)에 접근할 때 사용자 인증 상태 확인
 * - 미인증 사용자가 보호된 라우트에 접근하면 로그인 페이지로 리다이렉트
 *
 * 동작 원리:
 * 1. 라우트 네비게이션이 발생할 때 가드가 실행된다
 * 2. AuthService의 isAuthenticated 시그널을 확인
 * 3. 인증되지 않았으면 /login으로 리다이렉트
 * 4. 인증되었으면 라우트 이동 허용
 *
 * 사용 위치:
 * - app.routes.ts의 canActivate 배열에 추가
 * 예시:
 *   {
 *     path: 'dashboard',
 *     canActivate: [AuthGuard],
 *     component: DashboardComponent
 *   }
 *
 * 주의사항:
 * - 로그인 페이지('/login')에는 가드를 추가하지 않음
 * - 인증 상태는 localStorage의 accessToken 존재 여부로 판단
 * - 토큰이 만료되었으면 auto-refresh 메커니즘이 갱신을 시도
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /**
   * 라우트 활성화 가능 여부 판단
   *
   * @param route 현재 라우트 스냅샷
   * @param state 라우터 상태
   * @returns true이면 라우트 진행, false면 차단 및 리다이렉트
   */
  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    // 현재 인증 상태 확인
    const isAuthenticated = this.authService.isAuthenticated();

    if (isAuthenticated) {
      // 인증됨: 라우트 진행 허용
      return true;
    } else {
      // 미인증: 로그인 페이지로 리다이렉트
      this.router.navigate(['/login'], {
        queryParams: { returnUrl: state.url }
      });
      return false;
    }
  }
}
