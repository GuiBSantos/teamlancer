import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  auth = inject(AuthService);
  private profileService = inject(ProfileService);
  private fb = inject(FormBuilder);

  saving = signal(false);
  saved  = signal(false);
  error  = signal('');

  readonly COLORS = [
    '#1a1a2e', '#16213e', '#0f3460', '#1b262c',
    '#2d2d2d', '#1a3a2a', '#3a1a1a', '#2a1a3a'
  ];

  selectedColor = signal<string>('#2d2d2d');

  form = this.fb.group({
    name:     ['', [Validators.required, Validators.minLength(2)]],
    location: [''],
    bio:      [''],
  });

  readonly initials = computed(() => {
    const name = (this.form.get('name')?.value ?? '') || (this.auth.user()?.name ?? '');
    return name.split(' ').map((w: string) => w[0]).slice(0, 2).join('').toUpperCase();
  });

  ngOnInit() {
    const user = this.auth.user() as any;
    if (user) {
      this.form.patchValue({
        name:     user.name     ?? '',
        location: user.location ?? '',
        bio:      user.bio      ?? '',
      });
      if (user.avatarColor) this.selectedColor.set(user.avatarColor);
    }
  }

  selectColor(c: string) {
    this.selectedColor.set(c);
  }

  submit() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');

    const payload = {
      name:        this.form.value.name     ?? '',
      location:    this.form.value.location ?? '',
      bio:         this.form.value.bio      ?? '',
      avatarColor: this.selectedColor(),
    };

    this.profileService.update(payload).subscribe({
      next: () => {
        this.auth.patchUser(payload);
        this.saving.set(false);
        this.saved.set(true);
        setTimeout(() => this.saved.set(false), 3000);
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Erro ao salvar. Tente novamente.');
      }
    });
  }
}
