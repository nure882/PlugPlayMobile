import { CartItem } from "../../models/CartItem.ts";
import {
  useGetCartQuery,
  useAddToCartMutation,
  useIsInCartQuery,
  useUpdateQuantityMutation,
  useDeleteCartItemMutation,
  useClearCartMutation,
} from "../../api/cartApi.ts";
import {useGetAllProductsQuery} from '../../api/productsApi.ts';
import { storage } from "../../utils/StorageService.ts";
import { useMemo } from "react";
import { skipToken } from '@reduxjs/toolkit/query';
import { useState } from "react";

class CartService {
  useCart(userId?: number): { cartItems: CartItem[], isLoading: boolean, isError: boolean, refetch: () => void } {
    const { data: cartItemsFromApi = [], isLoading, isError, refetch } = useGetCartQuery(userId ?? skipToken);

    const cartItems = useMemo(() => {
      if (userId) return cartItemsFromApi;
      const stored = storage.getGuestCart();
      return stored ?? [];
    }, [cartItemsFromApi, userId]);

    //temporary solution for rerendering guest cart
    const [version, setVersion] = useState(0);

    const refetchCart = () => {
      if (userId) {
        refetch();
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
    const {data: products} = useGetAllProductsQuery();

    return async (item: { productId: number; quantity: number }) => {
     
      if (userId) {
        await addToCartMutation({ ...item, userId });
      } else {
        const cart = storage.getGuestCart() ?? [];
        const existing = cart.find(ci => ci.productId === item.productId);
        const product = products?.find(p => p.id === item.productId);
        const productPrice = (product?.price ?? 0);

        if (existing) {
            existing.quantity += item.quantity;
            existing.total = existing.quantity * productPrice;
        }
        else {
            cart.push({ userId: undefined, productId: item.productId, id: Date.now(), quantity: 1, total: productPrice});
        }
        storage.setGuestCart(cart);
      }
    };
  }

  
  //TODO: more elegant way to get product price
  useUpdateQuantity(userId?: number) {
    const [updateQuantityMutation] = useUpdateQuantityMutation();
    const {data: products} = useGetAllProductsQuery();

    return async (cartItemId: number, quantity: number) => {
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
            const product = products?.find(p => p.id === item.productId);
            item.quantity = quantity;
            item.total = item.quantity * (product?.price ?? 0);
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