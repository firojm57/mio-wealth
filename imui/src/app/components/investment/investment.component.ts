import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';
import { InvestmentService } from '../../services/investment.service';
import { getDomainIcon } from '../../models/category.model';

@Component({
  selector: 'app-investment',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './investment.component.html',
  styleUrl: './investment.component.css'
})
export class InvestmentComponent implements OnInit {
  protected readonly dashboardService = inject(DashboardService);
  protected readonly investmentService = inject(InvestmentService);

  readonly getDomainIcon = getDomainIcon;

  get holdings() {
    return this.investmentService.holdings();
  }

  get totalValue(): number {
    return this.holdings.reduce((sum, h) => sum + (h.amount || 0), 0);
  }

  get totalInvested(): number {
    return this.totalValue;
  }

  ngOnInit(): void {
    this.investmentService.loadHoldings().subscribe();
  }
}
