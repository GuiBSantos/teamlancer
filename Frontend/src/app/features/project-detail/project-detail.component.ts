import { Component, inject, OnInit, OnDestroy, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ChatService } from '../../core/services/chat.service';
import { RatingService } from '../../core/services/rating.service';
import { ProjectService } from '../../core/services/project.service';
import { AuthService } from '../../core/services/auth.service';
import { ChatMessage, Project, ProjectRatings, ProjectStatus } from '../../core/models';

@Component({
  selector: 'app-project-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './project-detail.component.html',
  styleUrl: './project-detail.component.css'
})
export class ProjectDetailComponent implements OnInit, OnDestroy {
  private route       = inject(ActivatedRoute);
  private chatSvc     = inject(ChatService);
  private ratingSvc   = inject(RatingService);
  private projectSvc  = inject(ProjectService);
  auth                = inject(AuthService);

  project    = signal<Project | null>(null);
  messages   = signal<ChatMessage[]>([]);
  ratings    = signal<ProjectRatings | null>(null);
  loading    = signal(true);

  newMessage = '';
  sending    = signal(false);

  showRatingForm = signal(false);
  ratingScore    = signal(0);
  ratingComment  = '';
  ratingSubmitting = signal(false);
  ratingError    = signal('');

  updatingStatus = signal(false);

  private pollSub?: Subscription;
  private projectId = '';

  readonly STATUS_LABELS: Record<string, string> = {
    IN_PROGRESS: 'Em andamento',
    IN_TESTING:  'Em teste',
    COMPLETED:   'Concluído',
    ON_HOLD:     'Pausado',
    CANCELLED:   'Cancelado'
  };

  readonly STATUS_FLOW: ProjectStatus[] = ['IN_PROGRESS', 'IN_TESTING', 'COMPLETED'];

  ngOnInit() {
    this.projectId = this.route.snapshot.paramMap.get('id') ?? '';
    this.loadAll();

    this.pollSub = interval(10000).pipe(
      switchMap(() => this.chatSvc.getMessages(this.projectId))
    ).subscribe(msgs => this.messages.set(msgs));
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
  }

  loadAll() {
    this.loading.set(true);
    this.projectSvc.getById(this.projectId).subscribe({
      next: p => {
        this.project.set(p);
        this.loading.set(false);
        this.loadMessages();
        if (p.status === 'COMPLETED') this.loadRatings();
      },
      error: () => this.loading.set(false)
    });
  }

  loadMessages() {
    this.chatSvc.getMessages(this.projectId).subscribe(msgs => this.messages.set(msgs));
  }

  loadRatings() {
    this.ratingSvc.getProjectRatings(this.projectId).subscribe(r => this.ratings.set(r));
  }

  sendMessage() {
    if (!this.newMessage.trim() || this.sending()) return;
    this.sending.set(true);
    this.chatSvc.send(this.projectId, this.newMessage.trim()).subscribe({
      next: msg => {
        this.messages.update(list => [...list, msg]);
        this.newMessage = '';
        this.sending.set(false);
      },
      error: () => this.sending.set(false)
    });
  }

  onEnter(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  setStatus(status: ProjectStatus) {
    if (this.updatingStatus()) return;
    this.updatingStatus.set(true);
    this.projectSvc.updateStatus(this.projectId, status).subscribe({
      next: p => {
        this.project.set(p);
        this.updatingStatus.set(false);
        if (p.status === 'COMPLETED') this.loadRatings();
      },
      error: () => this.updatingStatus.set(false)
    });
  }

  submitRating() {
    if (this.ratingScore() === 0 || this.ratingSubmitting()) return;
    this.ratingSubmitting.set(true);
    this.ratingError.set('');
    this.ratingSvc.rate(this.projectId, {
      score: this.ratingScore(),
      comment: this.ratingComment || undefined
    }).subscribe({
      next: () => {
        this.showRatingForm.set(false);
        this.loadRatings();
        this.ratingSubmitting.set(false);
      },
      error: (e) => {
        this.ratingError.set(e.status === 409 ? 'Você já avaliou este projeto.' : 'Erro ao enviar avaliação.');
        this.ratingSubmitting.set(false);
      }
    });
  }

  isTeamOwner(): boolean {
    const user = this.auth.user();
    const p = this.project();
    if (!user || !p) return false;
    return p.teamId === user.userId;
  }

  isMine(msg: ChatMessage): boolean {
    return msg.senderId === this.auth.user()?.userId;
  }

  hasRated(): boolean {
    const r = this.ratings();
    if (!r) return false;
    const type = this.auth.isClient() ? 'clientRating' : 'teamRating';
    return !!r[type];
  }

  stars(n: number): boolean[] {
    return Array.from({ length: 5 }, (_, i) => i < n);
  }
}
