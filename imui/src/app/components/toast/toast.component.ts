import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    @if (toastService.currentToast(); as toast) {
      <div class="fixed bottom-6 right-6 z-50 max-w-md w-full px-4 animate-fade-in pointer-events-auto">
        <div
          class="flex items-center justify-between p-4 rounded-2xl shadow-xl border backdrop-blur-md transition-all duration-300"
          [class.bg-card]="true"
          [class.border-feedback-success]="toast.type === 'success'"
          [class.border-feedback-danger]="toast.type === 'danger'"
          [class.border-border]="toast.type === 'info'"
        >
          <div class="flex items-center space-x-3">
            @if (toast.type === 'success') {
              <div class="w-8 h-8 rounded-full bg-feedback-success/15 text-feedback-success flex items-center justify-center shrink-0">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                </svg>
              </div>
            } @else if (toast.type === 'danger') {
              <div class="w-8 h-8 rounded-full bg-feedback-danger/15 text-feedback-danger flex items-center justify-center shrink-0">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </div>
            } @else {
              <div class="w-8 h-8 rounded-full bg-accent-secondary/15 text-accent-secondary flex items-center justify-center shrink-0">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
              </div>
            }
            <p class="text-sm font-medium text-text-primary">{{ toast.message }}</p>
          </div>

          <button
            type="button"
            (click)="toastService.clearToast()"
            class="ml-4 p-1.5 rounded-lg text-text-secondary hover:text-text-primary hover:bg-border/20 transition-colors cursor-pointer"
            aria-label="Close notification"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
      </div>
    }
  `
})
export class ToastComponent {
  protected readonly toastService = inject(ToastService);
}
