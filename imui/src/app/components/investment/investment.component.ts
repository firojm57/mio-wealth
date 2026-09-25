import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslateModule } from '@ngx-translate/core';
import { DashboardService } from '../../services/dashboard.service';
import { InvestmentService } from '../../services/investment.service';
import { CategoryService } from '../../services/category.service';
import { ToastService } from '../../services/toast.service';
import { InvestmentDTO, InvestmentHolding, MarkSoldDTO } from '../../models/investment.model';
import { CategoryDTO, getDomainIcon } from '../../models/category.model';
import { ModalDialogComponent } from '../../shared/components/modal-dialog/modal-dialog.component';
import { ConfirmDialogComponent } from '../../shared/components/confirm-dialog/confirm-dialog.component';
import { TagInputComponent } from '../../shared/components/tag-input/tag-input.component';

@Component({
  selector: 'app-investment',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule, ModalDialogComponent, ConfirmDialogComponent, TagInputComponent],
  templateUrl: './investment.component.html',
  styleUrl: './investment.component.css'
})
export class InvestmentComponent implements OnInit {
  protected readonly dashboardService = inject(DashboardService);
  protected readonly investmentService = inject(InvestmentService);
  protected readonly categoryService = inject(CategoryService);
  private readonly toastService = inject(ToastService);

  readonly getDomainIcon = getDomainIcon;

  // Table Filter State
  readonly tableFilter = signal<'ALL' | 'ACTIVE' | 'SOLD'>('ALL');

  // Modal States
  readonly isFormOpen = signal<boolean>(false);
  readonly isQuickSoldOpen = signal<boolean>(false);
  readonly isUpdatePercentageOpen = signal<boolean>(false);
  readonly isDeleteOpen = signal<boolean>(false);
  readonly isAddCategoryOpen = signal<boolean>(false);
  readonly isSubmitting = signal<boolean>(false);

  // Form Submission Validation States (for inline error displays)
  readonly formSubmitted = signal<boolean>(false);
  readonly quickSoldSubmitted = signal<boolean>(false);
  readonly categorySubmitted = signal<boolean>(false);

  // Selected Holding Targets
  readonly editingId = signal<string | null>(null);
  readonly targetHolding = signal<InvestmentHolding | null>(null);
  readonly quickSoldHolding = signal<InvestmentHolding | null>(null);
  readonly percentageHolding = signal<InvestmentHolding | null>(null);

  // Tab Selection for Create Modal (defaulting to Stocks)
  readonly selectedCategoryTab = signal<string>('STOCKS');

  // Form Models
  formData: InvestmentDTO = this.getInitialFormData();
  quickSoldData: MarkSoldDTO = { sellingPrice: 0, soldDate: this.getTodayDate() };
  percentageData = { percentage: 0 };
  newCategoryData: CategoryDTO = { code: '', name: '', domain: 'INVESTMENT', description: '' };

  get holdings() {
    return this.investmentService.holdings();
  }

  get categories() {
    return this.categoryService.categories();
  }

  // Filtered Holdings
  readonly filteredHoldings = computed(() => {
    const list = this.holdings;
    const filter = this.tableFilter();
    if (filter === 'ACTIVE') return list.filter(h => !h.isSold);
    if (filter === 'SOLD') return list.filter(h => h.isSold);
    return list;
  });

  // KPI Metrics
  readonly activeCount = computed(() => this.holdings.filter(h => !h.isSold).length);
  readonly soldCount = computed(() => this.holdings.filter(h => h.isSold).length);

  readonly totalInvested = computed(() =>
    this.holdings.filter(h => !h.isSold).reduce((sum, h) => sum + (h.buyingPrice || 0), 0)
  );

  readonly totalCurrentValue = computed(() =>
    this.holdings.filter(h => !h.isSold).reduce((sum, h) => sum + (h.currentValue ?? h.buyingPrice ?? 0), 0)
  );

