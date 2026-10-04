export type ColumnType = 'text' | 'date' | 'period' | 'currency' | 'badge' | 'custom';

export interface TableColumn<T = any> {
  key: string;
  label: string;
  type?: ColumnType;
  align?: 'left' | 'center' | 'right';
  width?: string;
  headerClass?: string;
  cellClass?: string;
  periodStartKey?: string;
  periodEndKey?: string;
}

export interface TableAction<T = any> {
  id: string;
  label?: string;
  icon?: string;
  title?: string;
  variant?: 'icon' | 'button' | 'badge';
  tone?: 'primary' | 'secondary' | 'danger' | 'success';
  visible?: (row: T) => boolean;
  disabled?: (row: T) => boolean;
}

export interface TablePagination {
  pageIndex: number;
  pageSize: number;
  totalElements: number;
  totalPages?: number;
  pageSizeOptions?: number[];
}
