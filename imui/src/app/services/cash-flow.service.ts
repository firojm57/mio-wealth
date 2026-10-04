import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CashFlowSummary,
  CashFlowTrendItem,
  CashTransaction,
  CashTransactionRequest,
  PageResponse,
  TransactionType
} from '../models/cash-flow.model';

@Injectable({
  providedIn: 'root'
})
export class CashFlowService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiPrefix}/cash-flow`;

  // Signals
  readonly activeTab = signal<TransactionType>('INCOME');
  readonly summary = signal<CashFlowSummary | null>(null);
  readonly transactionsPage = signal<PageResponse<CashTransaction> | null>(null);
  readonly trends = signal<CashFlowTrendItem[]>([]);
  readonly loading = signal<boolean>(false);
  readonly currentPage = signal<number>(0);
  readonly pageSize = signal<number>(20);
  readonly selectedCategory = signal<string>('ALL');
  readonly searchQuery = signal<string>('');
  readonly selectedMonth = signal<string>(new Date().toISOString().slice(0, 7));

  setActiveTab(tab: TransactionType): void {
    this.activeTab.set(tab);
    this.currentPage.set(0);
    this.selectedCategory.set('ALL');
    this.searchQuery.set('');
    this.loadTransactions(0, this.pageSize(), tab, 'ALL', '').subscribe();
  }

  loadSummary(year?: number, month?: number): Observable<CashFlowSummary> {
    let params = new HttpParams();
    if (year) params = params.set('year', year.toString());
    if (month) params = params.set('month', month.toString());

    return this.http.get<CashFlowSummary>(`${this.baseUrl}/summary`, { params }).pipe(
      tap((res) => this.summary.set(res))
    );
  }

  loadTrends(months = 6): Observable<CashFlowTrendItem[]> {
    const params = new HttpParams().set('months', months.toString());
    return this.http.get<CashFlowTrendItem[]>(`${this.baseUrl}/trends`, { params }).pipe(
      tap((res) => this.trends.set(res))
    );
  }

  loadTransactions(
    page = this.currentPage(),
    size = this.pageSize(),
    type = this.activeTab(),
    category = this.selectedCategory(),
    search = this.searchQuery()
  ): Observable<PageResponse<CashTransaction>> {
    this.loading.set(true);
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('type', type);

    if (category && category !== 'ALL') {
      params = params.set('category', category);
    }
    if (search && search.trim().length > 0) {
      params = params.set('search', search.trim());
    }

    return this.http.get<PageResponse<CashTransaction>>(`${this.baseUrl}/transactions`, { params }).pipe(
      tap({
        next: (res) => {
          this.transactionsPage.set(res);
          this.currentPage.set(res.pageNumber);
          this.pageSize.set(res.pageSize);
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
        }
      })
    );
  }

  createTransaction(request: CashTransactionRequest): Observable<CashTransaction> {
    return this.http.post<CashTransaction>(`${this.baseUrl}/transactions`, request).pipe(
      tap(() => {
        this.loadTransactions().subscribe();
        this.loadSummary().subscribe();
      })
    );
  }

  updateTransaction(id: string, request: CashTransactionRequest): Observable<CashTransaction> {
    return this.http.put<CashTransaction>(`${this.baseUrl}/transactions/${id}`, request).pipe(
      tap(() => {
        this.loadTransactions().subscribe();
        this.loadSummary().subscribe();
      })
    );
  }

  deleteTransaction(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/transactions/${id}`).pipe(
      tap(() => {
        this.loadTransactions().subscribe();
        this.loadSummary().subscribe();
      })
    );
  }
}
