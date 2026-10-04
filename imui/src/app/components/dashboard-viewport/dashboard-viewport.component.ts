import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';
import { BalanceService } from '../../services/balance.service';
import { InvestmentService } from '../../services/investment.service';
import { CashFlowService } from '../../services/cash-flow.service';
import { InvestmentHolding } from '../../models/investment.model';
import { CashFlowSummary, CashFlowTrendItem } from '../../models/cash-flow.model';
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
  protected readonly cashFlowService = inject(CashFlowService);

  readonly getDomainIcon = getDomainIcon;

  ngOnInit(): void {
    this.balanceService.loadSummary().subscribe();
    this.investmentService.loadHoldings().subscribe();
    this.cashFlowService.loadSummary().subscribe();
    this.cashFlowService.loadTrends(6).subscribe();
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

  get cashReserves(): number {
    return this.cashFlowSummary?.netCashFlow ?? 0;
  }

  get holdings(): InvestmentHolding[] {
    return this.investmentService.holdings();
  }

  get cashFlowSummary(): CashFlowSummary | null {
    return this.cashFlowService.summary();
  }

  get trends(): CashFlowTrendItem[] {
    return this.cashFlowService.trends();
  }

  get currentMonthLabel(): string {
    return new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
  }
}
