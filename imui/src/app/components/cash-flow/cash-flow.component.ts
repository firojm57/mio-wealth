import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';
import { CashFlowService } from '../../services/cash-flow.service';
import { CategoryService } from '../../services/category.service';
import { ToastService } from '../../services/toast.service';
import {
  CashTransaction,
  CashTransactionRequest,
  TransactionType,
  TransferKind,
  TaxHead
} from '../../models/cash-flow.model';
import { FinancialDomain, getDomainIcon } from '../../models/category.model';
import { ModalDialogComponent } from '../../shared/components/modal-dialog/modal-dialog.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { TagInputComponent } from '../../shared/components/tag-input/tag-input.component';
import { DataTableComponent } from '../../shared/components/data-table/data-table.component';
import { TableColumn, TableAction, TablePagination } from '../../shared/components/data-table/data-table.model';

@Component({
  selector: 'app-cash-flow',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TranslateModule,
    ModalDialogComponent,
    ConfirmDialogComponent,
    TagInputComponent,
    DataTableComponent
  ],
  templateUrl: './cash-flow.component.html',
  styleUrl: './cash-flow.component.css'
})
export class CashFlowComponent implements OnInit {
  protected readonly dashboardService = inject(DashboardService);
  protected readonly cashFlowService = inject(CashFlowService);
  protected readonly categoryService = inject(CategoryService);
  private readonly toastService = inject(ToastService);

  readonly getDomainIcon = getDomainIcon;

  // Tabs
  readonly tabs: { key: TransactionType; label: string; icon: string }[] = [
    { key: 'INCOME', label: 'Income', icon: 'icon-arrow-up-right' },
    { key: 'EXPENSE', label: 'Expenses', icon: 'icon-expenses' },
    { key: 'TRANSFER', label: 'Transfers', icon: 'icon-cashflow' }
  ];

  // Transfer Kinds & Tax Heads
  readonly transferKinds: TransferKind[] = ['FAMILY', 'PEER_LOAN', 'INTER_ACCOUNT', 'REMITTANCE'];
  readonly taxHeads: TaxHead[] = [
    'SALARY',
    'CAPITAL_GAINS_STCG',
    'CAPITAL_GAINS_LTCG',
    'BUSINESS_PROFESSION',
    'OTHER_SOURCES',
    'SECTION_80C_DEDUCTION',
    'SECTION_80D_DEDUCTION',
    'EXEMPT'
  ];

  // Modal State
  readonly isFormOpen = signal<boolean>(false);
  readonly isDeleteOpen = signal<boolean>(false);
  readonly isSubmitting = signal<boolean>(false);
  readonly isTaxProfileExpanded = signal<boolean>(false);
  readonly formSubmitted = signal<boolean>(false);

  // Selected Target for Edit / Delete
  readonly editingId = signal<string | null>(null);
  readonly deleteTarget = signal<CashTransaction | null>(null);

  // Form Model
  formData: CashTransactionRequest = this.getInitialFormData('INCOME');

  ngOnInit(): void {
    this.refreshAll();
  }

  get summary() {
    return this.cashFlowService.summary();
  }

  get transactionsPage() {
    return this.cashFlowService.transactionsPage();
  }

  get activeTab() {
    return this.cashFlowService.activeTab();
  }

  get categories() {
    return this.categoryService.categories();
  }

  readonly domainCategories = computed(() => {
    const active = this.activeTab;
    return this.categories.filter((c) => c.domain === (active as FinancialDomain));
  });

  readonly tableColumns = computed<TableColumn[]>(() => {
    const tab = this.activeTab;
    const cols: TableColumn[] = [
      { key: 'transactionDate', label: 'Date', type: 'date' },
      { key: 'title', label: 'Title' },
      { key: 'categoryName', label: 'Category', type: 'badge' }
    ];
    if (tab === 'INCOME') {
      cols.push({ key: 'period', label: 'Period Window', type: 'period' });
    } else if (tab === 'TRANSFER') {
      cols.push({ key: 'transferRoute', label: 'Source → Destination' });
      cols.push({ key: 'transferKind', label: 'Transfer Kind' });
    }
    cols.push({ key: 'amount', label: 'Amount' });
    cols.push({ key: 'remarks', label: 'Remarks' });
    return cols;
  });

  readonly tableActions: TableAction<CashTransaction>[] = [
    { id: 'edit', icon: 'icon-edit', title: 'Edit transaction', variant: 'icon' },
    { id: 'delete', icon: 'icon-trash', title: 'Delete transaction', variant: 'icon', tone: 'danger' }
  ];

  readonly tablePagination = computed<TablePagination | null>(() => {
    const p = this.transactionsPage;
    if (!p) return null;
    return {
      pageIndex: p.pageNumber,
      pageSize: p.pageSize,
      totalElements: p.totalElements,
      totalPages: p.totalPages,
      pageSizeOptions: [10, 20, 50]
    };
  });

  onTableAction(event: { actionId: string; row: CashTransaction }): void {
    if (event.actionId === 'edit') {
      this.openEditModal(event.row);
    } else if (event.actionId === 'delete') {
      this.openDeleteModal(event.row);
    }
  }

  selectTab(tab: TransactionType): void {
    this.cashFlowService.setActiveTab(tab);
    this.categoryService.loadCategories(tab as FinancialDomain).subscribe();
  }

