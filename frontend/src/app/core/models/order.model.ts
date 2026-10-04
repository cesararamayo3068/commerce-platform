export type OrderStatus = 'CONFIRMED' | 'CANCELLED';

export interface OrderItem {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  userId: number;
  cartId: number;
  status: OrderStatus;
  items: OrderItem[];
  subtotal: number;
  discount: number;
  total: number;
  promotionCode: string | null;
  createdAt: string;
  updatedAt: string;
}
