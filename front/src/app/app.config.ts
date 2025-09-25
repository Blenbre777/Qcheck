import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

/**
 * Angular 애플리케이션 전역 설정 객체
 *
 * 목적: Angular 20 애플리케이션의 핵심 서비스와 기능들을 프로바이더 형태로 설정
 * 사용 위치: main.ts에서 bootstrapApplication 함수의 두 번째 인자로 전달
 *
 * 포함된 프로바이더들:
 * 1. provideBrowserGlobalErrorListeners(): 전역 에러 처리 리스너 등록
 * 2. provideZoneChangeDetection(): Zone.js 기반 변화 감지 최적화
 * 3. provideRouter(): 라우팅 시스템 활성화
 * 4. provideClientHydration(): SSR 클라이언트 하이드레이션 기능
 *
 * 설정 특징:
 * - eventCoalescing: true - 이벤트 병합을 통한 성능 최적화
 * - withEventReplay: SSR에서 클라이언트로 전환 시 이벤트 재생 기능
 *
 * 주의사항:
 * - 서버 사이드 설정은 app.config.server.ts에서 별도 관리
 * - 프로바이더 순서가 중요할 수 있음 (의존성 주입 순서)
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes), provideClientHydration(withEventReplay())
  ]
};