  refreshAll(): void {
    this.cashFlowService.loadSummary().subscribe();
    this.cashFlowService.loadTransactions().subscribe();
    this.categoryService.loadCategories(this.activeTab as FinancialDomain).subscribe();
  }

  onSearchChange(query: string): void {
    this.cashFlowService.searchQuery.set(query);
    this.cashFlowService.loadTransactions().subscribe();
  }

  onCategoryFilterChange(categoryCode: string): void {
    this.cashFlowService.selectedCategory.set(categoryCode);
    this.cashFlowService.loadTransactions().subscribe();
  }

  onPageChange(page: number): void {
    this.cashFlowService.loadTransactions(page).subscribe();
  }

  onPageSizeChange(size: number): void {
    this.cashFlowService.pageSize.set(size);
    this.cashFlowService.loadTransactions(0, size).subscribe();
  }

  // Modals
  openCreateModal(): void {
    this.editingId.set(null);
    this.formData = this.getInitialFormData(this.activeTab);
    this.formSubmitted.set(false);
    this.isTaxProfileExpanded.set(false);
    this.isFormOpen.set(true);
  }

  openEditModal(item: CashTransaction): void {
    this.editingId.set(item.id);
    this.formData = {
      transactionType: item.transactionType,
      title: item.title,
      amount: item.amount,
      transactionDate: item.transactionDate,
      categoryCode: item.categoryCode,
      periodStart: item.periodStart ?? null,
      periodEnd: item.periodEnd ?? null,
      remarks: item.remarks ?? null,
      tags: item.tags ?? undefined,
      transferDetail: item.transferDetail
        ? {
            source: item.transferDetail.source,
            destination: item.transferDetail.destination,
            transferKind: item.transferDetail.transferKind
          }
        : null,
      taxProfile: item.taxProfile
        ? {
            financialYear: item.taxProfile.financialYear,
            taxHead: item.taxProfile.taxHead,
            isTaxable: item.taxProfile.isTaxable
          }
        : null
    };
    this.formSubmitted.set(false);
    this.isTaxProfileExpanded.set(Boolean(item.taxProfile));
    this.isFormOpen.set(true);
  }

  closeFormModal(): void {
    this.isFormOpen.set(false);
    this.editingId.set(null);
    this.formSubmitted.set(false);
  }

  openDeleteModal(item: CashTransaction): void {
    this.deleteTarget.set(item);
    this.isDeleteOpen.set(true);
  }

  closeDeleteModal(): void {
    this.isDeleteOpen.set(false);
    this.deleteTarget.set(null);
  }

  toggleTaxProfile(): void {
    this.isTaxProfileExpanded.set(!this.isTaxProfileExpanded());
    if (this.isTaxProfileExpanded() && !this.formData.taxProfile) {
      this.formData.taxProfile = {
        financialYear: this.deriveCurrentFinancialYear(),
        taxHead: this.formData.transactionType === 'INCOME' ? 'SALARY' : 'OTHER_SOURCES',
        isTaxable: this.formData.transactionType === 'INCOME'
      };
    }
  }

  saveTransaction(): void {
    this.formSubmitted.set(true);

    if (!this.formData.title?.trim() || !this.formData.amount || this.formData.amount <= 0 || !this.formData.categoryCode) {
      this.toastService.showToast('Please fill in all required fields.', 'danger');
      return;
    }

    if (this.formData.transactionType === 'TRANSFER') {
      if (!this.formData.transferDetail) {
        this.formData.transferDetail = { transferKind: 'FAMILY' };
      }
    } else {
      this.formData.transferDetail = null;
    }

    this.isSubmitting.set(true);
    const id = this.editingId();

    const request$ = id
      ? this.cashFlowService.updateTransaction(id, this.formData)
      : this.cashFlowService.createTransaction(this.formData);

    request$.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeFormModal();
        this.refreshAll();
        this.toastService.showToast(
          id ? 'Transaction updated successfully!' : 'Transaction created successfully!',
          'success'
        );
      },
      error: () => {
        this.isSubmitting.set(false);
        this.toastService.showToast('Failed to save transaction. Please try again.', 'danger');
      }
    });
  }

  confirmDelete(): void {
    const target = this.deleteTarget();
    if (!target) return;

    this.isSubmitting.set(true);
    this.cashFlowService.deleteTransaction(target.id).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeDeleteModal();
        this.refreshAll();
        this.toastService.showToast('Transaction deleted successfully!', 'success');
      },
      error: () => {
        this.isSubmitting.set(false);
        this.toastService.showToast('Failed to delete transaction.', 'danger');
      }
    });
  }

  private getInitialFormData(type: TransactionType): CashTransactionRequest {
    const today = new Date().toISOString().slice(0, 10);
    return {
      transactionType: type,
      title: '',
      amount: null as unknown as number,
      transactionDate: today,
      categoryCode: '',
      periodStart: type === 'INCOME' ? today.slice(0, 8) + '01' : null,
      periodEnd: type === 'INCOME' ? today : null,
      remarks: '',
      tags: '',
      transferDetail: type === 'TRANSFER' ? { source: '', destination: '', transferKind: 'FAMILY' } : null,
      taxProfile: null
    };
  }

  private deriveCurrentFinancialYear(): string {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    return month >= 4 ? `${year}-${year + 1}` : `${year - 1}-${year}`;
  }
}