  readonly totalUnrealizedPL = computed(() =>
    this.holdings.filter(h => !h.isSold).reduce((sum, h) => sum + (h.unrealizedProfitLoss ?? 0), 0)
  );

  readonly totalUnrealizedReturnPct = computed(() => {
    const cost = this.totalInvested();
    if (!cost || cost <= 0) return 0;
    return +((this.totalUnrealizedPL() / cost) * 100).toFixed(2);
  });

  readonly realizedProfitLoss = computed(() =>
    this.holdings.filter(h => h.isSold).reduce((sum, h) => sum + (h.profitLoss || 0), 0)
  );

  readonly realizedValue = computed(() =>
    this.holdings.filter(h => h.isSold).reduce((sum, h) => sum + (h.sellingPrice || 0), 0)
  );

  ngOnInit(): void {
    this.refreshData();
  }

  refreshData(): void {
    this.investmentService.loadHoldings().subscribe();
    this.categoryService.loadCategories('INVESTMENT').subscribe({
      next: (cats) => {
        if (cats.length > 0 && !cats.some(c => c.code === this.selectedCategoryTab())) {
          this.selectedCategoryTab.set(cats[0].code);
        }
      }
    });
  }

  // Create & Edit Modal Operations
  openCreateModal(): void {
    this.editingId.set(null);
    this.formSubmitted.set(false);
    this.formData = this.getInitialFormData();

    const stockCat = this.categories.find(c => c.code === 'STOCKS');
    const defaultCode = stockCat ? stockCat.code : (this.categories[0]?.code || 'STOCKS');
    this.selectedCategoryTab.set(defaultCode);
    this.formData.categoryCode = defaultCode;

    this.isFormOpen.set(true);
  }

  openEditModal(holding: InvestmentHolding): void {
    this.editingId.set(holding.id);
    this.formSubmitted.set(false);
    this.selectedCategoryTab.set(holding.categoryCode);
    this.formData = {
      id: holding.id,
      assetName: holding.name,
      categoryCode: holding.categoryCode,
      buyingPrice: holding.buyingPrice,
      quantity: holding.quantity,
      unitPrice: holding.unitPrice,
      investmentDate: holding.investmentDate ? holding.investmentDate.split('T')[0] : this.getTodayDate(),
      isSold: holding.isSold,
      sellingPrice: holding.sellingPrice ?? null,
      soldDate: holding.soldDate ? holding.soldDate.split('T')[0] : null,
      currentPercentageChange: holding.currentPercentageChange ?? 0,
      remarks: holding.remarks || '',
      tags: holding.tags || ''
    };
    this.isFormOpen.set(true);
  }

  closeFormModal(): void {
    this.isFormOpen.set(false);
    this.editingId.set(null);
    this.formSubmitted.set(false);
  }

  selectCategoryTab(categoryCode: string): void {
    this.selectedCategoryTab.set(categoryCode);
    this.formData.categoryCode = categoryCode;
  }

  onQuantityOrUnitPriceChange(): void {
    if (this.formData.quantity && this.formData.unitPrice && this.formData.unitPrice > 0) {
      this.formData.buyingPrice = +(this.formData.quantity * this.formData.unitPrice).toFixed(2);
    }
  }

  onBuyingPriceChange(): void {
    if (this.formData.buyingPrice && this.formData.quantity && this.formData.quantity > 0) {
      this.formData.unitPrice = +(this.formData.buyingPrice / this.formData.quantity).toFixed(2);
    }
  }

  toggleSoldInForm(): void {
    this.formData.isSold = !this.formData.isSold;
    if (this.formData.isSold && !this.formData.soldDate) {
      this.formData.soldDate = this.getTodayDate();
    }
  }

  getFormProfitLoss(): number | null {
    if (!this.formData.isSold || !this.formData.sellingPrice || !this.formData.buyingPrice) {
      return null;
    }
    return +(this.formData.sellingPrice - this.formData.buyingPrice).toFixed(2);
  }

