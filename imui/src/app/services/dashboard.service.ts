import { Injectable, signal } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  readonly activeTab = signal<string>('Overview');
  readonly isDark = signal<boolean>(false);
  readonly mobileMenuOpen = signal<boolean>(false);
  readonly currencyCode = signal<string>(environment.currencyCode || 'INR');
  readonly currencySymbol = signal<string>(environment.currencySymbol || '₹');

  toggleMobileMenu() {
    this.mobileMenuOpen.set(!this.mobileMenuOpen());
  }

  closeMobileMenu() {
    this.mobileMenuOpen.set(false);
  }

  constructor() {
    if (typeof window !== 'undefined') {
      const prefersDark = window.matchMedia ? window.matchMedia('(prefers-color-scheme: dark)').matches : false;
      const savedTheme = localStorage.getItem('theme');
      const dark = savedTheme === 'dark' || (!savedTheme && prefersDark);
      this.setTheme(dark);
    }
  }

  setActiveTab(tabName: string) {
    this.activeTab.set(tabName);
  }

  toggleTheme() {
    this.setTheme(!this.isDark());
  }

  setTheme(dark: boolean) {
    this.isDark.set(dark);
    if (typeof window !== 'undefined') {
      if (dark) {
        document.documentElement.setAttribute('data-theme', 'dark');
        localStorage.setItem('theme', 'dark');
      } else {
        document.documentElement.removeAttribute('data-theme');
        localStorage.setItem('theme', 'light');
      }
    }
  }
}
