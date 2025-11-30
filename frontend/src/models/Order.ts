import {Address} from "./Address.ts";
import OrderItem from "./OrderItem.ts";
import DeliveryMethod from "./enums/DeliveryMethod.ts";
import OrderStatus from "./enums/OrderStatus.ts";
import PaymentMethod from "./enums/PaymentMethod.ts";
import PaymentStatus from "./enums/PaymentStatus.ts";

// Extended OrderItem with UI-specific properties
export interface OrderItemWithDetails extends OrderItem {
    productName?: string;
    price?: number;
}

export interface Order {
    id: number;
    userId: number;
    orderDate: string;
    status: OrderStatus;
    totalAmount: number;
    discountAmount: number;
    deliveryMethod: DeliveryMethod;
    paymentMethod: PaymentMethod;
    deliveryAddressId: number;
    paymentStatus: PaymentStatus;
    transactionId: number;
    paymentCreated: string;
    paymentProcessed: string;
    paymentFailureReason: string | null;
    updatedAt: string;
    deliveryAddress: Address;
    orderItems: OrderItemWithDetails[];
}