import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TeamService } from '../../core/services/team.service';
import { TeamSummary, Page } from '../../core/models';

@Component({
  selector: 'app-teams',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './teams.component.html',
  styleUrl: './teams.component.css'
})
export class TeamsComponent implements OnInit {
  private teamService = inject(TeamService);

  result = signal<Page<TeamSummary> | null>(null);
  loading = signal(true);
  query = signal('');
  searchInput = '';

  ngOnInit() { this.load(); }

  load(page = 0) {
    this.loading.set(true);
    this.teamService.list(this.query(), page).subscribe({
      next: res => { this.result.set(res); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  search() {
    this.query.set(this.searchInput);
    this.load(0);
  }

  clearSearch() {
    this.searchInput = '';
    this.query.set('');
    this.load(0);
  }

  goPage(page: number) { this.load(page); }

  scoreClass(score: number): string {
    if (score >= 80) return 'score-high';
    if (score >= 60) return 'score-mid';
    return 'score-low';
  }

  get pages(): number[] {
    const total = this.result()?.totalPages ?? 0;
    return Array.from({ length: total }, (_, i) => i);
  }
}
