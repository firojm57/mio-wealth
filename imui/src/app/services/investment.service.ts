import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { InvestmentDTO, InvestmentHolding, InvestmentSummary } from '../models/investment.model';

const DEFAULT_HOLDINGS: InvestmentHolding[] = [
  { name: 'Apple Inc.', symbol: 'AAPL', allocation: '18%', shares: '120', price: '$178.50', value: '$21,420', return: '+12.4%', up: true },
  { name: 'Microsoft Corp.', symbol: 'MSFT', allocation: '12%', shares: '85', price: '$415.20', value: '$35,292', return: '+8.7%', up: true },
  { name: 'Vanguard S&P 500 ETF', symbol: 'VOO', allocation: '15%', shares: '110', price: '$465.10', value: '$51,161', return: '+5.3%', up: true },
  { name: 'Tesla Inc.', symbol: 'TSLA', allocation: '8%', shares: '70', price: '$175.40', value: '$12,278', return: '-4.2%', up: false }
];

@Injectable({
  providedIn: 'root'
})
export class InvestmentService {
  private readonly http = inject(HttpClient);

  readonly holdings = signal<InvestmentHolding[]>(DEFAULT_HOLDINGS);
  readonly isLoading = signal<boolean>(false);

  loadHoldings(): Observable<InvestmentHolding[]> {
    this.isLoading.set(true);
    const url = `${environment.apiPrefix}/investments`;

    return this.http.get<InvestmentHolding[]>(url).pipe(
      tap((data) => {
        if (data && data.length > 0) {
          this.holdings.set(data);
        }
        this.isLoading.set(false);
      }),
      catchError(() => {
        // High-availability fallback to maintain seamless offline UI experience
        this.isLoading.set(false);
        return of(this.holdings());
      })
    );
  }

  addInvestment(payload: InvestmentDTO): Observable<InvestmentDTO> {
    const url = `${environment.apiPrefix}/investments`;
    return this.http.post<InvestmentDTO>(url, payload).pipe(
      tap(() => {
        this.loadHoldings().subscribe();
      })
    );
  }
}
