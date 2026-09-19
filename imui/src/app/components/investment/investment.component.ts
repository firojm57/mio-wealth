import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';
import { InvestmentService } from '../../services/investment.service';

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

  get holdings() {
    return this.investmentService.holdings();
  }

  ngOnInit(): void {
    this.investmentService.loadHoldings().subscribe();
  }
}
