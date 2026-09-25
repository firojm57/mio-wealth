import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { TagDTO } from '../models/tag.model';

@Injectable({
  providedIn: 'root'
})
export class TagService {
  private readonly http = inject(HttpClient);

  readonly tags = signal<TagDTO[]>([]);
  readonly isLoading = signal<boolean>(false);

  loadTags(domain: string = 'INVESTMENT'): Observable<TagDTO[]> {
    this.isLoading.set(true);
    const url = `${environment.apiPrefix}/tags?domain=${domain}`;

    return this.http.get<TagDTO[]>(url).pipe(
      tap((data) => {
        this.tags.set(data ?? []);
        this.isLoading.set(false);
      }),
      catchError(() => {
        this.isLoading.set(false);
        return of([]);
      })
    );
  }

  createTag(payload: TagDTO): Observable<TagDTO> {
    const url = `${environment.apiPrefix}/tags`;
    return this.http.post<TagDTO>(url, payload).pipe(
      tap((created) => {
        const current = this.tags();
        if (!current.some(t => t.name.toLowerCase() === created.name.toLowerCase())) {
          this.tags.set([...current, created]);
        }
      })
    );
  }
}
