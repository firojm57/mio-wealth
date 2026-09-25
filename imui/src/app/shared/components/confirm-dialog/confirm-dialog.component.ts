import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalDialogComponent } from '../modal-dialog/modal-dialog.component';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, ModalDialogComponent],
  templateUrl: './confirm-dialog.component.html'
})
export class ConfirmDialogComponent {
  readonly isOpen = input<boolean>(false);
  readonly title = input<string>('Confirm Action');
  readonly message = input<string>('Are you sure you want to proceed?');
  readonly tone = input<'primary' | 'danger' | 'warning' | 'info'>('primary');
  readonly confirmText = input<string>('Confirm');
  readonly cancelText = input<string>('Cancel');
  readonly isSubmitting = input<boolean>(false);

  readonly confirm = output<void>();
  readonly cancel = output<void>();

  getToneIconContainerClass(): string {
    switch (this.tone()) {
      case 'danger': return 'w-12 h-12 rounded-2xl bg-feedback-danger/10 text-feedback-danger flex items-center justify-center mb-4';
      case 'warning': return 'w-12 h-12 rounded-2xl bg-accent-secondary/10 text-accent-secondary flex items-center justify-center mb-4';
      case 'info': return 'w-12 h-12 rounded-2xl bg-blue-500/10 text-blue-500 flex items-center justify-center mb-4';
      case 'primary':
      default: return 'w-12 h-12 rounded-2xl bg-accent-primary/10 text-accent-primary flex items-center justify-center mb-4';
    }
  }

  getConfirmButtonClass(): string {
    switch (this.tone()) {
      case 'danger':
        return 'px-6 py-2 rounded-full bg-feedback-danger hover:bg-feedback-danger/90 text-white text-xs font-semibold shadow-md shadow-feedback-danger/10 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition-colors';
      case 'warning':
        return 'px-6 py-2 rounded-full bg-accent-secondary hover:bg-accent-secondary/90 text-card text-xs font-semibold shadow-md shadow-accent-secondary/10 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition-colors';
      case 'info':
        return 'px-6 py-2 rounded-full bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold shadow-md cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition-colors';
      case 'primary':
      default:
        return 'px-6 py-2 rounded-full bg-accent-primary hover:bg-accent-primary/90 text-card text-xs font-semibold shadow-md shadow-accent-primary/10 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed transition-colors';
    }
  }
}
