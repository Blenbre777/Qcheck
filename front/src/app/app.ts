import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * 메인 애플리케이션 컴포넌트 - Qcheck 프로젝트의 루트 컴포넌트
 *
 * 목적: Angular 애플리케이션의 최상위 컴포넌트로서 전체 애플리케이션의 기본 구조를 제공
 * 주요 역할:
 * - 라우터 아웃렛을 통한 페이지 네비게이션 제공
 * - 애플리케이션 전반에 걸친 공통 레이아웃 관리
 * - Angular SSR 지원을 위한 기본 컴포넌트 설정
 *
 * 사용 패턴: Angular 20의 최신 standalone 컴포넌트 아키텍처 사용
 * 템플릿: app.html 파일에서 HTML 구조 정의
 * 스타일: app.scss 파일에서 컴포넌트별 스타일 정의
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  /**
   * 애플리케이션 제목을 저장하는 시그널
   *
   * 자료형: WritableSignal<string>
   * 기본값: 'front' (프로젝트 식별용)
   * 반응성: Angular 시그널 패턴으로 상태 변화 자동 감지
   *
   * 사용 목적:
   * - 브라우저 타이틀 바에 표시될 제목
   * - 애플리케이션 헤더나 네비게이션에서 참조 가능
   *
   * 주의사항: readonly로 선언되어 외부에서 직접 수정 불가
   */
  protected readonly title = signal('front');
}
