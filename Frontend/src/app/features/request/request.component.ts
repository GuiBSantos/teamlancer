import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RequestService } from '../../core/services/request.service';
import { TeamService } from '../../core/services/team.service';
import { AuthService } from '../../core/services/auth.service';
import { TeamDetail } from '../../core/models';

@Component({
  selector: 'app-request',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, CommonModule],
  templateUrl: './request.component.html',
  styleUrl: './request.component.css'
})
export class RequestComponent implements OnInit {
  private route          = inject(ActivatedRoute);
  private router         = inject(Router);
  private fb             = inject(FormBuilder);
  private requestService = inject(RequestService);
  private teamService    = inject(TeamService);
  auth                   = inject(AuthService);

  team       = signal<TeamDetail | null>(null);
  loading    = signal(true);
  submitting = signal(false);
  submitted  = signal(false);
  error      = signal('');

  form = this.fb.group({
    projectName: ['', [Validators.required, Validators.maxLength(200)]],
    description: ['', Validators.required],
    budgetRange: [''],
    deadline:    ['']
  });

  ngOnInit() {
    const teamId = this.route.snapshot.paramMap.get('teamId') ?? '';

    this.teamService.getById(teamId).subscribe({
      next: t  => { this.team.set(t); this.loading.set(false); },
      error: () => {
        this.teamService.getBySlug(teamId).subscribe({
          next: t  => { this.team.set(t); this.loading.set(false); },
          error: () => { this.error.set('Time não encontrado.'); this.loading.set(false); }
        });
      }
    });
  }

  submit() {
    if (this.form.invalid || !this.team()) return;
    this.submitting.set(true);
    this.error.set('');

    this.requestService.create({
      teamId:      this.team()!.id,
      projectName: this.form.value.projectName!,
      description: this.form.value.description!,
      budgetRange: this.form.value.budgetRange ?? undefined,
      deadline:    this.form.value.deadline    ?? undefined,
    }).subscribe({
      next: () => {
        this.submitted.set(true);
        this.submitting.set(false);
      },
      error: e => {
        this.error.set(
          e.status === 409
            ? 'Você já tem uma solicitação pendente para este time.'
            : e.error?.message ?? 'Erro ao enviar. Tente novamente.'
        );
        this.submitting.set(false);
      }
    });
  }
}
