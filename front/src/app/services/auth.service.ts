import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

/**
 * 로그인 요청 인터페이스
 *
 * 백엔드 API: POST /api/auth/login
 */
interface LoginRequest {
  email: string;
  password: string;
}

/**
 * 로그인 응답 인터페이스
 *
 * 백엔드 API: POST /api/auth/login 응답
 */
interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  empInfo: {
    empSeq: number;
    email: string;
    empNm: string;
    companySeq: number;
    empRole: string;
  };
}

/**
 * JWT 토큰 페이로드 인터페이스
 *
 * JWT 디코딩 후 추출되는 정보
 */
interface JwtPayload {
  sub: string;
  companySeq: number;
  empRole: string;
  email: string;
  iat: number;
  exp: number;
}

/**
 * 인증 서비스
 *
 * 책임:
 * - 사용자 로그인/로그아웃 처리
 * - JWT 토큰 관리 (저장, 조회, 갱신)
 * - 인증 상태 관리
 * - 자동 토큰 갱신
 *
 * 토큰 저장소:
 * - localStorage: AccessToken, RefreshToken (영구 저장)
 *
 * 설정:
 * - API 기본 URL: environment.apiUrl (또는 'http://localhost:8081')
 * - 토큰 만료 시간: 1일 (AccessToken), 7일 (RefreshToken)
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/auth';

  // 인증 상태 시그널
  isAuthenticated = signal(this.checkTokenExists());
  currentUser = signal<LoginResponse['empInfo'] | null>(null);

  // 에러 메시지 시그널
  authError = signal<string>('');

  // 로딩 상태
  private loadingSubject = new BehaviorSubject(false);
  isLoading$ = this.loadingSubject.asObservable();

  // 토큰 갱신 타이머
  private refreshTokenTimer: any;

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.initializeAuthState();
  }

  /**
   * 인증 상태 초기화
   *
   * 앱 로드 시:
   * 1. localStorage에서 토큰 확인
   * 2. 토큰이 있으면 인증 상태 활성화
   * 3. 사용자 정보 복원 (localStorage에 저장된 경우)
   */
  private initializeAuthState(): void {
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
      try {
        this.currentUser.set(JSON.parse(savedUser));
        this.isAuthenticated.set(true);
        this.scheduleTokenRefresh();
      } catch (e) {
        console.error('Failed to restore user info:', e);
        this.clearAuthData();
      }
    }
  }

  /**
   * 사용자 로그인
   *
   * 요청:
   * POST /api/auth/login
   * {
   *   "email": "user@example.com",
   *   "password": "password123"
   * }
   *
   * 응답:
   * {
   *   "accessToken": "eyJhbGc...",
   *   "refreshToken": "eyJhbGc...",
   *   "tokenType": "Bearer",
   *   "expiresIn": 86400,
   *   "empInfo": { ... }
   * }
   *
   * @param email 사용자 이메일
   * @param password 사용자 비밀번호
   * @returns Observable<LoginResponse>
   */
  login(email: string, password: string): Observable<LoginResponse> {
    this.loadingSubject.next(true);
    this.authError.set('');

    const loginRequest: LoginRequest = { email, password };

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, loginRequest)
      .pipe(
        tap((response) => {
          // 토큰 저장
          this.saveTokens(response);

          // 사용자 정보 저장
          this.currentUser.set(response.empInfo);
          localStorage.setItem('currentUser', JSON.stringify(response.empInfo));

          // 인증 상태 활성화
          this.isAuthenticated.set(true);

          // 토큰 자동 갱신 스케줄링
          this.scheduleTokenRefresh();

          this.loadingSubject.next(false);
        }),
        catchError((error) => {
          this.loadingSubject.next(false);
          return this.handleAuthError(error);
        })
      );
  }

  /**
   * 사용자 로그아웃
   *
   * 처리:
   * 1. 백엔드에 로그아웃 요청 (선택사항)
   * 2. localStorage에서 토큰 및 사용자 정보 삭제
   * 3. 인증 상태 비활성화
   * 4. 로그인 페이지로 리다이렉트
   */
  logout(): Observable<any> {
    this.loadingSubject.next(true);

    return this.http.post(`${this.apiUrl}/logout`, {})
      .pipe(
        tap(() => {
          this.clearAuthData();
          this.loadingSubject.next(false);
          this.router.navigate(['/login']);
        }),
        catchError((error) => {
          // 로그아웃 API 실패 시에도 클라이언트 상태 초기화
          this.clearAuthData();
          this.loadingSubject.next(false);
          return throwError(() => error);
        })
      );
  }

  /**
   * RefreshToken으로 AccessToken 갱신
   *
   * 요청:
   * POST /api/auth/refresh
   * {
   *   "refreshToken": "eyJhbGc..."
   * }
   *
   * 응답:
   * {
   *   "accessToken": "eyJhbGc...",
   *   "tokenType": "Bearer",
   *   "expiresIn": 86400
   * }
   *
   * @returns Observable<{accessToken: string}>
   */
  refreshAccessToken(): Observable<any> {
    const refreshToken = localStorage.getItem('refreshToken');

    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http.post<any>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(
        tap((response: any) => {
          localStorage.setItem('accessToken', response.accessToken);
          this.scheduleTokenRefresh();
        }),
        catchError((error) => {
          // 갱신 실패 시 로그아웃 처리
          this.clearAuthData();
          this.router.navigate(['/login']);
          return throwError(() => error);
        })
      );
  }

  /**
   * AccessToken 조회
   *
   * @returns AccessToken 문자열, 없으면 null
   */
  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  /**
   * RefreshToken 조회
   *
   * @returns RefreshToken 문자열, 없으면 null
   */
  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  /**
   * 현재 사용자 정보 조회
   *
   * @returns 로그인한 사용자 정보 (empSeq, email, empNm, companySeq, empRole)
   */
  getCurrentUser(): LoginResponse['empInfo'] | null {
    return this.currentUser();
  }

  /**
   * 토큰 존재 여부 확인
   *
   * @returns 유효한 토큰이 있으면 true
   */
  checkTokenExists(): boolean {
    const token = localStorage.getItem('accessToken');
    return !!token && !this.isTokenExpired(token);
  }

  /**
   * 토큰 만료 여부 확인
   *
   * @param token JWT 토큰
   * @returns 만료되었으면 true
   */
  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.decodeToken(token);
      if (!payload || !payload.exp) {
        return true;
      }
      // exp는 초 단위, Date.now()는 밀리초 단위
      return payload.exp * 1000 < Date.now();
    } catch (e) {
      return true;
    }
  }

  /**
   * JWT 토큰 디코딩
   *
   * @param token JWT 토큰
   * @returns 디코딩된 payload
   */
  private decodeToken(token: string): JwtPayload | null {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64).split('').map((c) => {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join('')
      );
      return JSON.parse(jsonPayload) as JwtPayload;
    } catch (e) {
      console.error('Failed to decode token:', e);
      return null;
    }
  }

  /**
   * 토큰 자동 갱신 스케줄링
   *
   * AccessToken 만료 5분 전에 자동으로 RefreshToken을 사용하여 새로운 AccessToken 발급
   *
   * 동작:
   * 1. 현재 토큰의 만료 시간 계산
   * 2. 만료 5분 전 시점에 갱신 실행
   * 3. 갱신 완료 후 다시 스케줄링
   */
  private scheduleTokenRefresh(): void {
    // 기존 타이머 제거
    if (this.refreshTokenTimer) {
      clearTimeout(this.refreshTokenTimer);
    }

    const token = this.getAccessToken();
    if (!token) return;

    try {
      const payload = this.decodeToken(token);
      if (!payload || !payload.exp) return;

      // 만료 시간 계산 (만료 5분 전)
      const expiresIn = payload.exp * 1000 - Date.now();
      const refreshTime = expiresIn - 5 * 60 * 1000; // 5분 전

      if (refreshTime > 0) {
        this.refreshTokenTimer = setTimeout(() => {
          console.log('Auto-refreshing access token');
          this.refreshAccessToken().subscribe({
            next: () => console.log('Token refreshed successfully'),
            error: (error) => console.error('Token refresh failed:', error)
          });
        }, refreshTime);
      }
    } catch (e) {
      console.error('Failed to schedule token refresh:', e);
    }
  }

  /**
   * 토큰 및 사용자 정보 삭제
   *
   * 처리:
   * - localStorage에서 accessToken, refreshToken, currentUser 삭제
   * - 신호 상태 초기화 (isAuthenticated = false, currentUser = null)
   * - 토큰 갱신 타이머 제거
   */
  private clearAuthData(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('currentUser');

    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.authError.set('');

    if (this.refreshTokenTimer) {
      clearTimeout(this.refreshTokenTimer);
    }
  }

  /**
   * 토큰 저장
   *
   * @param response 로그인 응답
   */
  private saveTokens(response: LoginResponse): void {
    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
  }

  /**
   * 인증 에러 처리
   *
   * @param error HTTP 에러 응답
   * @returns Observable 에러
   */
  private handleAuthError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred during authentication';

    if (error.error instanceof ErrorEvent) {
      // 클라이언트 에러
      errorMessage = error.error.message;
    } else if (error.status === 401) {
      // 인증 실패
      errorMessage = 'Invalid email or password';
    } else if (error.status === 400) {
      // 잘못된 요청
      errorMessage = error.error?.message || 'Invalid input';
    } else if (error.status === 0) {
      // 네트워크 에러
      errorMessage = 'Network error. Please check your connection.';
    }

    this.authError.set(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
