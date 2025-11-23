import { CartItem } from "../../models/CartItem.ts";
import { Product } from "../../models/Product.ts";
import {
  useGetCartQuery,
  useAddToCartMutation,
  useIsInCartQuery,
  useUpdateQuantityMutation,
  useDeleteCartItemMutation,
  useClearCartMutation,
} from "../../api/cartApi.ts";
import { storage } from "../../utils/StorageService.ts";
import { useMemo } from "react";
import { skipToken } from '@reduxjs/toolkit/query';
import { useState } from "react";

class CartService {
  useCart(userId?: number): { cartItems: CartItem[], isLoading: boolean, isError: boolean, refetch: () => void } {
    const {
      data: cartItemsFromApi = [],
      isLoading,
      isError,
      refetch: refetchApiCart,
    } = useGetCartQuery(userId as number, {
      skip: !userId,
    });
   
    const stored = storage.getGuestCart();
    const cartItems = useMemo(() => {
      if (userId) {
        // console.log(`[CartService] getting items from api for user/${userId}`)
        return cartItemsFromApi;
      } 

      return stored;
    }, [cartItemsFromApi, stored, userId]);

    const [version, setVersion] = useState(0);

    const refetchCart = () => {
      if (userId) {
        refetchApiCart();
      }
      else {
        //console.log("[CartService] refetch cart with version")
        setVersion((v) => v + 1);
      }
    };

    // console.log('[CartService] Results:', {
    //   cartItemsCount: cartItems.length,
    //   isLoading,
    //   isError,
    //   cartItems,
    // });

    return { cartItems, isLoading, isError, refetch: refetchCart };
  }

  useAddToCart(userId?: number) {
    const [addToCartMutation] = useAddToCartMutation();

    return async (product: Product, quantity: number ) => {
     
      if (userId) {
        await addToCartMutation({ productId: product.id, quantity: quantity, userId });
      } else {
        const cart = storage.getGuestCart();
        const existing = cart.find(ci => ci.productId === product.id);

        if (existing) {
            existing.quantity += quantity;
            existing.total = existing.quantity * product.price;
        }
        else {
            cart.push({ userId: undefined, productId: product.id, id: Date.now(), quantity: 1, total: product.price});
        }
        storage.setGuestCart(cart);
      }
    };
  }

  useMergeGuestCart(userId?: number) {
    const stored = storage.getGuestCart();
    const [addToCartMutation] = useAddToCartMutation();

    return async () => {
      if (!userId || storage.getGuestCart().length === 0) {
        return;
      }

      //console.log(`[CartService] merging items from api for user/${userId}`)

      for (const item of stored) {
        await addToCartMutation({
          userId,
          productId: item.productId,
          quantity: item.quantity,
        });
      }

      storage.clearGuestCart();
    }
  }

  useUpdateQuantity(userId?: number) {
    const [updateQuantityMutation] = useUpdateQuantityMutation();

    return async (cartItemId: number, quantity: number, price: number) => {
      if (userId) {
        await updateQuantityMutation({ cartItemId, newQuantity: quantity });
      } 
      else {
        if(quantity < 1) {
            return;
        }

        const cart = storage.getGuestCart();
        const item = cart.find(ci => ci.id === cartItemId);
       
        if (item) {
            item.quantity = quantity;
            item.total = quantity * price;
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
        const cart = storage.getGuestCart();
        storage.setGuestCart(cart.filter(ci => ci.id !== cartItemId));
      }
    };
  }

  useClearCart(userId?: number) {
    const [clearMutation] = useClearCartMutation();
    return async () => {
      if (userId) {
        await clearMutation(userId);
      } 
      else storage.clearGuestCart();
    };
  }

  useIsInCart(productId: number, userId?: number) {
    const { data, refetch } = useIsInCartQuery(userId? { productId, userId } : skipToken);
    if (userId) {
      return { isInCart: data ?? false, refetch };
    } else {
      const cart: CartItem[] = storage.getGuestCart() ?? [];
      const exists = cart.some(ci => ci.productId === productId);

      return { isInCart: exists, refetch: () => {} };
    }
  }

}
export const cartService = new CartService();
