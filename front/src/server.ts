import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';

/**
 * 브라우저용 빌드 파일이 위치한 디렉터리 경로
 *
 * 목적: 정적 파일(HTML, CSS, JS, 이미지 등) 서빙을 위한 경로 설정
 * 경로 구성: 현재 파일 위치 기준 '../browser' 상대 경로
 * 파일 내용: Angular CLI 빌드 결과물 (npm run build 실행 후 생성)
 */
const browserDistFolder = join(import.meta.dirname, '../browser');

/**
 * Express 애플리케이션 인스턴스
 *
 * 목적: HTTP 서버 생성 및 라우팅 처리
 * 사용 미들웨어:
 * - 정적 파일 서빙 미들웨어
 * - Angular SSR 처리 미들웨어
 */
const app = express();

/**
 * Angular SSR 엔진 인스턴스
 *
 * 목적: Angular 애플리케이션의 서버 사이드 렌더링 처리
 * 기능:
 * - main.server.ts의 bootstrap 함수 호출
 * - HTML 응답 생성 및 반환
 * - 클라이언트 하이드레이션을 위한 상태 직렬화
 */
const angularApp = new AngularNodeAppEngine();

/**
 * Example Express Rest API endpoints can be defined here.
 * Uncomment and define endpoints as necessary.
 *
 * Example:
 * ```ts
 * app.get('/api/{*splat}', (req, res) => {
 *   // Handle API request
 * });
 * ```
 */

/**
 * 정적 파일 서빙 미들웨어 설정
 *
 * 목적: 빌드된 클라이언트 파일들(CSS, JS, 이미지 등)을 HTTP로 제공
 * 서빙 경로: /browser 디렉터리의 모든 파일
 *
 * 설정 옵션:
 * - maxAge: '1y' - 브라우저 캐시를 1년으로 설정 (성능 최적화)
 * - index: false - 디렉터리 인덱스 파일 자동 서빙 비활성화
 * - redirect: false - 디렉터리 접근 시 자동 리다이렉트 비활성화
 *
 * 주의사항:
 * - Angular가 SPA이므로 index.html은 별도 처리
 * - 정적 파일 우선 처리하여 Angular 라우팅과 충돌 방지
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Angular SSR 요청 처리 미들웨어
 *
 * 목적: 정적 파일이 아닌 모든 요청을 Angular 애플리케이션으로 라우팅
 * 처리 과정:
 * 1. AngularNodeAppEngine이 요청 분석
 * 2. 해당 라우트에 맞는 컴포넌트 서버 렌더링
 * 3. HTML 응답 생성 및 클라이언트로 전송
 * 4. 클라이언트에서 하이드레이션 수행
 *
 * 매개변수:
 * - req: HTTP 요청 객체 (URL, 헤더, 쿼리 파라미터 등)
 * - res: HTTP 응답 객체
 * - next: Express의 다음 미들웨어 호출 함수
 *
 * 에러 처리:
 * - Angular 렌더링 실패 시 next(error)로 에러 미들웨어에 전달
 * - 응답이 없는 경우 next()로 404 처리 위임
 *
 * 주의사항:
 * - 이 미들웨어는 모든 라우트의 fallback 역할
 * - 정적 파일 미들웨어보다 뒤에 위치해야 함
 */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * 서버 시작 로직 - 메인 모듈로 실행될 때만 동작
 *
 * 목적: 이 파일이 직접 실행될 때만 HTTP 서버 시작 (모듈로 import될 때는 실행 안됨)
 * 조건 확인: isMainModule()로 직접 실행 여부 판단
 *
 * 서버 설정:
 * - 포트: 환경변수 PORT 또는 기본값 4000
 * - 리스닝 대상: 모든 네트워크 인터페이스 (0.0.0.0)
 *
 * 시작 과정:
 * 1. 환경변수에서 포트 번호 확인 (배포 환경 고려)
 * 2. Express 서버 시작
 * 3. 성공 시 콘솔에 서버 주소 출력
 * 4. 실패 시 에러 발생 및 프로세스 종료
 *
 * 사용 시나리오:
 * - 개발 환경: npm run serve:ssr:front
 * - 프로덕션 환경: PM2, Docker 등에서 직접 실행
 *
 * 주의사항:
 * - 프로덕션에서는 PORT 환경변수 설정 필수
 * - 에러 발생 시 프로세스가 완전히 종료됨
 */
if (isMainModule(import.meta.url)) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * 외부 서버 플랫폼용 요청 핸들러
 *
 * 목적: Express 앱을 다양한 서버 환경에서 사용할 수 있도록 래핑
 * 사용 환경:
 * - Angular CLI 개발 서버 (ng serve 명령어)
 * - Angular CLI 빌드 프로세스
 * - Firebase Cloud Functions (서버리스 배포)
 * - Vercel, Netlify 등 서버리스 플랫폼
 * - AWS Lambda, Google Cloud Functions
 *
 * 동작 방식:
 * 1. createNodeRequestHandler가 Express 앱을 Node.js 요청 핸들러로 변환
 * 2. 플랫폼별 요청 형식을 Express 형식으로 변환
 * 3. Express 응답을 플랫폼별 응답 형식으로 변환
 *
 * 반환값: (req: IncomingMessage, res: ServerResponse) => void
 *   Node.js 표준 HTTP 요청 핸들러 함수
 *
 * 주의사항:
 * - 서버리스 환경에서는 포트 리스닝 대신 이 핸들러 사용
 * - 콜드 스타트 성능 고려 필요 (서버리스 환경)
 */
export const reqHandler = createNodeRequestHandler(app);
