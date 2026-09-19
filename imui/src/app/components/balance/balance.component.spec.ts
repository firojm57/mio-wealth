import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { BalanceComponent } from './balance.component';
import { DashboardService } from '../../services/dashboard.service';
import { BalanceService } from '../../services/balance.service';

describe('BalanceComponent', () => {
  let component: BalanceComponent;
  let fixture: ComponentFixture<BalanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BalanceComponent, TranslateModule.forRoot()],
      providers: [
        DashboardService,
        BalanceService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BalanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the Balance component', () => {
    expect(component).toBeTruthy();
  });

  it('should calculate total assets correctly', () => {
    expect(component.getTotalAssets()).toBe(1795650);
  });

  it('should calculate total liabilities correctly', () => {
    expect(component.getTotalLiabilities()).toBe(547340);
  });

  it('should calculate net worth and equity ratio correctly', () => {
    expect(component.getNetWorth()).toBe(1248310);
    expect(component.getEquityRatio()).toBeCloseTo(69.518, 2);
  });
});
