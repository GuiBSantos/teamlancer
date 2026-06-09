import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CreateProjectRequest, Page, ProjectRequest } from '../models';

@Injectable({ providedIn: 'root' })
export class RequestService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  create(req: CreateProjectRequest) {
    return this.http.post<ProjectRequest>(`${this.api}/requests`, req);
  }

  myRequests(page = 0, size = 10) {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ProjectRequest>>(`${this.api}/requests/me`, { params });
  }

  getById(id: string) {
    return this.http.get<ProjectRequest>(`${this.api}/requests/${id}`);
  }

  updateStatus(id: string, status: string) {
    return this.http.patch<ProjectRequest>(`${this.api}/requests/${id}/status`, { status });
  }

  teamRequests(teamId: string, page = 0, size = 10) {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ProjectRequest>>(`${this.api}/requests/team/${teamId}`, { params });
  }
}
