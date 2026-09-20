import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { InvestmentDTO, InvestmentHolding, InvestmentSummary } from '../models/investment.model';

@Injectable({
  providedIn: 'root'
})
export class InvestmentService {
  private readonly http = inject(HttpClient);

  readonly holdings = signal<InvestmentHolding[]>([]);
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

  addInvestment(payload: InvestmentDTO): Observable<InvestmentDTO> {
    const url = `${environment.apiPrefix}/investments`;
    return this.http.post<InvestmentDTO>(url, payload).pipe(
      tap(() => {
        this.loadHoldings().subscribe();
      })
    );
  }
}
