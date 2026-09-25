import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { InvestmentDTO, InvestmentHolding, MarkSoldDTO } from '../models/investment.model';

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

  updatePercentage(id: string, currentPercentageChange: number): Observable<InvestmentHolding> {
    const url = `${environment.apiPrefix}/investments/${id}/percentage`;
    return this.http.patch<InvestmentHolding>(url, { currentPercentageChange });
  }
}
