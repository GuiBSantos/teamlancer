import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {
  CreateTeamRequest, Page, TeamDetail, TeamMember, TeamPortfolio, TeamSummary,
  TeamInvite, CreateInviteRequest, JoinRequest, CreateJoinRequest
} from '../models';

@Injectable({ providedIn: 'root' })
export class TeamService {
  private http = inject(HttpClient);
  private api  = environment.apiUrl;

  list(query?: string, page = 0, size = 10) {
    let params = new HttpParams().set('page', page).set('size', size);
    if (query?.trim()) params = params.set('q', query.trim());
    return this.http.get<Page<TeamSummary>>(`${this.api}/teams`, { params });
  }

  featured() {
    return this.http.get<TeamSummary[]>(`${this.api}/teams/featured`);
  }

  getBySlug(slug: string) {
    return this.http.get<TeamDetail>(`${this.api}/teams/${slug}`);
  }

  getById(id: string) {
    return this.http.get<TeamDetail>(`${this.api}/teams/id/${id}`);
  }

  getMembers(id: string) {
    return this.http.get<TeamMember[]>(`${this.api}/teams/${id}/members`);
  }

  getPortfolio(id: string) {
    return this.http.get<TeamPortfolio[]>(`${this.api}/teams/${id}/portfolio`);
  }

  myTeam() {
    return this.http.get<TeamDetail>(`${this.api}/teams/mine`);
  }

  create(req: CreateTeamRequest) {
    return this.http.post<TeamDetail>(`${this.api}/teams`, req);
  }

  delete(teamId: string) {
    return this.http.delete<void>(`${this.api}/teams/${teamId}`);
  }

  leave(teamId: string) {
    return this.http.delete<void>(`${this.api}/teams/${teamId}/leave`);
  }

  addMember(teamId: string, userId: string, roleInTeam: string) {
    return this.http.post<void>(`${this.api}/teams/${teamId}/members`, { userId, roleInTeam });
  }

  removeMember(teamId: string, userId: string) {
    return this.http.delete<void>(`${this.api}/teams/${teamId}/members/${userId}`);
  }

  sendInvite(teamId: string, req: CreateInviteRequest) {
    return this.http.post<TeamInvite>(`${this.api}/teams/${teamId}/invites`, req);
  }

  teamInvites(teamId: string) {
    return this.http.get<TeamInvite[]>(`${this.api}/teams/${teamId}/invites`);
  }

  myInvites() {
    return this.http.get<TeamInvite[]>(`${this.api}/invites/me`);
  }

  respondInvite(inviteId: string, status: 'ACCEPTED' | 'REJECTED') {
    return this.http.patch<TeamInvite>(`${this.api}/invites/${inviteId}`, { status });
  }

  requestToJoin(req: CreateJoinRequest) {
    return this.http.post<JoinRequest>(`${this.api}/join-requests`, req);
  }

  teamJoinRequests(teamId: string) {
    return this.http.get<JoinRequest[]>(`${this.api}/teams/${teamId}/join-requests`);
  }

  respondJoinRequest(requestId: string, status: 'ACCEPTED' | 'REJECTED') {
    return this.http.patch<JoinRequest>(`${this.api}/join-requests/${requestId}`, { status });
  }

  myJoinRequests() {
    return this.http.get<JoinRequest[]>(`${this.api}/join-requests/me`);
  }
}
