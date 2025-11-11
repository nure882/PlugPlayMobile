export interface CartItem {
  id: number;
  total: number;
  quantity: number;
  productId: number;
  userId?: number;
}
