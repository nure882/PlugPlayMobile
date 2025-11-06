export interface Review {
  id: number;
  productId: number;
  userId: number | null;
  rating: number;
  comment?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}
