export type TransactionType = 'INCOME' | 'EXPENSE' | 'TRANSFER';

export type TransferKind = 'FAMILY' | 'PEER_LOAN' | 'INTER_ACCOUNT' | 'REMITTANCE';

export type TaxHead =
  | 'SALARY'
  | 'CAPITAL_GAINS_STCG'
  | 'CAPITAL_GAINS_LTCG'
  | 'BUSINESS_PROFESSION'
  | 'OTHER_SOURCES'
  | 'SECTION_80C_DEDUCTION'
  | 'SECTION_80D_DEDUCTION'
  | 'EXEMPT';

export interface TransferDetail {
  source?: string;
  destination?: string;
  transferKind: TransferKind;
}

export interface TaxProfile {
  financialYear: string;
  taxHead?: TaxHead;
  isTaxable: boolean;
}

export interface CashTransaction {
  id: string;
  transactionType: TransactionType;
  title: string;
  amount: number;
  transactionDate: string;
  categoryCode: string;
  categoryName?: string;
  periodStart?: string | null;
  periodEnd?: string | null;
  remarks?: string | null;
  tags?: string | null;
  transferDetail?: TransferDetail | null;
  taxProfile?: TaxProfile | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CashTransactionRequest {
  transactionType: TransactionType;
  title: string;
  amount: number;
  transactionDate: string;
  categoryCode: string;
  periodStart?: string | null;
  periodEnd?: string | null;
  remarks?: string | null;
  tags?: string;
  transferDetail?: TransferDetail | null;
  taxProfile?: TaxProfile | null;
}

export interface CashFlowSummary {
  totalIncome: number;
  totalExpense: number;
  netCashFlow: number;
  savingsRate: number;
  totalTransferInflow: number;
  totalTransferOutflow: number;
}

export interface CashFlowTrendItem {
  month: string;
  income: number;
  expense: number;
  netCashFlow: number;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}
