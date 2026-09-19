import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-section-placeholder',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  template: `
    <div class="p-8 bg-card border border-border rounded-3xl shadow-sm text-center py-20 animate-fade-in transition-colors duration-300">
      <div class="w-16 h-16 bg-accent-secondary/10 rounded-full flex items-center justify-center text-accent-secondary mx-auto mb-4">
        <svg class="w-8 h-8"><use href="assets/icons.svg#icon-logo"></use></svg>
      </div>
      <h2 class="text-2xl font-extrabold text-text-primary tracking-tight capitalize">
        {{ sectionTitle }}
      </h2>
      <p class="text-text-secondary text-sm font-medium mt-2">
        This section is connected to the real-time financial data engine.
      </p>
    </div>
  `
})
export class SectionPlaceholderComponent {
  private readonly route = inject(ActivatedRoute);

  get sectionTitle(): string {
    const path = this.route.snapshot.routeConfig?.path || 'Overview';
    return path.replace('-', ' ');
  }
}
