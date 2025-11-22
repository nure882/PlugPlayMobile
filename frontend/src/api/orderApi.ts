import {baseApi} from './baseApi.ts';
import OrderItem from "../models/OrderItem";

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
  }),
});

export const { usePlaceOrderMutation } = orderApi;