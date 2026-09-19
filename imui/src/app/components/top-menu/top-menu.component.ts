import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-top-menu',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './top-menu.component.html',
  styleUrl: './top-menu.component.css'
})
export class TopMenuComponent {
  protected readonly dashboardService = inject(DashboardService);
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  showProfileDropdown = false;

  toggleProfileDropdown(): void {
    this.showProfileDropdown = !this.showProfileDropdown;
  }

  closeDropdown(): void {
    this.showProfileDropdown = false;
  }

  selectSettings(): void {
    this.dashboardService.setActiveTab('Settings');
    this.router.navigate(['/settings']);
    this.closeDropdown();
  }

  signOut(): void {
    this.closeDropdown();
    this.authService.logout();
  }

  getUserInitials(): string {
    const user = this.authService.currentUser();
    if (!user) return 'MW';
    const first = user.firstName ? user.firstName[0].toUpperCase() : '';
    const last = user.lastName ? user.lastName[0].toUpperCase() : '';
    return first + last || 'MW';
  }

  getUserDisplayName(): string {
    const user = this.authService.currentUser();
    if (!user) return 'Mio Investor';
    return `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Mio Investor';
  }

  getUserEmail(): string {
    const user = this.authService.currentUser();
    return user?.email || 'investor@miowealth.local';
  }
}
