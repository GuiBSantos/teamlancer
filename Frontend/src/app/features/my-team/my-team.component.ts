import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TeamService } from '../../core/services/team.service';
import { RequestService } from '../../core/services/request.service';
import { AuthService } from '../../core/services/auth.service';
import { TeamDetail, TeamInvite, JoinRequest, ProjectRequest } from '../../core/models';

type Tab = 'overview' | 'requests' | 'invites' | 'join-requests';

@Component({
  selector: 'app-my-team',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterLink, DatePipe],
  templateUrl: './my-team.component.html',
  styleUrl: './my-team.component.css'
})
export class MyTeamComponent implements OnInit {
  private teamService    = inject(TeamService);
  private requestService = inject(RequestService);
  auth = inject(AuthService);
  private fb = inject(FormBuilder);

  team        = signal<TeamDetail | null>(null);
  loading     = signal(true);
  creating    = signal(false);
  saving      = signal(false);
  error       = signal('');
  success     = signal('');
  activeTab   = signal<Tab>('overview');

  showDeleteConfirm = signal(false);
  showLeaveConfirm  = signal(false);
  deleting          = signal(false);
  leaving           = signal(false);

  invites        = signal<TeamInvite[]>([]);
  inviteEmail    = '';
  inviteRole     = 'Desenvolvedor';
  sendingInvite  = signal(false);
  inviteError    = signal('');
  inviteSuccess  = signal('');

  joinRequests  = signal<JoinRequest[]>([]);
  respondingId  = signal<string | null>(null);

  projectRequests    = signal<ProjectRequest[]>([]);
  respondingReqId    = signal<string | null>(null);

  techInput = '';
  techStack = signal<string[]>([]);
  form = this.fb.group({
    name:        ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    location:    [''],
  });

  readonly isOwner = computed(() => {
    const uid = this.auth.user()?.userId;
    return uid ? this.team()?.ownerId === uid : false;
  });

  ngOnInit() {
    this.teamService.myTeam().subscribe({
      next: t  => { this.team.set(t); this.loading.set(false); this.loadPanelData(); },
      error: () => this.loading.set(false)
    });
  }

  loadPanelData() {
    const t = this.team();
    if (!t) return;

    if (this.isOwner()) {
      this.teamService.teamInvites(t.id).subscribe({ next: l => this.invites.set(l), error: () => {} });
      this.teamService.teamJoinRequests(t.id).subscribe({ next: l => this.joinRequests.set(l), error: () => {} });
      this.requestService.teamRequests(t.id).subscribe({
        next: page => this.projectRequests.set(page.content),
        error: () => {}
      });
    }
  }

  setTab(tab: Tab) { this.activeTab.set(tab); }

  startCreating()  { this.creating.set(true); }
  cancelCreating() { this.creating.set(false); this.error.set(''); }

  addTech() {
    const t = this.techInput.trim();
    if (t && !this.techStack().includes(t)) this.techStack.update(a => [...a, t]);
    this.techInput = '';
  }
  removeTech(tech: string) { this.techStack.update(a => a.filter(t => t !== tech)); }

  createTeam() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');
    this.teamService.create({ ...this.form.value as any, techStack: this.techStack() }).subscribe({
      next: t => {
        this.team.set(t); this.creating.set(false); this.saving.set(false);
        this.success.set('Time criado com sucesso!');
        setTimeout(() => this.success.set(''), 3000);
        this.loadPanelData();
      },
      error: err => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Erro ao criar time.');
      }
    });
  }

  confirmDelete() { this.showDeleteConfirm.set(true); }
  cancelDelete()  { this.showDeleteConfirm.set(false); }

  deleteTeam() {
    const t = this.team();
    if (!t) return;
    this.deleting.set(true);
    this.teamService.delete(t.id).subscribe({
      next: () => { this.team.set(null); this.deleting.set(false); this.showDeleteConfirm.set(false); },
      error: err => { this.deleting.set(false); this.error.set(err.error?.message ?? 'Erro ao excluir time.'); }
    });
  }

  confirmLeave() { this.showLeaveConfirm.set(true); }
  cancelLeave()  { this.showLeaveConfirm.set(false); }

  leaveTeam() {
    const t = this.team();
    if (!t) return;
    this.leaving.set(true);
    this.teamService.leave(t.id).subscribe({
      next: () => { this.team.set(null); this.leaving.set(false); this.showLeaveConfirm.set(false); },
      error: err => { this.leaving.set(false); this.error.set(err.error?.message ?? 'Erro ao sair do time.'); }
    });
  }

  sendInvite() {
    const t = this.team();
    if (!t || !this.inviteEmail.trim()) return;
    this.sendingInvite.set(true);
    this.inviteError.set('');
    this.teamService.sendInvite(t.id, { email: this.inviteEmail.trim(), roleInTeam: this.inviteRole }).subscribe({
      next: inv => {
        this.invites.update(l => [inv, ...l]);
        this.inviteEmail = '';
        this.sendingInvite.set(false);
        this.inviteSuccess.set('Convite enviado!');
        setTimeout(() => this.inviteSuccess.set(''), 3000);
      },
      error: err => {
        this.sendingInvite.set(false);
        this.inviteError.set(
          err.status === 404 ? 'Nenhum usuário com este e-mail.'
          : err.status === 409 ? 'Usuário já é membro ou já foi convidado.'
          : err.error?.message ?? 'Erro ao enviar convite.'
        );
      }
    });
  }

  respondJoin(id: string, status: 'ACCEPTED' | 'REJECTED') {
    this.respondingId.set(id);
    this.teamService.respondJoinRequest(id, status).subscribe({
      next: updated => { this.joinRequests.update(l => l.map(r => r.id === id ? updated : r)); this.respondingId.set(null); },
      error: () => this.respondingId.set(null)
    });
  }

  pendingJoinRequests() { return this.joinRequests().filter(r => r.status === 'PENDING'); }

  respondRequest(id: string, status: 'ACCEPTED' | 'REJECTED') {
    this.respondingReqId.set(id);
    this.requestService.updateStatus(id, status).subscribe({
      next: updated => {
        this.projectRequests.update(l => l.map(r => r.id === id ? updated : r));
        this.respondingReqId.set(null);
      },
      error: () => this.respondingReqId.set(null)
    });
  }

  pendingProjectRequests() { return this.projectRequests().filter(r => r.status === 'PENDING'); }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'Aguardando', ACCEPTED: 'Aceito', REJECTED: 'Recusado', CANCELLED: 'Cancelado'
    };
    return map[status] ?? status;
  }

  statusClass(status: string): string {
    return `badge badge-${status.toLowerCase()}`;
  }
}
