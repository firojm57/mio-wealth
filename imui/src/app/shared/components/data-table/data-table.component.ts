import { Component, Input, Output, EventEmitter, TemplateRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableColumn, TableAction, TablePagination } from './data-table.model';
import { DashboardService } from '../../../services/dashboard.service';

@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './data-table.component.html'
})
export class DataTableComponent {
  private readonly dashboardService = inject(DashboardService);

  @Input({ required: true }) columns: TableColumn[] = [];
  @Input({ required: true }) data: any[] = [];
  @Input() loading = false;
  @Input() emptyTitle = 'No records found';
  @Input() emptySubtitle = '';
  @Input() emptyIcon = 'icon-investments';
  @Input() actions: TableAction[] = [];
  @Input() pagination: TablePagination | null = null;
  @Input() customTemplates?: { [key: string]: TemplateRef<any> };
  @Input() actionsTemplate: TemplateRef<any> | null = null;
  @Input() rowKey = 'id';
  @Input() currencyCode = this.dashboardService.currencyCode();

  @Output() actionClick = new EventEmitter<{ actionId: string; row: any }>();
  @Output() pageChange = new EventEmitter<number>();
  @Output() pageSizeChange = new EventEmitter<number>();

  get totalColumns(): number {
    return this.columns.length + (this.hasActions ? 1 : 0);
  }

  get hasActions(): boolean {
    return Boolean(this.actionsTemplate || (this.actions && this.actions.length > 0));
  }

  isActionVisible(action: TableAction, row: any): boolean {
    return action.visible ? action.visible(row) : true;
  }

  isActionDisabled(action: TableAction, row: any): boolean {
    return action.disabled ? action.disabled(row) : false;
  }

  getActionClasses(action: TableAction): string {
    const isButton = action.variant === 'button';
    if (isButton) {
      if (action.tone === 'success') {
        return 'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-feedback-success/10 hover:bg-feedback-success/20 text-feedback-success cursor-pointer transition-colors duration-200';
      }
      if (action.tone === 'danger') {
        return 'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-feedback-danger/10 hover:bg-feedback-danger/20 text-feedback-danger cursor-pointer transition-colors duration-200';
      }
      if (action.tone === 'primary') {
        return 'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-accent-primary hover:bg-accent-primary/90 text-card cursor-pointer transition-colors duration-200 shadow-sm';
      }
      return 'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-card border border-border hover:bg-background text-text-primary cursor-pointer transition-colors duration-200 shadow-xs';
    }

    // Icon variant
    if (action.tone === 'danger') {
      return 'p-1 rounded-md text-text-secondary hover:text-feedback-danger hover:bg-feedback-danger/10 cursor-pointer transition-colors duration-200';
    }
    if (action.tone === 'success') {
      return 'p-1 rounded-md text-text-secondary hover:text-feedback-success hover:bg-feedback-success/10 cursor-pointer transition-colors duration-200';
    }
    if (action.tone === 'primary') {
      return 'p-1 rounded-md text-accent-primary hover:bg-accent-primary/10 cursor-pointer transition-colors duration-200';
    }
    return 'p-1 rounded-md text-text-secondary hover:text-text-primary hover:bg-background cursor-pointer transition-colors duration-200';
  }

  getRangeEnd(): number {
    if (!this.pagination) return 0;
    const end = (this.pagination.pageIndex + 1) * this.pagination.pageSize;
    return end > this.pagination.totalElements ? this.pagination.totalElements : end;
  }

  getEffectiveTotalPages(): number {
    if (!this.pagination) return 1;
    if (this.pagination.totalPages != null && this.pagination.totalPages > 0) {
      return this.pagination.totalPages;
    }
    return Math.ceil(this.pagination.totalElements / this.pagination.pageSize) || 1;
  }

  isLastPage(): boolean {
    if (!this.pagination) return true;
    return this.pagination.pageIndex + 1 >= this.getEffectiveTotalPages();
  }

  onAction(actionId: string, row: any): void {
    this.actionClick.emit({ actionId, row });
  }

  onPageChange(page: number): void {
    if (page < 0 || page >= this.getEffectiveTotalPages()) return;
    this.pageChange.emit(page);
  }

  onPageSizeChange(size: number): void {
    this.pageSizeChange.emit(size);
  }

  trackRow(row: any): any {
    return row?.[this.rowKey] ?? row;
  }
}
