import { Routes } from '@angular/router';
import { authGuard, guestGuard, memberGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'teams',
    loadComponent: () =>
      import('./features/teams/teams.component').then(m => m.TeamsComponent)
  },
  {
    path: 'teams/:slug',
    loadComponent: () =>
      import('./features/team-detail/team-detail.component').then(m => m.TeamDetailComponent)
  },
  {
    path: 'auth/login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'auth/register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/profile.component').then(m => m.ProfileComponent)
  },
  {
    path: 'my-team',
    canActivate: [authGuard, memberGuard],
    loadComponent: () =>
      import('./features/my-team/my-team.component').then(m => m.MyTeamComponent)
  },
  {
    path: 'request/:teamId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/request/request.component').then(m => m.RequestComponent)
  },
  {
    path: 'projects/:id',
    canActivate: [authGuard],
    loadComponent: () =>
        import('./features/project-detail/project-detail.component').then(m => m.ProjectDetailComponent)
  },
  { path: '**', redirectTo: '' }
];
