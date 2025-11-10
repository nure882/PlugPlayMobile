import { X, Minus, Plus, Trash2 } from 'lucide-react';
import React, {useMemo } from 'react';
import {useGetAllProductsQuery} from '../../api/productsApi.ts';
import { cartService } from '../../features/cart/CartService.ts';
import {storage} from '../../utils/StorageService';
import { useGetUserByTokenQuery } from '../../api/userInfoApi.ts';
import { skipToken } from '@reduxjs/toolkit/query';
import LoadingMessage from '../common/LoadingMessage.tsx';
import ErrorMessage from '../common/ErrorMessage.tsx';
import Modal from '../common/Modal.tsx';

interface ShoppingCartProps {
  isOpen: boolean;
  onClose: () => void;
}

export function ShoppingCart({ isOpen, onClose }: ShoppingCartProps) {
  const token = storage.getAccessToken();
  const {data: user, isLoading: isLoadingUser, isError: isUserError} = useGetUserByTokenQuery(token ?? skipToken);

  const {cartItems, isLoading, isError, refetch: updateCart} = cartService.useCart(user?.id);
  const {data: products, isLoading : isLoadingProducts, isError : isProductsError} = useGetAllProductsQuery();

  const sortedItems = React.useMemo(
    () => [...(cartItems ?? [])].sort((a, b) => a.id - b.id),
    [cartItems]
  );

  //cartItems with mapped data from products
  const enrichedItems = useMemo(() =>
  sortedItems.map(item => ({
    ...item,
    product: products?.find(p => p.id === item.productId),
  })),
  [sortedItems, products]
  );

  const updateQuantity = cartService.useUpdateQuantity(user?.id);
  const deleteCartItem =  cartService.useDeleteCartItem(user?.id);
  const clearCart = cartService.useClearCart(user?.id);

  const handleUpdateQuantity = async (id: number, newQuantity: number, price: number) => {
    await updateQuantity(id, newQuantity, price);
    updateCart();
  }

  const handleDelete = async (id: number) => {

    await deleteCartItem(id);
    updateCart();
  };

  const handleClear = async () => {
    await clearCart();
    updateCart();
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('uk-UA', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(price);
  };

  if (!isOpen){
    return null;
  } 

  if (isLoading || isLoadingUser || isLoadingProducts) {
     return (
      <Modal title="Shopping Cart" isOpen={isOpen} onClose={onClose}>
        {LoadingMessage("shopping cart")}
      </Modal>
    );
  } 
  
  if(isError || isUserError || isProductsError || !cartItems) {
    return (
      <Modal title="Shopping Cart" isOpen={isOpen} onClose={onClose}>
        {ErrorMessage("Error loading cart","Failed to retrieve products.")}
      </Modal>
    );
  }

  const subtotal = cartItems.reduce((sum, item) => sum + item.total, 0);

  return (
    <>
      <div
        className="fixed inset-0 bg-black/50 z-50"
        onClick={onClose}
      />

      <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-2xl max-h-[90vh] bg-white rounded-2xl shadow-2xl z-50 overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <h2 className="text-2xl">Shopping cart</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="overflow-y-auto px-6 py-4" style={{ maxHeight: 'calc(90vh - 220px)' }}>
          <div className="space-y-4">
            {enrichedItems.map((item) => (
              <div
                key={item.id}
                className="flex gap-4 p-4 bg-white border border-gray-200 rounded-xl hover:shadow-md transition-shadow"
              >
                <div className="w-24 h-24 flex-shrink-0 bg-gray-100 rounded-lg overflow-hidden">
                  { <img
                    src={item.product?.pictureUrls[0] ?? ''}
                    alt={item.product?.name ?? 'product'}
                    className="w-full h-full object-cover"
                  /> }
                </div>

                <div className="flex-1 min-w-0">
                  <h3 className="mb-1 line-clamp-2"><b>{item.product?.name ?? ''}</b></h3>
                  <h4 className="text-sm mb-1 line-clamp-2">{item.product?.description ?? ''}</h4>
                  <div className="flex flex-col items-start gap-1">
                    <div className="text-red-600">
                      price: {formatPrice(item.product?.price ?? 0)} ₴
                    </div>
                    <div className="text-red-600">
                      total: {formatPrice(item.total)} ₴
                    </div>
                  </div>
                </div>

                <div className="flex flex-col items-end justify-between">
                  <button
                    onClick={() => handleDelete(item.id)}
                    className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
                  >
                    <Trash2 className="w-5 h-5 text-gray-400" />
                  </button>

                  <div className="flex items-center gap-2 border border-gray-300 rounded-lg">
                    <button
                      onClick={() => handleUpdateQuantity(item.id, item.quantity - 1, item.product?.price ?? 0)}
                      className="p-2 hover:bg-gray-100 transition-colors"
                    >
                      <Minus className="w-4 h-4" />
                    </button>
                    <span className="w-8 text-center">{item.quantity}</span>
                    <button
                      onClick={() => handleUpdateQuantity(item.id, item.quantity + 1, item.product?.price ?? 0)}
                      className="p-2 hover:bg-gray-100 transition-colors"
                    >
                      <Plus className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="px-6 py-4 border-t border-gray-200 bg-gray-50">
          <div className="flex items-center justify-between mb-4">
            <div>
              <p className="text-gray-500 text-sm mb-1">Total sum</p>
              <p className="text-3xl">{formatPrice(subtotal)} ₴</p>
            </div>
            <button
              className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-6 text-lg rounded-xl disabled:bg-gray-400 disabled:cursor-not-allowed"
              disabled={cartItems.length === 0}
            >
              Order now
            </button>
            <button
              onClick={handleClear}
              disabled={cartItems.length === 0}
              className="flex items-center gap-2 bg-red-100 text-red-700 px-5 py-3 rounded-xl hover:bg-red-200 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              <Trash2 className="w-5 h-5" />
              Clear
            </button>
          </div>
        </div>
      </div>
    </>
  );
}