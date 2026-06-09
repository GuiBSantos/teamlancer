import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TeamService } from '../../core/services/team.service';
import { AuthService } from '../../core/services/auth.service';
import { TeamDetail } from '../../core/models';

@Component({
  selector: 'app-team-detail',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './team-detail.component.html',
  styleUrl: './team-detail.component.css'
})
export class TeamDetailComponent implements OnInit {
  private route       = inject(ActivatedRoute);
  private router      = inject(Router);
  private teamService = inject(TeamService);
  auth                = inject(AuthService);

  team    = signal<TeamDetail | null>(null);
  loading = signal(true);
  error   = signal('');

  showJoinForm = signal(false);
  joinMessage  = '';
  joiningTeam  = signal(false);
  joinSuccess  = signal(false);
  joinError    = signal('');

  ngOnInit() {
    const slug = this.route.snapshot.paramMap.get('slug') ?? '';
    this.teamService.getBySlug(slug).subscribe({
      next: t  => { this.team.set(t); this.loading.set(false); },
      error: () => { this.error.set('Time não encontrado.'); this.loading.set(false); }
    });
  }

  get canRequest(): boolean {
    return this.auth.isLoggedIn() && this.auth.isClient();
  }

  get canJoin(): boolean {
    if (!this.auth.isLoggedIn() || !this.auth.isMember()) return false;
    const uid = this.auth.user()?.userId;
    return !this.team()?.members.some(m => m.userId === uid);
  }

  get isAlreadyMember(): boolean {
    if (!this.auth.isMember()) return false;
    const uid = this.auth.user()?.userId;
    return !!this.team()?.members.some(m => m.userId === uid);
  }

  requestProject() {
    if (!this.auth.isLoggedIn()) { this.router.navigate(['/auth/login']); return; }
    this.router.navigate(['/request', this.team()!.id]);
  }

  submitJoin() {
    if (!this.team()) return;
    this.joiningTeam.set(true);
    this.joinError.set('');
    this.teamService.requestToJoin({ teamId: this.team()!.id, message: this.joinMessage }).subscribe({
      next: () => {
        this.joiningTeam.set(false);
        this.joinSuccess.set(true);
        this.showJoinForm.set(false);
      },
      error: e => {
        this.joiningTeam.set(false);
        this.joinError.set(
          e.status === 409
            ? 'Você já enviou uma solicitação para este time.'
            : e.error?.message ?? 'Erro ao enviar. Tente novamente.'
        );
      }
    });
  }

  scoreClass(score: number): string {
    if (score >= 80) return 'score-badge score-high';
    if (score >= 60) return 'score-badge score-mid';
    return 'score-badge score-low';
  }
}
