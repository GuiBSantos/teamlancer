import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface ProfileUpdateRequest {
  name?: string;
  location?: string;
  bio?: string;
  avatarColor?: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private http = inject(HttpClient);
  private api  = environment.apiUrl;

  update(payload: ProfileUpdateRequest) {
    return this.http.patch<any>(`${this.api}/users/me`, payload);
  }

  get() {
    return this.http.get<any>(`${this.api}/users/me`);
  }
}
