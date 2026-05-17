import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * JWT 인증 인터셉터
 *
 * 목적:
 * - 모든 HTTP 요청에 JWT 토큰을 자동으로 Authorization 헤더에 추가
 * - Authorization: Bearer <accessToken> 형식으로 전송
 *
 * 동작 원리:
 * 1. 모든 HTTP 요청을 가로챈다 (HttpInterceptor)
 * 2. localStorage에서 accessToken을 조회
 * 3. 토큰이 있으면 Authorization 헤더에 추가
 * 4. 수정된 요청을 백엔드로 전송
 * 5. 응답을 그대로 반환
 *
 * 사용 위치:
 * - app.config.ts의 providers 배열에 HTTP_INTERCEPTORS로 등록
 *
 * 주의사항:
 * - 로그인, 로그아웃 엔드포인트는 토큰을 자동으로 받으므로 별도 처리 불필요
 * - 토큰이 없으면 Authorization 헤더를 추가하지 않음 (로그인 요청 등)
 * - 401 Unauthorized 응답은 AuthService의 logout 메서드에서 처리
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  /**
   * HTTP 요청 인터셉트
   *
   * @param request 원본 HTTP 요청
   * @param next 요청을 다음 핸들러로 전달하는 함수
   * @returns 수정된 요청을 처리한 Observable<HttpEvent>
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // 현재 저장된 accessToken 조회
    const token = this.authService.getAccessToken();

    // 토큰이 있으면 Authorization 헤더에 추가
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    // 수정된 요청을 다음 핸들러로 전달
    return next.handle(request);
  }
}
