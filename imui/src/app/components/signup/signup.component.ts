import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TranslateModule],
  template: `
    <div class="min-h-screen flex items-center justify-center p-4 bg-background text-text-primary transition-colors duration-300">
      <div class="w-full max-w-md bg-card border border-border rounded-3xl p-8 shadow-xl backdrop-blur-md animate-fade-in my-8">
        <!-- Logo & Header -->
        <div class="text-center mb-6">
          <div class="w-14 h-14 bg-accent-secondary rounded-2xl flex items-center justify-center text-white mx-auto shadow-md shadow-accent-secondary/30 mb-4">
            <svg class="w-8 h-8"><use href="assets/icons.svg#icon-logo"></use></svg>
          </div>
          <h1 class="text-2xl font-extrabold tracking-tight text-text-primary">
            Create an <span class="font-semibold text-accent-secondary">Account</span>
          </h1>
          <p class="text-xs text-text-secondary mt-1 font-medium">
            Start managing your wealth, assets, and investments.
          </p>
        </div>

        <!-- Registration Form -->
        <form (ngSubmit)="onSubmit()" class="space-y-3.5">
          <!-- Name Row -->
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="block text-[11px] font-semibold text-text-secondary mb-1 uppercase tracking-wider">
                First Name *
              </label>
              <input
                type="text"
                name="firstName"
                [(ngModel)]="firstName"
                required
                placeholder="e.g. Alex"
                class="w-full px-3.5 py-2.5 bg-background border border-border rounded-xl text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-2 focus:ring-accent-primary/20 transition-all text-xs font-medium"
              />
            </div>
            <div>
              <label class="block text-[11px] font-semibold text-text-secondary mb-1 uppercase tracking-wider">
                Last Name *
              </label>
              <input
                type="text"
                name="lastName"
                [(ngModel)]="lastName"
                required
                placeholder="e.g. Smith"
                class="w-full px-3.5 py-2.5 bg-background border border-border rounded-xl text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-2 focus:ring-accent-primary/20 transition-all text-xs font-medium"
              />
            </div>
          </div>

          <!-- Email -->
          <div>
            <label class="block text-[11px] font-semibold text-text-secondary mb-1 uppercase tracking-wider">
              Email Address *
            </label>
            <input
              type="email"
              name="email"
              [(ngModel)]="email"
              required
              placeholder="e.g. user@example.com"
              class="w-full px-3.5 py-2.5 bg-background border border-border rounded-xl text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-2 focus:ring-accent-primary/20 transition-all text-xs font-medium"
            />
          </div>

          <!-- User ID -->
          <div>
            <label class="block text-[11px] font-semibold text-text-secondary mb-1 uppercase tracking-wider">
              User ID / Username *
            </label>
            <input
              type="text"
              name="userId"
              [(ngModel)]="userId"
              required
              placeholder="e.g. alex_smith"
              class="w-full px-3.5 py-2.5 bg-background border border-border rounded-xl text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-2 focus:ring-accent-primary/20 transition-all text-xs font-medium"
            />
          </div>

          <!-- Password -->
          <div>
            <label class="block text-[11px] font-semibold text-text-secondary mb-1 uppercase tracking-wider">
              Password *
            </label>
            <input
              type="password"
              name="password"
              [(ngModel)]="password"
              required
              placeholder="At least 6 characters"
              class="w-full px-3.5 py-2.5 bg-background border border-border rounded-xl text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-2 focus:ring-accent-primary/20 transition-all text-xs font-medium"
            />
          </div>

          <!-- Confirm Password -->
          <div>
            <label class="block text-[11px] font-semibold text-text-secondary mb-1 uppercase tracking-wider">
              Confirm Password *
            </label>
            <input
              type="password"
              name="confirmPassword"
              [(ngModel)]="confirmPassword"
              required
              placeholder="Repeat your password"
              class="w-full px-3.5 py-2.5 bg-background border border-border rounded-xl text-text-primary placeholder:text-text-secondary/50 focus:outline-none focus:ring-2 focus:ring-accent-primary/20 transition-all text-xs font-medium"
            />
          </div>

          <!-- Submit Button -->
          <button
            type="submit"
            [disabled]="isLoading() || !isFormValid()"
            class="w-full mt-4 py-3 px-4 bg-text-primary text-card font-semibold text-sm rounded-xl hover:opacity-90 active:scale-98 transition-all cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed shadow-md"
          >
            @if (isLoading()) {
              <span>Creating Account...</span>
            } @else {
              <span>Create Account</span>
            }
          </button>
        </form>

        <!-- Redirect to Login -->
        <div class="mt-6 pt-5 border-t border-border/60 text-center">
          <p class="text-xs text-text-secondary">
            Already have an account?
            <a
              routerLink="/login"
              class="font-semibold text-accent-secondary hover:underline cursor-pointer ml-1"
            >
              Sign In
            </a>
          </p>
        </div>
      </div>
    </div>
  `
})
export class SignupComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);

  firstName = '';
  lastName = '';
  email = '';
  userId = '';
  password = '';
  confirmPassword = '';

  readonly isLoading = signal<boolean>(false);

  isFormValid(): boolean {
    return (
      !!this.firstName.trim() &&
      !!this.lastName.trim() &&
      !!this.email.trim() &&
      !!this.userId.trim() &&
      this.password.length >= 6 &&
      this.password === this.confirmPassword
    );
  }

  onSubmit(): void {
    if (!this.isFormValid()) {
      if (this.password !== this.confirmPassword) {
        this.toastService.showToast('Passwords do not match. Please re-enter.', 'danger');
      } else if (this.password.length < 6) {
        this.toastService.showToast('Password must be at least 6 characters long.', 'danger');
      } else {
        this.toastService.showToast('Please complete all required fields.', 'danger');
      }
      return;
    }

    this.isLoading.set(true);

    this.authService
      .register({
        userId: this.userId.trim(),
        password: this.password,
        firstName: this.firstName.trim(),
        lastName: this.lastName.trim(),
        email: this.email.trim()
      })
      .subscribe({
        next: () => {
          this.isLoading.set(false);
          this.toastService.showToast('Account created successfully! Please sign in.', 'success');
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.isLoading.set(false);
          const msg = error.error?.message || 'Failed to create account. User ID may already exist.';
          this.toastService.showToast(msg, 'danger');
        }
      });
  }
}
