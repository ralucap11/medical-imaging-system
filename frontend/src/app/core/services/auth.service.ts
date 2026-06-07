import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment.development';


export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}


export interface LoginRequest {
  email: string;
  password: string;
}


export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY  = 'auth_user';

  constructor(private http: HttpClient, private router: Router) {}

  register(data: RegisterRequest): Observable<any> {
    return this.http.post(`${environment.apiUrl}/auth/register`, data);
  }

  login(data: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, data).pipe(
      tap(response => {
        localStorage.setItem(this.TOKEN_KEY, response.token);
      })
    );
  }


  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['/auth/login']);
  }


  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }


  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): { username: string; role: string } | null {
    const user = localStorage.getItem(this.USER_KEY);
    return user ? JSON.parse(user) : null;
  }
}
