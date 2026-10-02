import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { AssetItem, BalanceMetrics, BalanceSummary, LiabilityItem, LiabilityRequest } from '../models/balance.model';

@Injectable({
  providedIn: 'root'
})
export class BalanceService {
  private readonly http = inject(HttpClient);

  readonly summary = signal<BalanceMetrics | null>(null);
  readonly assets = signal<AssetItem[]>([]);
  readonly liabilities = signal<LiabilityItem[]>([]);
  readonly isLoading = signal<boolean>(false);

  readonly totalAssets = computed(() => this.summary()?.totalAssets ?? 0);
  readonly totalLiabilities = computed(() => this.summary()?.totalLiabilities ?? 0);
  readonly netWorth = computed(() => this.summary()?.netWorth ?? 0);
  readonly equityRatio = computed(() => this.summary()?.equityRatio ?? 0);

  loadSummary(): Observable<BalanceMetrics> {
    const url = `${environment.apiPrefix}/balance/summary`;
    return this.http.get<BalanceMetrics>(url).pipe(
      tap((data) => {
        this.summary.set(data);
      }),
      catchError(() => {
        const fallback = { totalAssets: 0, totalLiabilities: 0, netWorth: 0, equityRatio: 0 };
        this.summary.set(fallback);
        return of(fallback);
      })
    );
  }

  loadAssets(): Observable<AssetItem[]> {
    const url = `${environment.apiPrefix}/balance/assets`;
    return this.http.get<AssetItem[]>(url).pipe(
      tap((data) => {
        this.assets.set(data ?? []);
      }),
      catchError(() => {
        this.assets.set([]);
        return of([]);
      })
    );
  }

  loadLiabilities(): Observable<LiabilityItem[]> {
    const url = `${environment.apiPrefix}/balance/liabilities`;
    return this.http.get<LiabilityItem[]>(url).pipe(
      tap((data) => {
        this.liabilities.set(data ?? []);
      }),
      catchError(() => {
        this.liabilities.set([]);
        return of([]);
      })
    );
  }

  addLiability(payload: LiabilityRequest): Observable<LiabilityItem> {
    const url = `${environment.apiPrefix}/balance/liabilities`;
    return this.http.post<LiabilityItem>(url, payload).pipe(
      tap(() => {
        this.loadLiabilities().subscribe();
        this.loadSummary().subscribe();
      })
    );
  }

  deleteLiability(id: string): Observable<void> {
    const url = `${environment.apiPrefix}/balance/liabilities/${id}`;
    return this.http.delete<void>(url).pipe(
      tap(() => {
        this.loadLiabilities().subscribe();
        this.loadSummary().subscribe();
      })
    );
  }

  loadBalanceSummary(): Observable<BalanceSummary> {
    this.isLoading.set(true);
    const url = `${environment.apiPrefix}/balance`;

    return this.http.get<BalanceSummary>(url).pipe(
      tap((data) => {
        this.assets.set(data?.assets ?? []);
        this.liabilities.set(data?.liabilities ?? []);
        this.summary.set({
          totalAssets: data?.totalAssets ?? 0,
          totalLiabilities: data?.totalLiabilities ?? 0,
          netWorth: data?.netWorth ?? 0,
          equityRatio: data?.equityRatio ?? 0
        });
        this.isLoading.set(false);
      }),
      catchError(() => {
        this.isLoading.set(false);
        this.assets.set([]);
        this.liabilities.set([]);
        this.summary.set({ totalAssets: 0, totalLiabilities: 0, netWorth: 0, equityRatio: 0 });
        return of({
          totalAssets: 0,
          totalLiabilities: 0,
          netWorth: 0,
          equityRatio: 0,
          assets: [],
          liabilities: []
        });
      })
    );
  }
}
