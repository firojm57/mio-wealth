import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, UserProfile, UserRegisterRequest } from '../models/user.model';
import { ToastService, ToastType } from './toast.service';

const TOKEN_KEY = 'mio_wealth_auth_token';
const USER_KEY = 'mio_wealth_user_profile';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  private readonly tokenSignal = signal<string | null>(this.getStoredToken());
  readonly currentUser = signal<UserProfile | null>(this.getStoredUser());

  readonly isAuthenticated = computed(() => !!this.tokenSignal());

  constructor() {
    this.initializeSession();
  }

  private getStoredToken(): string | null {
    if (typeof window === 'undefined') return null;
    return sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY);
  }

  private getStoredUser(): UserProfile | null {
    if (typeof window === 'undefined') return null;
    const raw = sessionStorage.getItem(USER_KEY) || localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserProfile;
    } catch {
      return null;
    }
  }

  initializeSession(): void {
    const token = this.getStoredToken();
    if (token) {
      this.tokenSignal.set(token);
      const user = this.getStoredUser();
      if (user) {
        this.currentUser.set(user);
      }
    }
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    const url = `${environment.apiPrefix}/auth/login`;
    return this.http.post<AuthResponse>(url, credentials).pipe(
      tap((response) => {
        this.persistSession(response.token, response.userProfile || {
          firstName: response.userId,
          lastName: 'User',
          email: `${response.userId}@miowealth.local`,
          userId: response.userId
        });
        this.toastService.showToast('Successfully signed in.', 'success');
      }),
      catchError((error) => {
        return throwError(() => error);
      })
    );
  }

  register(data: UserRegisterRequest): Observable<any> {
    const url = `${environment.apiPrefix}/auth/register`;
    return this.http.post(url, data).pipe(
      tap(() => {
        this.toastService.showToast('Account registered successfully. You can now sign in.', 'success');
      })
    );
  }

  logout(message: string = 'You have been signed out.', toastType: ToastType = 'info'): void {
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
    this.tokenSignal.set(null);
    this.currentUser.set(null);
    this.toastService.showToast(message, toastType);
    this.router.navigate(['/login']);
  }

  private persistSession(token: string, profile: UserProfile): void {
    if (typeof window !== 'undefined') {
      sessionStorage.setItem(TOKEN_KEY, token);
      sessionStorage.setItem(USER_KEY, JSON.stringify(profile));
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(profile));
    }
    this.tokenSignal.set(token);
    this.currentUser.set(profile);
  }
}
