import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';

export interface NavItem {
  name: string;
  tab: string;
  route: string;
  icon: string;
  translationKey: string;
}

@Component({
  selector: 'app-side-menu',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './side-menu.component.html',
  styleUrl: './side-menu.component.css'
})
export class SideMenuComponent {
  protected readonly dashboardService = inject(DashboardService);
  private readonly router = inject(Router);

  readonly navItems: NavItem[] = [
    { name: 'Overview', tab: 'Overview', route: 'overview', icon: 'icon-home', translationKey: 'NAV.OVERVIEW' },
    { name: 'Investments', tab: 'Investments', route: 'investments', icon: 'icon-investments', translationKey: 'NAV.INVESTMENTS' },
    { name: 'Cash Flow', tab: 'Cash Flow', route: 'cash-flow', icon: 'icon-cashflow', translationKey: 'NAV.CASH_FLOW' },
    { name: 'Expenses', tab: 'Expenses', route: 'expenses', icon: 'icon-expenses', translationKey: 'NAV.EXPENSES' },
    { name: 'Performance', tab: 'Performance', route: 'performance', icon: 'icon-performance', translationKey: 'NAV.PERFORMANCE' },
    { name: 'Balance', tab: 'Balance', route: 'balance', icon: 'icon-assets', translationKey: 'NAV.BALANCE' },
    { name: 'Goals', tab: 'Goals', route: 'goals', icon: 'icon-goals', translationKey: 'NAV.GOALS' }
  ];

  constructor() {
    this.syncActiveRoute(this.router.url || '');

    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => {
        this.syncActiveRoute(event.urlAfterRedirects);
      });
  }

  private syncActiveRoute(url: string): void {
    const matched = this.navItems.find((item) => url.includes(item.route));
    if (matched) {
      this.dashboardService.setActiveTab(matched.tab);
    }
  }

  selectItem(item: NavItem): void {
    this.dashboardService.setActiveTab(item.tab);
    this.router.navigate([item.route]);
  }

  selectTab(tabName: string): void {
    const item = this.navItems.find((n) => n.tab === tabName);
    if (item) {
      this.selectItem(item);
    } else {
      this.dashboardService.setActiveTab(tabName);
    }
  }

  isActive(itemOrTab: NavItem | string): boolean {
    const tabName = typeof itemOrTab === 'string' ? itemOrTab : itemOrTab.tab;
    const item = typeof itemOrTab === 'string' ? this.navItems.find((n) => n.tab === itemOrTab) : itemOrTab;
    const route = item?.route || tabName.toLowerCase();
    return (this.router.url ? this.router.url.includes(route) : false) || this.dashboardService.activeTab() === tabName;
  }

  getDesktopIconClass(item: NavItem): string {
    const active = this.isActive(item);
    if (active) {
      return 'bg-text-primary text-card shadow-sm scale-105';
    }
    return 'bg-transparent text-text-secondary hover:text-text-primary';
  }
}
