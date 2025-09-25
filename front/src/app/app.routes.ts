import { Routes } from '@angular/router';

/**
 * Angular 라우팅 설정 배열
 *
 * 목적: Qcheck 애플리케이션의 페이지 네비게이션 경로들을 정의
 * 사용 위치: app.config.ts의 provideRouter() 함수에서 참조
 *
 * 현재 설정된 라우트:
 * 1. 루트 경로 ('') - 로그인 페이지로 자동 리다이렉트
 * 2. 로그인 경로 ('/login') - 로그인 컴포넌트 지연 로딩
 *
 * 라우팅 특징:
 * - Lazy Loading: 필요할 때만 컴포넌트를 로드하여 초기 번들 크기 최적화
 * - pathMatch: 'full' - 정확히 빈 경로일 때만 리다이렉트 실행
 * - import().then() 패턴으로 동적 가져오기 구현
 *
 * 향후 확장 계획:
 * - /dashboard: 메인 대시보드 페이지
 * - /profile: 사용자 프로필 페이지
 * - /settings: 설정 페이지
 *
 * 주의사항:
 * - 라우트 가드나 인증 체크 로직 추가 고려 필요
 * - SSR 환경에서도 정상 작동 확인됨
 */
export const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./login.component').then(c => c.LoginComponent)
  }
];
