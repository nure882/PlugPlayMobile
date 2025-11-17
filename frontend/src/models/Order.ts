import {Address} from "./Address.ts";
import OrderItem from "./OrderItem.ts";

export interface Order {
  id: number;
  userId: number;
  orderDate: string;
  status: number;
  totalAmount: number;
  discountAmount: number;
  deliveryMethod: number;
  paymentMethod: number;
  deliveryAddressId: number;
  paymentStatus: number;
  transactionId: number;
  paymentCreated: string;
  paymentProcessed: string;
  paymentFailureReason: string | null;
  updatedAt: string;
  deliveryAddress: Address;
  orderItems: OrderItem[];
}
