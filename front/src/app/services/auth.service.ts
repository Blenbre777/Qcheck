import { Injectable, signal, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

interface LoginRequest {
  email: string;
  password: string;
}

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

interface JwtPayload {
  sub: string;
  companySeq: number;
  empRole: string;
  email: string;
  iat: number;
  exp: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8081/api/auth';
  private isBrowser: boolean;

  isAuthenticated = signal(false);
  currentUser = signal<LoginResponse['empInfo'] | null>(null);
  authError = signal<string>('');

  private loadingSubject = new BehaviorSubject(false);
  isLoading$ = this.loadingSubject.asObservable();

  private refreshTokenTimer: any;

  constructor(
    private http: HttpClient,
    private router: Router,
    @Inject(PLATFORM_ID) platformId: object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    if (this.isBrowser) {
      this.isAuthenticated.set(this.checkTokenExists());
      this.initializeAuthState();
    }
  }

  private initializeAuthState(): void {
    const savedUser = this.getItem('currentUser');
    if (savedUser) {
      try {
        this.currentUser.set(JSON.parse(savedUser));
        this.isAuthenticated.set(true);
        this.scheduleTokenRefresh();
      } catch (e) {
        this.clearAuthData();
      }
    }
  }

  login(email: string, password: string): Observable<LoginResponse> {
    this.loadingSubject.next(true);
    this.authError.set('');

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { email, password } as LoginRequest)
      .pipe(
        tap((response) => {
          this.saveTokens(response);
          this.currentUser.set(response.empInfo);
          this.setItem('currentUser', JSON.stringify(response.empInfo));
          this.isAuthenticated.set(true);
          this.scheduleTokenRefresh();
          this.loadingSubject.next(false);
        }),
        catchError((error) => {
          this.loadingSubject.next(false);
          return this.handleAuthError(error);
        })
      );
  }

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
          this.clearAuthData();
          this.loadingSubject.next(false);
          return throwError(() => error);
        })
      );
  }

  refreshAccessToken(): Observable<any> {
    const refreshToken = this.getItem('refreshToken');
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }
    return this.http.post<any>(`${this.apiUrl}/refresh`, { refreshToken })
      .pipe(
        tap((response: any) => {
          this.setItem('accessToken', response.accessToken);
          this.scheduleTokenRefresh();
        }),
        catchError((error) => {
          this.clearAuthData();
          this.router.navigate(['/login']);
          return throwError(() => error);
        })
      );
  }

  getAccessToken(): string | null {
    return this.getItem('accessToken');
  }

  getRefreshToken(): string | null {
    return this.getItem('refreshToken');
  }

  getCurrentUser(): LoginResponse['empInfo'] | null {
    return this.currentUser();
  }

  checkTokenExists(): boolean {
    if (!this.isBrowser) return false;
    const token = this.getItem('accessToken');
    return !!token && !this.isTokenExpired(token);
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.decodeToken(token);
      if (!payload?.exp) return true;
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  private decodeToken(token: string): JwtPayload | null {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64).split('').map((c) =>
          '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        ).join('')
      );
      return JSON.parse(jsonPayload) as JwtPayload;
    } catch {
      return null;
    }
  }

  private scheduleTokenRefresh(): void {
    if (this.refreshTokenTimer) clearTimeout(this.refreshTokenTimer);
    const token = this.getAccessToken();
    if (!token) return;
    try {
      const payload = this.decodeToken(token);
      if (!payload?.exp) return;
      const refreshTime = payload.exp * 1000 - Date.now() - 5 * 60 * 1000;
      if (refreshTime > 0) {
        this.refreshTokenTimer = setTimeout(() => {
          this.refreshAccessToken().subscribe({
            error: (e) => console.error('Token refresh failed:', e)
          });
        }, refreshTime);
      }
    } catch { /* ignore */ }
  }

  private clearAuthData(): void {
    this.removeItem('accessToken');
    this.removeItem('refreshToken');
    this.removeItem('currentUser');
    this.isAuthenticated.set(false);
    this.currentUser.set(null);
    this.authError.set('');
    if (this.refreshTokenTimer) clearTimeout(this.refreshTokenTimer);
  }

  private saveTokens(response: LoginResponse): void {
    this.setItem('accessToken', response.accessToken);
    this.setItem('refreshToken', response.refreshToken);
  }

  private getItem(key: string): string | null {
    return this.isBrowser ? localStorage.getItem(key) : null;
  }

  private setItem(key: string, value: string): void {
    if (this.isBrowser) localStorage.setItem(key, value);
  }

  private removeItem(key: string): void {
    if (this.isBrowser) localStorage.removeItem(key);
  }

  private handleAuthError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred during authentication';
    if (error.error instanceof ErrorEvent) {
      errorMessage = error.error.message;
    } else if (error.status === 401) {
      errorMessage = 'Invalid email or password';
    } else if (error.status === 400) {
      errorMessage = error.error?.message || 'Invalid input';
    } else if (error.status === 0) {
      errorMessage = 'Network error. Please check your connection.';
    }
    this.authError.set(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
