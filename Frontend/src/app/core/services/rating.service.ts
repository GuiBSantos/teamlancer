import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { CreateRatingRequest, ProjectRatings, Rating } from '../models';

@Injectable({ providedIn: 'root' })
export class RatingService {
  private http = inject(HttpClient);
  private api = environment.apiUrl;

  getProjectRatings(projectId: string) {
    return this.http.get<ProjectRatings>(`${this.api}/projects/${projectId}/ratings`);
  }

  rate(projectId: string, req: CreateRatingRequest) {
    return this.http.post<Rating>(`${this.api}/projects/${projectId}/ratings`, req);
  }
}
