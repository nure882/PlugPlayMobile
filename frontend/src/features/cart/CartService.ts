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
import { useState, useEffect } from "react";

class CartService {
  useCart(userId?: number): { cartItems: CartItem[], isLoading: boolean, isError: boolean, refetch: () => void } {
    //this.mergeGuestCart();
    const [addToCartMutation] = useAddToCartMutation();
    const {
      data: cartItemsFromApi = [],
      isLoading,
      isError,
      refetch: refetchApiCart,
      isUninitialized,
    } = useGetCartQuery(userId as number, {
      skip: !userId,
    });
   
    const cartItems = useMemo(() => {
      const stored = storage.getGuestCart();

      if (userId) {
        return cartItemsFromApi;
      } 

      return stored;
    }, [cartItemsFromApi, userId]);

    //merge guest cart
    useEffect(() => {
      if (!userId) {
        return;
      }

      const stored = storage.getGuestCart();
        for (const item of stored) {
           addToCartMutation({
            userId,
            productId: item.productId,
            quantity: item.quantity,
          });
        }

        storage.clearGuestCart();

        // wait for RTK Query to actually start before refetching
        if (!isUninitialized) {
          refetchApiCart();
        }
    });

    //temporary solution for rerendering guest cart
    const [version, setVersion] = useState(0);

    const refetchCart = () => {
      if (userId) {
        refetchApiCart();
      }
      else {
        setVersion((v) => v + 1);
      }
    };

    return { cartItems, isLoading, isError, refetch: refetchCart };
  }

  //TODO: more elegant way to get product price
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

  //TODO: more elegant way to get product price
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

        const cart = storage.getGuestCart() ?? [];
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
        const cart = storage.getGuestCart() ?? [];
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