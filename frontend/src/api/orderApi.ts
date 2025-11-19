import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { CartItem } from "../models/CartItem";

export interface PlaceOrderRequest {
  userId: number;
  addressId: number;
  items: CartItem[];
  paymentMethod: number;
}

export interface LiqPayPaymentData {
  data: string;
  signature: string;
}

export interface PlaceOrderResponse {
  paymentData: LiqPayPaymentData ;
}

export const orderApi = createApi({
  reducerPath: "orderApi",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api/order", // URL of your controller
  }),
  endpoints: (builder) => ({
    placeOrder: builder.mutation<PlaceOrderResponse, PlaceOrderRequest>({
      query: (order) => ({
        url: "",
        method: "POST",
        body: order,
      }),
    }),
  }),
});

export const { usePlaceOrderMutation } = orderApi;