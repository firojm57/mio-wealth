import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { forkJoin } from 'rxjs';
import { DashboardService } from '../../services/dashboard.service';
import { BalanceService } from '../../services/balance.service';
import { getDomainIcon } from '../../models/category.model';

@Component({
  selector: 'app-balance',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './balance.component.html',
  styleUrl: './balance.component.css'
})
export class BalanceComponent implements OnInit {
  protected readonly dashboardService = inject(DashboardService);
  protected readonly balanceService = inject(BalanceService);

  readonly getDomainIcon = getDomainIcon;

  get assets() {
    return this.balanceService.assets();
  }

  get liabilities() {
    return this.balanceService.liabilities();
  }

  ngOnInit(): void {
    // Load summary, assets, and liabilities concurrently via independent endpoints
    forkJoin([
      this.balanceService.loadSummary(),
      this.balanceService.loadAssets(),
      this.balanceService.loadLiabilities()
    ]).subscribe();
  }

  getTotalAssets(): number {
    return this.balanceService.totalAssets();
  }

  getTotalLiabilities(): number {
    return this.balanceService.totalLiabilities();
  }

  getNetWorth(): number {
    return this.balanceService.netWorth();
  }

  getEquityRatio(): number {
    return this.balanceService.equityRatio();
  }
}
