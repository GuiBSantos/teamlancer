import { Injectable, signal, effect } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private _dark = signal<boolean>(this.loadPreference());
  readonly isDark = this._dark.asReadonly();

  constructor() {
    effect(() => {
      const dark = this._dark();
      document.documentElement.classList.toggle('dark', dark);
      localStorage.setItem('tl_theme', dark ? 'dark' : 'light');
    });
  }

  toggle() {
    this._dark.update(v => !v);
  }

  private loadPreference(): boolean {
    const saved = localStorage.getItem('tl_theme');
    if (saved) return saved === 'dark';
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
}
