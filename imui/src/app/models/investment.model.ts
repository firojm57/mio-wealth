export interface InvestmentHolding {
  id: string;
  name: string;
  symbol?: string;
  domain: string;
  categoryCode: string;
  categoryName: string;
  buyingPrice: number;
  amount?: number;
  quantity: number;
  unitPrice: number;
  investmentDate: string;
  isSold: boolean;
  sellingPrice?: number | null;
  soldDate?: string | null;
  profitLoss?: number | null;
  returnPercentage?: number | null;
  currentPercentageChange?: number | null;
  currentValue?: number | null;
  unrealizedProfitLoss?: number | null;
  remarks?: string;
  tags?: string;
}

export interface InvestmentDTO {
  id?: string;
  assetName: string;
  categoryCode: string;
  buyingPrice: number;
  quantity?: number;
  unitPrice?: number;
  investmentDate?: string;
  isSold?: boolean;
  sellingPrice?: number | null;
  soldDate?: string | null;
  currentPercentageChange?: number | null;
  remarks?: string;
  tags?: string;
}

export interface MarkSoldDTO {
  sellingPrice: number;
  soldDate?: string;
}

export interface InvestmentSummary {
  totalInvested: number;
  activeInvested: number;
  realizedProfitLoss: number;
  holdings: InvestmentHolding[];
}
