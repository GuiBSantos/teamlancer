import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TeamService } from '../../core/services/team.service';
import { TeamSummary } from '../../core/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {
  private teamService = inject(TeamService);
  featured = signal<TeamSummary[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.teamService.featured().subscribe({
      next: teams => { this.featured.set(teams); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  scoreClass(score: number): string {
    if (score >= 80) return 'score-high';
    if (score >= 60) return 'score-mid';
    return 'score-low';
  }
}
