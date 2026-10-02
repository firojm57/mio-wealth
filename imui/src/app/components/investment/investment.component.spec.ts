import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { InvestmentComponent } from './investment.component';
import { DashboardService } from '../../services/dashboard.service';
import { InvestmentService } from '../../services/investment.service';

describe('InvestmentComponent', () => {
  let component: InvestmentComponent;
  let fixture: ComponentFixture<InvestmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvestmentComponent, TranslateModule.forRoot()],
      providers: [
        DashboardService,
        InvestmentService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InvestmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the Investment component', () => {
    expect(component).toBeTruthy();
  });

  it('should render empty state when no holdings exist', async () => {
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelectorAll('tbody tr').length).toBe(1);
    expect(compiled.textContent).toContain('No investment records found');
  });

  it('should render table rows for each holding when present', async () => {
    const service = TestBed.inject(InvestmentService);
    service.holdings.set([
      { id: '1', name: 'Stock 1', domain: 'INVESTMENT', categoryCode: 'STOCKS', categoryName: 'Stocks', buyingPrice: 1000, quantity: 1, unitPrice: 1000, investmentDate: '2026-09-01', isSold: false, currentValue: 1100, unrealizedProfitLoss: 100, currentPercentageChange: 10 },
      { id: '2', name: 'Stock 2', domain: 'INVESTMENT', categoryCode: 'STOCKS', categoryName: 'Stocks', buyingPrice: 2000, quantity: 1, unitPrice: 2000, investmentDate: '2026-09-01', isSold: false, currentValue: 2200, unrealizedProfitLoss: 200, currentPercentageChange: 10 }
    ]);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelectorAll('tbody tr').length).toBe(2);
  });

  it('should render Unrealized and Realized Profit / Loss cards with positive values', async () => {
    const service = TestBed.inject(InvestmentService);
    service.summary.set({
      totalInvested: 10000,
      currentPortfolioValue: 12500,
      unrealizedProfitLoss: 2500,
      unrealizedProfitLossPercentage: 25.0,
      realizedProfitLoss: 1500,
      realizedProfitLossPercentage: 15.0,
      profitLossPercentage: 25.0,
      assetClassesCount: 2,
      totalHoldingsCount: 5,
      activeHoldingsCount: 3,
      soldHoldingsCount: 2
    });
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const cards = compiled.querySelectorAll('.grid > div');
    expect(cards.length).toBe(4);

    // Card 3: Unrealized Profit / Loss
    const card3Text = cards[2].textContent ?? '';
    expect(card3Text).toContain('Unrealized Profit / Loss');
    expect(card3Text).toContain('+25.00%');
    expect(card3Text).toContain('Profitable');

    // Card 4: Realized Profit / Loss
    const card4Text = cards[3].textContent ?? '';
    expect(card4Text).toContain('Realized Profit / Loss');
    expect(card4Text).toContain('+15.00%');
    expect(card4Text).toContain('Profitable');
  });

  it('should render deficit styling when unrealized and realized profit/loss are negative', async () => {
    const service = TestBed.inject(InvestmentService);
    service.summary.set({
      totalInvested: 10000,
      currentPortfolioValue: 8000,
      unrealizedProfitLoss: -2000,
      unrealizedProfitLossPercentage: -20.0,
      realizedProfitLoss: -500,
      realizedProfitLossPercentage: -5.0,
      profitLossPercentage: -20.0,
      assetClassesCount: 1,
      totalHoldingsCount: 3,
      activeHoldingsCount: 2,
      soldHoldingsCount: 1
    });
    fixture.detectChanges();
    await fixture.whenStable();

    const compiled = fixture.nativeElement as HTMLElement;
    const cards = compiled.querySelectorAll('.grid > div');

    // Card 3: Unrealized Profit / Loss
    const card3Text = cards[2].textContent ?? '';
    expect(card3Text).toContain('Unrealized Profit / Loss');
    expect(card3Text).toContain('-20.00%');
    expect(card3Text).toContain('Deficit');

    // Card 4: Realized Profit / Loss
    const card4Text = cards[3].textContent ?? '';
    expect(card4Text).toContain('Realized Profit / Loss');
    expect(card4Text).toContain('-5.00%');
    expect(card4Text).toContain('Deficit');
  });
});
