import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Project, ProjectStatus } from '../models';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  getById(id: string) {
    return this.http.get<Project>(`${this.api}/projects/${id}`);
  }

  updateStatus(id: string, status: ProjectStatus) {
    return this.http.patch<Project>(`${this.api}/projects/${id}/status`, { status });
  }
}
