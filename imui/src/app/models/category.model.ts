export type FinancialDomain = 'INVESTMENT' | 'SAVING' | 'EXPENSE' | 'LIABILITY' | 'INCOME' | 'TRANSFER';

export interface Category {
  code: string;
  name: string;
  domain: FinancialDomain;
  description?: string;
  isCustom?: boolean;
}

export interface CategoryDTO {
  code: string;
  name: string;
  domain: FinancialDomain;
  description?: string;
}

export const DOMAIN_ICONS: Record<string, string> = {
  INVESTMENT: 'icon-investments',
  SAVING: 'icon-cashflow',
  LIABILITY: 'icon-liabilities',
  EXPENSE: 'icon-expenses',
  INCOME: 'icon-cashflow',
  TRANSFER: 'icon-cashflow'
};

export function getDomainIcon(domain?: string): string {
  return (domain && DOMAIN_ICONS[domain]) || 'icon-cashflow';
}
