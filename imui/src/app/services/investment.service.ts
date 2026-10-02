import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { InvestmentDTO, InvestmentHolding, InvestmentSummary, MarkSoldDTO } from '../models/investment.model';

@Injectable({
  providedIn: 'root'
})
export class InvestmentService {
  private readonly http = inject(HttpClient);

  readonly holdings = signal<InvestmentHolding[]>([]);
  readonly summary = signal<InvestmentSummary>({
    totalInvested: 0,
    currentPortfolioValue: 0,
    unrealizedProfitLoss: 0,
    unrealizedProfitLossPercentage: 0,
    realizedProfitLoss: 0,
    realizedProfitLossPercentage: 0,
    profitLossPercentage: 0,
    assetClassesCount: 0,
    totalHoldingsCount: 0,
    activeHoldingsCount: 0,
    soldHoldingsCount: 0
  });
  readonly isLoading = signal<boolean>(false);

  loadHoldings(): Observable<InvestmentHolding[]> {
    this.isLoading.set(true);
    const url = `${environment.apiPrefix}/investments`;

    return this.http.get<InvestmentHolding[]>(url).pipe(
      tap((data) => {
        this.holdings.set(data ?? []);
        this.isLoading.set(false);
      }),
      catchError(() => {
        this.isLoading.set(false);
        this.holdings.set([]);
        return of([]);
      })
    );
  }

  loadSummary(): Observable<InvestmentSummary> {
    const url = `${environment.apiPrefix}/investments/summary`;

    return this.http.get<InvestmentSummary>(url).pipe(
      tap((data) => {
        if (data) {
          this.summary.set(data);
        }
      }),
      catchError(() => {
        return of(this.summary());
      })
    );
  }

  getInvestment(id: string): Observable<InvestmentHolding> {
    const url = `${environment.apiPrefix}/investments/${id}`;
    return this.http.get<InvestmentHolding>(url);
  }

  addInvestment(payload: InvestmentDTO): Observable<InvestmentHolding> {
    const url = `${environment.apiPrefix}/investments`;
    return this.http.post<InvestmentHolding>(url, payload);
  }

  updateInvestment(id: string, payload: InvestmentDTO): Observable<InvestmentHolding> {
    const url = `${environment.apiPrefix}/investments/${id}`;
    return this.http.put<InvestmentHolding>(url, payload);
  }

  deleteInvestment(id: string): Observable<void> {
    const url = `${environment.apiPrefix}/investments/${id}`;
    return this.http.delete<void>(url);
  }

  markAsSold(id: string, payload: MarkSoldDTO): Observable<InvestmentHolding> {
    const url = `${environment.apiPrefix}/investments/${id}/sold`;
    return this.http.patch<InvestmentHolding>(url, payload);
  }

  updatePercentage(id: string, percentChange: number): Observable<InvestmentHolding> {
    const url = `${environment.apiPrefix}/investments/${id}/percentage`;
    return this.http.patch<InvestmentHolding>(url, { percentChange, currentPercentageChange: percentChange });
  }
}
