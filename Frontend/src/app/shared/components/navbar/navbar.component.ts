import { Component, inject, signal, HostListener, ElementRef, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  auth  = inject(AuthService);
  theme = inject(ThemeService);
  private el = inject(ElementRef);

  menuOpen = signal(false);

  readonly initials = computed(() => {
    const name = this.auth.user()?.name ?? '';
    return name.split(' ').map((w: string) => w[0]).slice(0, 2).join('').toUpperCase();
  });

  readonly avatarBg = computed(() => {
    return (this.auth.user() as any)?.avatarColor ?? '#2D2D2D';
  });

  toggleMenu() { this.menuOpen.update(v => !v); }
  closeMenu()  { this.menuOpen.set(false); }

  logout() {
    this.closeMenu();
    this.auth.logout();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    if (!this.el.nativeElement.contains(event.target)) {
      this.closeMenu();
    }
  }
}
