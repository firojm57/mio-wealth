export interface InvestmentHolding {
  id: string;
  symbol: string;
  name: string;
  domain: string;
  categoryCode: string;
  categoryName: string;
  amount: number;
  quantity: number;
  unitPrice: number;
  returnRate: number;
  tags?: string;
  action: string;
  investmentDate: string;
}

export interface InvestmentDTO {
  id?: string;
  symbol: string;
  assetName: string;
  categoryCode: string;
  amount: number;
  quantity: number;
  unitPrice?: number;
  remarks?: string;
  tags?: string;
  action?: string;
}

export interface InvestmentSummary {
  totalInvested: number;
  currentValue: number;
  unrealizedReturnPercent: number;
  holdings: InvestmentHolding[];
}