  getFormReturnPercentage(): number | null {
    const pl = this.getFormProfitLoss();
    if (pl === null || !this.formData.buyingPrice || this.formData.buyingPrice <= 0) {
      return null;
    }
    return +((pl / this.formData.buyingPrice) * 100).toFixed(2);
  }

  getFormCurrentValue(): number | null {
    if (!this.formData.buyingPrice || this.formData.buyingPrice <= 0) return null;
    const pct = this.formData.currentPercentageChange ?? 0;
    return +(this.formData.buyingPrice * (1 + pct / 100)).toFixed(2);
  }

  getFormUnrealizedPL(): number | null {
    const curVal = this.getFormCurrentValue();
    if (curVal === null || !this.formData.buyingPrice) return null;
    return +(curVal - this.formData.buyingPrice).toFixed(2);
  }

  saveInvestment(): void {
    this.formSubmitted.set(true);

    if (!this.formData.assetName?.trim() || !this.formData.buyingPrice || this.formData.buyingPrice <= 0 || !this.formData.categoryCode) {
      return;
    }

    if (this.formData.isSold && (!this.formData.sellingPrice || this.formData.sellingPrice <= 0)) {
      return;
    }

    this.isSubmitting.set(true);
    const id = this.editingId();
    const payload: InvestmentDTO = {
      ...this.formData,
      investmentDate: this.formData.investmentDate && !this.formData.investmentDate.includes('T')
        ? `${this.formData.investmentDate}T00:00:00`
        : this.formData.investmentDate,
      soldDate: this.formData.isSold && this.formData.soldDate && !this.formData.soldDate.includes('T')
        ? `${this.formData.soldDate}T00:00:00`
        : (this.formData.isSold ? this.formData.soldDate : null)
    };

    const action$ = id
      ? this.investmentService.updateInvestment(id, payload)
      : this.investmentService.addInvestment(payload);

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
        this.toastService.showToast('Failed to save investment. Please check server status.', 'danger');
      }
    });
  }

  // Quick Action: Mark as Sold Dialog
  openQuickSoldModal(holding: InvestmentHolding): void {
    this.quickSoldHolding.set(holding);
    this.quickSoldSubmitted.set(false);
    this.quickSoldData = {
      sellingPrice: holding.buyingPrice,
      soldDate: this.getTodayDate()
    };
    this.isQuickSoldOpen.set(true);
  }

  closeQuickSoldModal(): void {
    this.isQuickSoldOpen.set(false);
    this.quickSoldHolding.set(null);
    this.quickSoldSubmitted.set(false);
  }

  getQuickSoldProfitLoss(): number {
    const holding = this.quickSoldHolding();
    if (!holding || !this.quickSoldData.sellingPrice) return 0;
    return +(this.quickSoldData.sellingPrice - holding.buyingPrice).toFixed(2);
  }

  getQuickSoldReturnPercentage(): number {
    const holding = this.quickSoldHolding();
    if (!holding || !holding.buyingPrice || holding.buyingPrice <= 0) return 0;
    const pl = this.getQuickSoldProfitLoss();
    return +((pl / holding.buyingPrice) * 100).toFixed(2);
  }

  confirmQuickSold(): void {
    this.quickSoldSubmitted.set(true);
    const holding = this.quickSoldHolding();
    if (!holding || !this.quickSoldData.sellingPrice || this.quickSoldData.sellingPrice <= 0) {
      return;
    }

    this.isSubmitting.set(true);
    const payload: MarkSoldDTO = {
      ...this.quickSoldData,
      soldDate: this.quickSoldData.soldDate && !this.quickSoldData.soldDate.includes('T')
        ? `${this.quickSoldData.soldDate}T00:00:00`
        : this.quickSoldData.soldDate
    };

    this.investmentService.markAsSold(holding.id, payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeQuickSoldModal();
        this.toastService.showToast(`Marked '${holding.name}' as sold.`, 'success');
        this.investmentService.loadHoldings().subscribe();
      },
      error: () => {
        this.isSubmitting.set(false);
        this.toastService.showToast('Failed to record sale. Please try again.', 'danger');
      }
    });
  }

  // Quick Action: Update % Change Dialog
  openUpdatePercentageModal(holding: InvestmentHolding): void {
    this.percentageHolding.set(holding);
    this.percentageData = { percentage: holding.currentPercentageChange ?? 0 };
    this.isUpdatePercentageOpen.set(true);
  }

  closeUpdatePercentageModal(): void {
    this.isUpdatePercentageOpen.set(false);
    this.percentageHolding.set(null);
  }

  getQuickPercentageCurrentValue(): number {
    const holding = this.percentageHolding();
    if (!holding || !holding.buyingPrice) return 0;
    return +(holding.buyingPrice * (1 + (this.percentageData.percentage || 0) / 100)).toFixed(2);
  }

  getQuickPercentageUnrealizedPL(): number {
    const holding = this.percentageHolding();
    if (!holding || !holding.buyingPrice) return 0;
    return +(this.getQuickPercentageCurrentValue() - holding.buyingPrice).toFixed(2);
  }

  confirmUpdatePercentage(): void {
    const holding = this.percentageHolding();
    if (!holding) return;

    this.isSubmitting.set(true);
    this.investmentService.updatePercentage(holding.id, this.percentageData.percentage || 0).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.closeUpdatePercentageModal();
        this.toastService.showToast(`Performance updated for '${holding.name}'.`, 'success');
        this.investmentService.loadHoldings().subscribe();
      },
      error: () => {
        this.isSubmitting.set(false);
        this.toastService.showToast('Failed to update performance. Please try again.', 'danger');
      }
    });
  }

  // Delete Dialog Operations
  openDeleteModal(holding: InvestmentHolding): void {
    this.targetHolding.set(holding);
    this.isDeleteOpen.set(true);
  }

  closeDeleteModal(): void {
    this.isDeleteOpen.set(false);
    this.targetHolding.set(null);
  }

  executeDelete(): void {
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

  // Create Category Modal Operations
  openAddCategoryModal(): void {
    this.categorySubmitted.set(false);
    this.newCategoryData = {
      code: '',
      name: '',
      domain: 'INVESTMENT',
      description: ''
    };
    this.isAddCategoryOpen.set(true);
  }

  closeAddCategoryModal(): void {
    this.isAddCategoryOpen.set(false);
    this.categorySubmitted.set(false);
  }

  onCategoryNameInput(): void {
    if (this.newCategoryData.name) {
      this.newCategoryData.code = this.newCategoryData.name
        .trim()
        .toUpperCase()
        .replace(/[^A-Z0-9]/g, '_');
    }
  }

  saveCustomCategory(): void {
    this.categorySubmitted.set(true);
    if (!this.newCategoryData.name?.trim() || !this.newCategoryData.code?.trim()) {
      return;
    }

    this.isSubmitting.set(true);
    this.categoryService.createCategory(this.newCategoryData).subscribe({
      next: (created) => {
        this.isSubmitting.set(false);
        this.closeAddCategoryModal();
        this.toastService.showToast(`Category '${created.name}' created successfully.`, 'success');
        this.selectCategoryTab(created.code);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.toastService.showToast('Failed to create category. Code may already exist.', 'danger');
      }
    });
  }

  private getInitialFormData(): InvestmentDTO {
    return {
      assetName: '',
      categoryCode: 'STOCKS',
      buyingPrice: 0,
      quantity: 1,
      unitPrice: 0,
      investmentDate: this.getTodayDate(),
      isSold: false,
      sellingPrice: null,
      soldDate: null,
      currentPercentageChange: 0,
      remarks: '',
      tags: ''
    };
  }

  private getTodayDate(): string {
    return new Date().toISOString().split('T')[0];
  }
}
