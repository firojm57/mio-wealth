export interface AssetItem {
  name: string;
  category: string;
  value: number;
  change: string;
  icon: string;
}

export interface LiabilityItem {
  name: string;
  category: string;
  value: number;
  rate: string;
  icon: string;
}

export interface BalanceSummary {
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
  equityRatio: number;
  assets: AssetItem[];
  liabilities: LiabilityItem[];
}
