import { BootstrapContext, bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { config } from './app/app.config.server';

/**
 * 서버 사이드 부트스트랩 함수
 *
 * 목적: SSR(Server-Side Rendering) 환경에서 Angular 애플리케이션을 초기화
 * 실행 환경: Node.js 서버 (Express.js)
 * 호출 위치: server.ts의 Angular Node App Engine에서 호출
 *
 * 매개변수:
 * - context: BootstrapContext
 *   서버 렌더링에 필요한 컨텍스트 정보 (요청 정보, 문서 정보 등)
 *
 * 반환값: Promise<ApplicationRef>
 *   부트스트랩된 Angular 애플리케이션 인스턴스
 *
 * 부트스트랩 과정:
 * 1. 서버 사이드 전용 설정(config) 로드
 * 2. 루트 컴포넌트(App)와 설정을 결합
 * 3. 서버 렌더링 컨텍스트 적용
 * 4. HTML 문자열 생성 및 반환 준비
 *
 * 사용 기술:
 * - Angular Universal: SSR 지원
 * - bootstrapApplication: Standalone 컴포넌트 부트스트랩
 * - app.config.server: 서버 사이드 전용 프로바이더 설정
 *
 * 주의사항:
 * - 브라우저 API는 서버에서 사용 불가 (window, document 등)
 * - 서버 렌더링 결과가 클라이언트 하이드레이션과 일치해야 함
 * - SEO와 초기 페이지 로딩 성능 향상에 기여
 */
const bootstrap = (context: BootstrapContext) =>
    bootstrapApplication(App, config, context);

export default bootstrap;
