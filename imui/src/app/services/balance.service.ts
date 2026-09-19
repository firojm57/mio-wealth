import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { AssetItem, BalanceSummary, LiabilityItem } from '../models/balance.model';

const DEFAULT_ASSETS: AssetItem[] = [
  { name: 'Cash & Savings', category: 'Liquid', value: 120150, change: '+1.2%', icon: 'icon-cashflow' },
  { name: 'Brokerage & Stocks', category: 'Investment', value: 452300, change: '+6.4%', icon: 'icon-investments' },
  { name: 'Real Estate Portfolio', category: 'Property', value: 1223200, change: '+0.8%', icon: 'icon-home' }
];

const DEFAULT_LIABILITIES: LiabilityItem[] = [
  { name: 'Home Mortgage', category: 'Secured Debt', value: 512140, rate: '3.85%', icon: 'icon-home' },
  { name: 'Student & Car Loans', category: 'Unsecured', value: 31200, rate: '4.5%', icon: 'icon-liabilities' },
  { name: 'Credit Cards Balance', category: 'Revolving', value: 4000, rate: '14.99%', icon: 'icon-expenses' }
];

@Injectable({
  providedIn: 'root'
})
export class BalanceService {
  private readonly http = inject(HttpClient);

  readonly assets = signal<AssetItem[]>(DEFAULT_ASSETS);
  readonly liabilities = signal<LiabilityItem[]>(DEFAULT_LIABILITIES);
  readonly isLoading = signal<boolean>(false);

  readonly totalAssets = computed(() =>
    this.assets().reduce((sum, item) => sum + item.value, 0)
  );

  readonly totalLiabilities = computed(() =>
    this.liabilities().reduce((sum, item) => sum + item.value, 0)
  );

  readonly netWorth = computed(() =>
    this.totalAssets() - this.totalLiabilities()
  );

  readonly equityRatio = computed(() => {
    const assets = this.totalAssets();
    return assets === 0 ? 0 : (this.netWorth() / assets) * 100;
  });

  loadBalanceSummary(): Observable<BalanceSummary> {
    this.isLoading.set(true);
    const url = `${environment.apiPrefix}/balance`;

    return this.http.get<BalanceSummary>(url).pipe(
      tap((data) => {
        if (data.assets && data.assets.length > 0) {
          this.assets.set(data.assets);
        }
        if (data.liabilities && data.liabilities.length > 0) {
          this.liabilities.set(data.liabilities);
        }
        this.isLoading.set(false);
      }),
      catchError(() => {
        // High-availability fallback
        this.isLoading.set(false);
        return of({
          totalAssets: this.totalAssets(),
          totalLiabilities: this.totalLiabilities(),
          netWorth: this.netWorth(),
          equityRatio: this.equityRatio(),
          assets: this.assets(),
          liabilities: this.liabilities()
        });
      })
    );
  }
}
