import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';
import { InvestmentService } from '../../services/investment.service';
import { CategoryService } from '../../services/category.service';
import { ToastService } from '../../services/toast.service';
import { InvestmentDTO, InvestmentHolding } from '../../models/investment.model';
import { getDomainIcon } from '../../models/category.model';

@Component({
  selector: 'app-investment',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './investment.component.html',
  styleUrl: './investment.component.css'
})
export class InvestmentComponent implements OnInit {
  protected readonly dashboardService = inject(DashboardService);
  protected readonly investmentService = inject(InvestmentService);
  protected readonly categoryService = inject(CategoryService);
  private readonly toastService = inject(ToastService);

  readonly getDomainIcon = getDomainIcon;

  // Modal State
  readonly isFormOpen = signal<boolean>(false);
  readonly isDeleteOpen = signal<boolean>(false);
  readonly isSubmitting = signal<boolean>(false);
  readonly editingId = signal<string | null>(null);
  readonly targetHolding = signal<InvestmentHolding | null>(null);

  // Form Model
  formData: InvestmentDTO = this.getInitialFormData();

  get holdings() {
    return this.investmentService.holdings();
  }

  get categories() {
    return this.categoryService.categories();
  }

  get totalValue(): number {
    return this.holdings.reduce((sum, h) => sum + (h.amount || 0), 0);
  }

  get totalInvested(): number {
    return this.totalValue;
  }

  ngOnInit(): void {
    this.refreshData();
  }

  refreshData(): void {
    this.investmentService.loadHoldings().subscribe();
    this.categoryService.loadCategories('INVESTMENT').subscribe();
  }

  openCreateModal(): void {
    this.editingId.set(null);
    this.formData = this.getInitialFormData();
    if (this.categories.length > 0) {
      this.formData.categoryCode = this.categories[0].code;
    }
    this.isFormOpen.set(true);
  }

  openEditModal(holding: InvestmentHolding): void {
    this.editingId.set(holding.id);
    this.formData = {
      id: holding.id,
      symbol: holding.symbol,
      assetName: holding.name,
      categoryCode: holding.categoryCode,
      amount: holding.amount,
      quantity: holding.quantity,
      unitPrice: holding.unitPrice,
      tags: holding.tags || '',
      action: holding.action || 'BUY'
    };
    this.isFormOpen.set(true);
  }

  closeFormModal(): void {
    this.isFormOpen.set(false);
    this.editingId.set(null);
  }

  openDeleteModal(holding: InvestmentHolding): void {
    this.targetHolding.set(holding);
    this.isDeleteOpen.set(true);
  }

  closeDeleteModal(): void {
    this.isDeleteOpen.set(false);
    this.targetHolding.set(null);
  }

  onAmountOrQuantityChange(): void {
    if (this.formData.amount && this.formData.quantity && this.formData.quantity > 0) {
      this.formData.unitPrice = +(this.formData.amount / this.formData.quantity).toFixed(2);
    }
  }

  saveInvestment(): void {
    if (!this.formData.symbol?.trim() || !this.formData.assetName?.trim() || !this.formData.amount || !this.formData.categoryCode) {
      this.toastService.showToast('Please fill in all required fields.', 'danger');
      return;
    }

    this.isSubmitting.set(true);
    const id = this.editingId();
    const action$ = id
      ? this.investmentService.updateInvestment(id, this.formData)
      : this.investmentService.addInvestment(this.formData);

    action$.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeFormModal();
        this.toastService.showToast(
          id ? 'Investment updated successfully.' : 'Investment added successfully.',
          'success'
        );
        this.investmentService.loadHoldings().subscribe();
      },
      error: () => {
        this.isSubmitting.set(false);
        this.toastService.showToast('Failed to save investment. Please try again.', 'danger');
      }
    });
  }

  confirmDelete(): void {
    const holding = this.targetHolding();
    if (!holding) return;

    this.isSubmitting.set(true);
    this.investmentService.deleteInvestment(holding.id).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeDeleteModal();
        this.toastService.showToast('Investment removed successfully.', 'success');
        this.investmentService.loadHoldings().subscribe();
      },
      error: () => {
        this.isSubmitting.set(false);
        this.toastService.showToast('Failed to delete investment.', 'danger');
      }
    });
  }

  private getInitialFormData(): InvestmentDTO {
    return {
      symbol: '',
      assetName: '',
      categoryCode: this.categories[0]?.code || 'STOCKS',
      amount: 0,
      quantity: 1,
      unitPrice: 0,
      tags: '',
      action: 'BUY'
    };
  }
}
