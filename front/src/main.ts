import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

/**
 * 클라이언트 사이드 Angular 애플리케이션 부트스트랩
 *
 * 목적: 브라우저 환경에서 Angular 애플리케이션을 초기화하고 실행
 * 실행 환경: 웹 브라우저 (클라이언트 사이드)
 *
 * 부트스트랩 과정:
 * 1. Angular 플랫폼 브라우저 모듈 로드
 * 2. 애플리케이션 설정(appConfig) 적용
 * 3. 루트 컴포넌트(App) 인스턴스 생성 및 DOM에 마운트
 * 4. 애플리케이션 실행 준비 완료
 *
 * 사용된 모듈:
 * - bootstrapApplication: Angular 20의 standalone 컴포넌트용 부트스트랩 함수
 * - appConfig: 클라이언트 사이드 전용 설정 (라우팅, 하이드레이션 등)
 * - App: 메인 애플리케이션 루트 컴포넌트
 *
 * 에러 처리:
 * - 부트스트랩 실패 시 콘솔에 에러 로그 출력
 * - 프로덕션 환경에서는 더 정교한 에러 처리 고려 필요
 *
 * 주의사항:
 * - SSR 환경에서는 main.server.ts가 별도로 실행됨
 * - 하이드레이션 과정에서 서버 렌더링된 DOM과 클라이언트 DOM이 일치해야 함
 */
bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
