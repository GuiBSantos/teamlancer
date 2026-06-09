import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { RequestService } from '../../core/services/request.service';
import { TeamService } from '../../core/services/team.service';
import { AuthService } from '../../core/services/auth.service';
import { ProjectRequest, TeamInvite, JoinRequest, TeamDetail } from '../../core/models';
import { PendingCountPipe, AcceptedCountPipe } from './dashboard.pipes';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule, DatePipe, PendingCountPipe, AcceptedCountPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private requestService = inject(RequestService);
  private teamService    = inject(TeamService);
  auth = inject(AuthService);

  requests = signal<ProjectRequest[]>([]);
  total    = signal(0);

  incomingProjectRequests = signal<ProjectRequest[]>([]);
  incomingTotal = signal(0);

  invites      = signal<TeamInvite[]>([]);
  joinRequests = signal<JoinRequest[]>([]);

  loading = signal(true);

  ngOnInit() {
    if (this.auth.isClient()) {
      this.requestService.myRequests().subscribe({
        next: page => { this.requests.set(page.content); this.total.set(page.totalElements); this.loading.set(false); },
        error: ()   => this.loading.set(false)
      });
    } else if (this.auth.isMember()) {
      let done = 0;
      const check = () => { if (++done === 3) this.loading.set(false); };

      this.teamService.myInvites().subscribe({
        next: list => { this.invites.set(list); check(); },
        error: ()  => check()
      });
      this.teamService.myJoinRequests().subscribe({
        next: list => { this.joinRequests.set(list); check(); },
        error: ()  => check()
      });

      this.teamService.myTeam().subscribe({
        next: (team: TeamDetail | null) => {
          if (team) {
            this.requestService.teamRequests(team.id).subscribe({
              next: page => { this.incomingProjectRequests.set(page.content); this.incomingTotal.set(page.totalElements); check(); },
              error: () => check()
            });
          } else {
            check();
          }
        },
        error: () => check()
      });
    } else {
      this.loading.set(false);
    }
  }

  cancelRequest(id: string) {
    this.requestService.updateStatus(id, 'CANCELLED').subscribe({
      next: updated => this.requests.update(list => list.map(r => r.id === id ? updated : r))
    });
  }

  respondProjectRequest(id: string, status: 'ACCEPTED' | 'REJECTED' | 'RENEGOTIATING') {
    this.requestService.updateStatus(id, status).subscribe({
      next: updated => {
        this.incomingProjectRequests.update(list => list.map(r => r.id === id ? updated : r));
      },
      error: (err) => console.error('Error updating project request status', err)
    });
  }

  respondingInviteId = signal<string | null>(null);

  respondInvite(id: string, status: 'ACCEPTED' | 'REJECTED') {
    this.respondingInviteId.set(id);
    this.teamService.respondInvite(id, status).subscribe({
      next: updated => {
        this.invites.update(list => list.map(i => i.id === id ? updated : i));
        this.respondingInviteId.set(null);
      },
      error: () => this.respondingInviteId.set(null)
    });
  }

  statusClass(status: string): string {
    return `badge badge-${status.toLowerCase()}`;
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'Aguardando', ACCEPTED: 'Aceito', REJECTED: 'Recusado', CANCELLED: 'Cancelado', RENEGOTIATING: 'Renegociando'
    };
    return map[status] ?? status;
  }
}