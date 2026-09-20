export interface BalanceMetrics {
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
  equityRatio: number;
}

export interface AssetItem {
  id: string;
  name: string;
  symbol: string;
  domain: string;
  categoryCode: string;
  categoryName: string;
  amount: number;
  quantity: number;
  unitPrice: number;
  tags?: string;
  date?: string;
}

export interface LiabilityItem {
  id: string;
  name: string;
  domain: string;
  categoryCode: string;
  categoryName: string;
  amount: number;
  interestRate: number;
  tags?: string;
  createdAt?: string;
}

export interface LiabilityRequest {
  name: string;
  categoryCode: string;
  amount: number;
  interestRate: number;
  remarks?: string;
  tags?: string;
}

export interface BalanceSummary {
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
  equityRatio: number;
  assets: AssetItem[];
  liabilities: LiabilityItem[];
}
