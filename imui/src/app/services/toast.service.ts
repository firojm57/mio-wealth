import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'info' | 'danger';

export interface ToastMessage {
  id: number;
  message: string;
  type: ToastType;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  readonly currentToast = signal<ToastMessage | null>(null);
  private timer: any = null;
  private nextId = 1;

  showToast(message: string, type: ToastType = 'info'): void {
    this.clearTimer();
    const id = this.nextId++;
    this.currentToast.set({ id, message, type });

    const timeoutMs = type === 'danger' ? 7000 : 4000;
    this.timer = setTimeout(() => {
      if (this.currentToast()?.id === id) {
        this.clearToast();
      }
    }, timeoutMs);
  }

  clearToast(): void {
    this.clearTimer();
    this.currentToast.set(null);
  }

  private clearTimer(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }
}
