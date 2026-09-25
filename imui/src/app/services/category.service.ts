import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { Category, CategoryDTO, FinancialDomain } from '../models/category.model';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly http = inject(HttpClient);

  readonly categories = signal<Category[]>([]);
  readonly isLoading = signal<boolean>(false);

  loadCategories(domain?: FinancialDomain): Observable<Category[]> {
    this.isLoading.set(true);
    const query = domain ? `?domain=${domain}` : '';
    const url = `${environment.apiPrefix}/categories${query}`;

    return this.http.get<Category[]>(url).pipe(
      tap((data) => {
        this.categories.set(data ?? []);
        this.isLoading.set(false);
      }),
      catchError(() => {
        this.isLoading.set(false);
        this.categories.set([]);
        return of([]);
      })
    );
  }

  createCategory(category: CategoryDTO): Observable<Category> {
    const url = `${environment.apiPrefix}/categories`;
    return this.http.post<Category>(url, category).pipe(
      tap((created) => {
        this.categories.update((current) => [...current, created]);
      })
    );
  }
}
