export interface Product{
    productId: number;
    productCode: string;
    productName: string;
    productDescription: string;
    productPrice: number;
    productWeight: number;
    productImageUrl: string | null;
    createdAt: string;
    updatedAt: string;
}
export interface ProductRequest{
    productCode:string;
    productName:string
    productDescription: string;
    productPrice: number;
    productWeight: number;
    productImageUrl: string | null;
}
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors: string[] | null;
}

export interface ListParams {
  page: number;
  size: number;
  sort: string;
  query: string;
}

// Sort values are Product ENTITY field names (what Spring Data expects)
export const SORT_OPTIONS = [
  { label: 'Newest first', value: 'createdAt,desc' },
  { label: 'Name A–Z', value: 'productName,asc' },
  { label: 'Price: low to high', value: 'productPrice,asc' },
  { label: 'Price: high to low', value: 'productPrice,desc' },
] as const;