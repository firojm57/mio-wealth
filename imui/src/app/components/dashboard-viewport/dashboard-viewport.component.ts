import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';
import { BalanceService } from '../../services/balance.service';
import { InvestmentService } from '../../services/investment.service';
import { InvestmentDTO, InvestmentHolding } from '../../models/investment.model';
import { getDomainIcon } from '../../models/category.model';

@Component({
  selector: 'app-dashboard-viewport',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './dashboard-viewport.component.html',
  styleUrl: './dashboard-viewport.component.css'
})
export class DashboardViewportComponent implements OnInit {
  protected readonly dashboardService = inject(DashboardService);
  protected readonly balanceService = inject(BalanceService);
  protected readonly investmentService = inject(InvestmentService);

  readonly getDomainIcon = getDomainIcon;

  ngOnInit(): void {
    // Only fetch lightweight summary metrics for dashboard (4 numbers, no fat list!)
    this.balanceService.loadSummary().subscribe();
    this.investmentService.loadHoldings().subscribe();
  }

  get netWorth(): number {
    return this.balanceService.netWorth();
  }

  get totalAssets(): number {
    return this.balanceService.totalAssets();
  }

  get totalLiabilities(): number {
    return this.balanceService.totalLiabilities();
  }

  get holdings(): InvestmentHolding[] {
    return this.investmentService.holdings();
  }

  getAllocation(item: InvestmentHolding): string {
    const total = this.holdings.reduce((sum, h) => sum + (h.amount || 0), 0);
    if (!total || total === 0) return '0%';
    return `${Math.round(((item.amount || 0) / total) * 100)}%`;
  }
}
