export type PaymentStatus = 'PENDING' | 'APPROVED' | 'REJECTED';
export interface Payment { id: number; orderId: number; status: PaymentStatus; amount: number; createdAt: string; updatedAt: string; }
