/**
 * API contract for Cart (mirrors CartResponse in the backend).
 */
export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export type CartStatus = 'ACTIVE' | 'CHECKED_OUT' | 'CANCELLED';

export interface Cart {
  id: number;
  userId: number;
  status: CartStatus;
  items: CartItem[];
  total: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * Request body for creating a cart (CartCreateRequest).
 */
export interface CartCreateRequest {
  userId: number;
}

/**
 * Request body for adding a product to a cart (CartItemRequest).
 */
export interface CartItemRequest {
  productId: number;
  quantity: number;
}

/**
 * Request body for updating an item quantity (CartItemQuantityUpdateRequest).
 * The backend sets the exact value provided (PUT semantics).
 */
export interface CartItemQuantityUpdateRequest {
  quantity: number;
}
