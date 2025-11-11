import {baseApi} from './baseApi.ts';
import {CartItem} from "../models/CartItem.ts";

export interface CreateCartItemDto {
  productId: number;
  quantity: number;
  userId: number;
}

export interface UpdateCartItemQuantityDto {
  cartItemId: number;
  newQuantity: number;
}

export const cartApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    getCart: builder.query<CartItem[], number>({
      query: (userId) => `cart/${userId}`,
    }),
    getCartItem: builder.query<CartItem, number>({
      query: (itemId) => `cart/item/${itemId}`,
    }),
    getCartItemsTotal: builder.query<number, number>({
      query: (userId) => `cart/total/${userId}`,
    }),
    isInCart: builder.query<boolean, { productId: number; userId: number }>({
      query: ({productId, userId}) => `cart/isincart/${productId}/${userId}`
    }),
    addToCart: builder.mutation<{ cartItemId: number }, CreateCartItemDto>({
      query: (dto) => ({
        url: 'cart',
        method: 'POST',
        body: dto,
      }),
    }),
    updateQuantity: builder.mutation<void, UpdateCartItemQuantityDto>({
      query: (dto) => ({
        url: 'cart/quantity',
        method: 'PUT',
        body: dto,
      }),
    }),
    deleteCartItem: builder.mutation<void, number>({
      query: (itemId) => ({
        url: `cart/${itemId}`,
        method: 'DELETE',
      }),
    }),
    clearCart: builder.mutation<void, number>({
      query: (userId) => ({
        url: `cart/clear/${userId}`,
        method: 'DELETE',
      }),
    }),
  }),
});

export const {
  useGetCartQuery,
  useGetCartItemQuery,
  useGetCartItemsTotalQuery,
  useIsInCartQuery,
  useAddToCartMutation,
  useUpdateQuantityMutation,
  useDeleteCartItemMutation,
  useClearCartMutation,
} = cartApi;
