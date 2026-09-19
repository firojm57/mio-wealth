export interface InvestmentHolding {
  name: string;
  symbol: string;
  allocation: string;
  shares: string;
  price: string;
  value: string;
  return: string;
  up: boolean;
}

export interface InvestmentDTO {
  id?: number;
  amount: number;
  quantity: number;
  investmentDate?: string;
  remarks?: string;
  action?: string;
  userId?: string;
  type?: string;
  typeName?: string;
}

export interface InvestmentSummary {
  totalInvested: number;
  currentValue: number;
  unrealizedReturnPercent: number;
  holdings: InvestmentHolding[];
}
