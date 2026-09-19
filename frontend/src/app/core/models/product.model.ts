/** API contract for Product (mirrors ProductResponse in the backend). */
export interface Product {
  id: number;
  name: string;
  description: string | null;
  brand: string | null;
  category: string | null;
  imageUrl: string | null;
  price: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProductCreateRequest {
  name: string;
  description: string | null;
  brand: string | null;
  category: string | null;
  imageUrl: string | null;
  price: number;
}

export interface ProductUpdateRequest extends ProductCreateRequest {
  active: boolean | null;
}
