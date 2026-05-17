import { Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';

/**
 * Angular 라우팅 설정 배열
 *
 * 목적: Qcheck 애플리케이션의 페이지 네비게이션 경로들을 정의
 * 사용 위치: app.config.ts의 provideRouter() 함수에서 참조
 *
 * 라우트 구조:
 * 1. 루트 경로 ('') - 로그인 페이지로 자동 리다이렉트
 * 2. 로그인 경로 ('/login') - 로그인 컴포넌트 (공개)
 * 3. 대시보드 ('/dashboard') - 인증 필수 (AuthGuard)
 * 4. 마스터 데이터 ('/master/**') - 인증 필수 (AuthGuard)
 * 5. 불량 관리 ('/defects/**') - 인증 필수 (AuthGuard)
 *
 * 라우팅 특징:
 * - Lazy Loading: 필요할 때만 컴포넌트를 로드하여 초기 번들 크기 최적화
 * - pathMatch: 'full' - 정확히 빈 경로일 때만 리다이렉트 실행
 * - import().then() 패턴으로 동적 가져오기 구현
 * - AuthGuard: 인증되지 않은 사용자는 /login으로 리다이렉트
 *
 * 보안 정책:
 * - 공개 라우트: /login, /reset-password 등
 * - 보호된 라우트: /dashboard, /master/*, /defects/* (AuthGuard 필요)
 * - 미인증 접근: /login으로 자동 리다이렉트
 *
 * 주의사항:
 * - AuthGuard는 localStorage의 accessToken 존재 여부를 확인
 * - 토큰 자동 갱신은 AuthService에서 처리
 * - 401 응답은 AuthService에서 logout 처리
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
  },
  // {
  //   path: 'dashboard',
  //   canActivate: [AuthGuard],
  //   loadComponent: () => import('./dashboard/dashboard.component').then(c => c.DashboardComponent)
  // },
  {
    path: 'master',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'lines',
        loadComponent: () => import('./master/lines.component').then(c => c.LinesComponent)
      },
      {
        path: 'parts',
        loadComponent: () => import('./master/parts.component').then(c => c.PartsComponent)
      },
      {
        path: 'units',
        loadComponent: () => import('./master/units.component').then(c => c.UnitsComponent)
      },
      {
        path: 'defect-codes',
        loadComponent: () => import('./master/defect-codes.component').then(c => c.DefectCodesComponent)
      }
    ]
  },
  {
    path: 'defects',
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./defects/defect-list.component').then(c => c.DefectListComponent)
      },
      {
        path: 'add',
        loadComponent: () => import('./defects/defect-form.component').then(c => c.DefectFormComponent)
      },
      {
        path: ':seq/edit',
        loadComponent: () => import('./defects/defect-form.component').then(c => c.DefectFormComponent)
      }
    ]
  }
];
