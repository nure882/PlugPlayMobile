import {baseApi} from './baseApi.ts';
import OrderItem from "../models/OrderItem";
import { Order } from '../models/Order.ts';

export interface PlaceOrderRequest {
  userId: number;
  paymentMethod: number;
  deliveryMethod: number;
  deliveryAddressId?: number;
  orderItems: OrderItem[];
}

export interface LiqPayPaymentData {
  data: string;
  signature: string;
}

export interface PlaceOrderResponse {
  orderId: number;
  paymentData: LiqPayPaymentData;
}

export const orderApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    placeOrder: builder.mutation<PlaceOrderResponse, PlaceOrderRequest>({
      query: (order) => ({
        url: "order/",
        method: "POST",
        body: order,
      }),
    }),
    getUserOrders: builder.query<Order[], number>({
      query: (userId) => `order/user/${userId}`,
    }),
    getOrderItems: builder.query<OrderItem[], number>({
      query: (orderId) => `order/${orderId}/order_items`,
    }),
    getOrderById: builder.query<Order, number>({
      query: (orderId) => `order/${orderId}`,
    }),
    cancelOrder: builder.mutation<void, number>({
      query: (orderId) => ({
        url: `order/cancel/${orderId}`,
        method: "PUT",
      })
    }),
  })
});

export const {
  usePlaceOrderMutation,
  useGetUserOrdersQuery,
  useGetOrderItemsQuery,
  useGetOrderByIdQuery,
  useCancelOrderMutation,
} = orderApi;