import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models';

const TOKEN_KEY  = 'tl_access_token';
const REFRESH_KEY = 'tl_refresh_token';
const USER_KEY   = 'tl_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http   = inject(HttpClient);
  private router = inject(Router);
  private api    = environment.apiUrl;

  private _user = signal<AuthResponse | null>(this.loadUser());
  readonly user      = this._user.asReadonly();
  readonly isLoggedIn = computed(() => !!this._user());
  readonly isClient   = computed(() => this._user()?.role === 'CLIENT');
  readonly isMember   = computed(() => this._user()?.role === 'MEMBER');

  register(req: RegisterRequest) {
    return this.http.post<AuthResponse>(`${this.api}/auth/register`, req).pipe(
      tap(res => this.persist(res))
    );
  }

  login(req: LoginRequest) {
    return this.http.post<AuthResponse>(`${this.api}/auth/login`, req).pipe(
      tap(res => this.persist(res))
    );
  }

  refresh() {
    const refreshToken = localStorage.getItem(REFRESH_KEY);
    return this.http.post<AuthResponse>(`${this.api}/auth/refresh`, { refreshToken }).pipe(
      tap(res => this.persist(res))
    );
  }

  logout() {
    this.http.post(`${this.api}/auth/logout`, {}).subscribe({
      complete: () => this.clear(),
      error:    () => this.clear()
    });
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  patchUser(patch: Partial<AuthResponse & { avatarColor: string; location: string; bio: string }>) {
    const current = this._user();
    if (!current) return;
    const updated = { ...current, ...patch };
    localStorage.setItem(USER_KEY, JSON.stringify(updated));
    this._user.set(updated as AuthResponse);
  }

  private persist(res: AuthResponse) {
    localStorage.setItem(TOKEN_KEY,  res.accessToken);
    localStorage.setItem(REFRESH_KEY, res.refreshToken);
    localStorage.setItem(USER_KEY,   JSON.stringify(res));
    this._user.set(res);
  }

  private clear() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
    this._user.set(null);
    this.router.navigate(['/']);
  }

  private loadUser(): AuthResponse | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}
