import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslateModule],
  template: `
    <div class="min-h-screen flex items-center justify-center p-4 bg-background text-text-primary transition-colors duration-300">
      <div class="w-full max-w-md bg-card border border-border rounded-3xl p-8 shadow-xl backdrop-blur-md animate-fade-in">
        <!-- Logo & Header -->
        <div class="text-center mb-8">
          <div class="w-14 h-14 bg-accent-secondary rounded-2xl flex items-center justify-center text-white mx-auto shadow-md shadow-accent-secondary/30 mb-4">
            <svg class="w-8 h-8"><use href="assets/icons.svg#icon-logo"></use></svg>
          </div>
          <h1 class="text-2xl font-extrabold tracking-tight text-text-primary">
            Mio <span class="font-semibold text-accent-secondary">Wealth</span>
          </h1>
          <p class="text-xs text-text-secondary mt-1 font-medium">
            Financial Management & Portfolio Platform
          </p>
        </div>

        <!-- Login Form -->
        <form (ngSubmit)="onSubmit()" class="space-y-4">
          <div>
            <label class="block text-xs font-semibold text-text-secondary mb-1.5 uppercase tracking-wider">
              User ID / Account
            </label>
            <input
              type="text"
              name="userId"
              [(ngModel)]="userId"
              placeholder="Enter your User ID"
              class="w-full px-4 py-3 bg-background border rounded-xl text-text-primary placeholder:text-text-secondary/50 focus:outline-none transition-all text-sm font-medium"
              [ngClass]="formSubmitted() && !userId.trim() ? 'border-feedback-danger focus:ring-2 focus:ring-feedback-danger/20' : 'border-border focus:ring-2 focus:ring-accent-primary/20 focus:border-accent-primary'"
            />
            @if (formSubmitted() && !userId.trim()) {
              <p class="text-[11px] font-medium text-feedback-danger mt-1">User ID is required</p>
            }
          </div>

          <div>
            <label class="block text-xs font-semibold text-text-secondary mb-1.5 uppercase tracking-wider">
              Password
            </label>
            <input
              type="password"
              name="password"
              [(ngModel)]="password"
              placeholder="••••••••"
              class="w-full px-4 py-3 bg-background border rounded-xl text-text-primary placeholder:text-text-secondary/50 focus:outline-none transition-all text-sm font-medium"
              [ngClass]="formSubmitted() && !password ? 'border-feedback-danger focus:ring-2 focus:ring-feedback-danger/20' : 'border-border focus:ring-2 focus:ring-accent-primary/20 focus:border-accent-primary'"
            />
            @if (formSubmitted() && !password) {
              <p class="text-[11px] font-medium text-feedback-danger mt-1">Password is required</p>
            }
          </div>

          <button
            type="submit"
            [disabled]="isLoading()"
            class="w-full mt-2 py-3 px-4 bg-text-primary text-card font-semibold text-sm rounded-xl hover:opacity-90 active:scale-98 transition-all cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed shadow-md"
          >
            @if (isLoading()) {
              <span>Signing In...</span>
            } @else {
              <span>Sign In</span>
            }
          </button>
        </form>

        <!-- Redirect to Signup -->
        <div class="mt-8 pt-5 border-t border-border/60 text-center">
          <p class="text-xs text-text-secondary">
            Don't have an account yet?
            <a
              routerLink="/signup"
              class="font-semibold text-accent-secondary hover:underline cursor-pointer ml-1"
            >
              Create an Account
            </a>
          </p>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  userId = '';
  password = '';
  readonly isLoading = signal<boolean>(false);
  readonly formSubmitted = signal<boolean>(false);

  onSubmit(): void {
    this.formSubmitted.set(true);
    if (!this.userId.trim() || !this.password) return;
    this.isLoading.set(true);

    this.authService.login({ userId: this.userId.trim(), password: this.password }).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.router.navigate(['/overview']);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }
}
