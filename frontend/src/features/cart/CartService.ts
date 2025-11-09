import { CartItem } from "../../models/CartItem.ts";
import {
  useGetCartQuery,
  useAddToCartMutation,
  useIsInCartQuery,
  useUpdateQuantityMutation,
  useDeleteCartItemMutation,
  useClearCartMutation,
} from "../../api/cartApi.ts";
import { storage } from "../../utils/StorageService.ts";
import {
  useGetProductByIdQuery,
} from "../../api/productsApi.ts";
import { useMemo } from "react";
import { skipToken } from '@reduxjs/toolkit/query';

class CartService {
  useCart(userId?: number): { cartItems: CartItem[], isLoading: boolean, isError: boolean, refetch: () => void } {
    const { data: cartItemsFromApi = [], isLoading, isError, refetch } = useGetCartQuery(userId ?? skipToken);

    const cartItems = useMemo(() => {
      if (userId) return cartItemsFromApi;
      const stored = storage.getGuestCart();
      return stored ?? [];
    }, [cartItemsFromApi, userId]);

    const refetchCart = () => {
      if (userId) {
        refetch();
      }
      else {
        // For guest, maybe trigger state update manually
      }
    };

    return { cartItems, isLoading, isError, refetch: refetchCart };
  }

  useAddToCart(userId?: number) {
    const [addToCartMutation] = useAddToCartMutation();
    return async (item: { productId: number; quantity: number }) => {
      if (userId) {
        await addToCartMutation({ ...item, userId });
      } else {
        const cart = storage.getGuestCart() ?? [];
        const existing = cart.find(ci => ci.productId === item.productId);
        if (existing) {
            existing.quantity += item.quantity;
        }
        else {
            const {data : product} = useGetProductByIdQuery(item.productId);
            cart.push({ userId: undefined, productId: item.productId, id: Date.now(), quantity: 1, total: product?.price ?? 0});
        }
        storage.setGuestCart(cart);
      }
    };
  }

  useUpdateQuantity(userId?: number) {
    const [updateQuantityMutation] = useUpdateQuantityMutation();
    return async (cartItemId: number, quantity: number) => {
      if (userId) {
        await updateQuantityMutation({ cartItemId, newQuantity: quantity });
      } 
      else {
        const cart = storage.getGuestCart() ?? [];
        const item = cart.find(ci => ci.id === cartItemId);
        if (item) {
            const {data : product} = useGetProductByIdQuery(item.productId);
            item.quantity = quantity;
            item.total = item.quantity * (product?.price ?? 0)
        }
        storage.setGuestCart(cart);
      }
    };
  }

  useDeleteCartItem(userId?: number) {
    const [deleteMutation] = useDeleteCartItemMutation();
    return async (cartItemId: number) => {
      if (userId) {
        await deleteMutation(cartItemId);
      } 
      else {
        const cart = storage.getGuestCart() ?? [];
        storage.setGuestCart(cart.filter(ci => ci.id !== cartItemId));
      }
    };
  }

  useClearCart(userId?: number) {
    const [clearMutation] = useClearCartMutation();
    return async () => {
      if (userId) await clearMutation(userId);
      else storage.clearGuestCart();
    };
  }

  useIsInCart(productId: number, userId?: number) {
    if (userId) {
      const { data, refetch } = useIsInCartQuery({ productId, userId }, { skip: !userId });
      return { isInCart: data ?? false, refetch };
    } else {
      const cart: CartItem[] = storage.getGuestCart() ?? [];
      const exists = cart.some(ci => ci.productId === productId);

      return { isInCart: exists, refetch: () => {} };
    }
  }
}

export const cartService = new CartService();