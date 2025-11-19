import React, { useEffect, useState, useMemo } from 'react';
import { storage } from '../utils/StorageService';
import LiqPayButton from '../components/order/LiqPayButton';
import { useNavigate } from 'react-router-dom';
import { Truck, Box, Zap, CreditCard, DollarSign } from 'lucide-react';
import {useGetUserByTokenQuery} from '../api/userInfoApi.ts';
import { Address } from '../models/Address.ts';
import { CartItem } from '../models/CartItem.ts';
import LoadingMessage from '../components/common/LoadingMessage.tsx';
import ErrorMessage from '../components/common/ErrorMessage.tsx';
import { cartService } from '../features/cart/CartService.ts';
import { useGetAllProductsQuery } from '../api/productsApi.ts';

const Checkout: React.FC = () => {
  const navigate = useNavigate();
 const token = storage.getAccessToken();
  const {data: user, isLoading: isLoadingUser, isError: isUserError, refetch} = useGetUserByTokenQuery(token ?? '', {skip: !token});

  const {cartItems, isLoading: isLoadingCart, isError: isCartError, refetch: updateCart} = cartService.useCart(user?.id);
  const {data: products, isLoading: isLoadingProducts, isError: isProductsError} = useGetAllProductsQuery();
  
  //cartItems with mapped data from products
  const enrichedItems = useMemo(() =>
      cartItems.map(item => ({
        ...item,
        product: products?.find(p => p.id === item.productId),
      })),
    [cartItems, products]
  );

  const [delivery, setDelivery] = useState<string>('courier');
  const [payment, setPayment] = useState<string>('card');

  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');

  const [deliveryAddress, setDeliveryAddress] = useState<Address>(
  {
    city: "",
    street: "",
    house: "",
    apartments: ""
  }
);

  const subtotal = cartItems.reduce((sum, item) => sum + item.total, 0);
  const shipping = 15.0;
  const total = subtotal + shipping;

  const format = (v: number) => {
    return new Intl.NumberFormat('uk-UA', { style: 'currency', currency: 'UAH' }).format(v);
  };

  const handleMainAction = () => {
    if (payment === 'cash') {
      storage.clearGuestCart();
      alert('Order placed. Thank you!');
      navigate('/');
    }
  };

   useEffect(() => {
    if (user) {
      setFirstName(user.firstName);
      setLastName(user.lastName);
      setEmail(user.email);
      setPhone(user.phoneNumber)

      if(user?.addresses?.[0]) {
        setDeliveryAddress(user?.addresses?.[0])
      }
    }
  }, [user]);

  const handleImageClick = (productId?: number) => {
    if (!productId) {
      return;
    }

    navigate(`/product/${productId}`);
  };

  if (isLoadingUser || isLoadingCart || isLoadingProducts) {
    return LoadingMessage("checkout page");
  }
  
  if (isUserError || isCartError || isProductsError) {
    return ErrorMessage("error loading checkout page", "couldn't retrieve data from the database")
  }

  return (
    <div className="container mx-auto p-6">
      <h1 className="text-4xl font-bold text-center text-blue-600 mb-8">
        Checkout
      </h1>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <section className="bg-white rounded-lg p-6 border">
            <h2 className="text-lg font-medium mb-4">Shipping Information</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input
                placeholder="First Name"
                className="p-3 rounded-lg bg-gray-100"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
              />
              <input
                placeholder="Last Name"
                className="p-3 rounded-lg bg-gray-100"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
              />
              <input
                placeholder="Email"
                className="p-3 rounded-lg bg-gray-100 md:col-span-2"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              <input
                placeholder="Phone Number"
                className="p-3 rounded-lg bg-gray-100 md:col-span-2"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
              />
              <input
                placeholder="City"
                className="p-3 rounded-lg bg-gray-100"
                value={deliveryAddress.city}
                onChange={(e) =>
                  setDeliveryAddress((prev) => ({
                    ...prev,
                    city: e.target.value,
                  }))
                }
              />
              <input
                placeholder="Street"
                className="p-3 rounded-lg bg-gray-100"
                value={deliveryAddress.street}
                onChange={(e) =>
                  setDeliveryAddress((prev) => ({
                    ...prev,
                    street: e.target.value,
                  }))
                }
              />
              <input
                placeholder="House"
                className="p-3 rounded-lg bg-gray-100"
                value={deliveryAddress.house}
                onChange={(e) =>
                  setDeliveryAddress((prev) => ({
                    ...prev,
                    house: e.target.value,
                  }))
                }
              />
              <input
                placeholder="Apartment (optional)"
                className="p-3 rounded-lg bg-gray-100"
                value={deliveryAddress.apartments}
                onChange={(e) =>
                  setDeliveryAddress((prev) => ({
                    ...prev,
                    apartments: e.target.value,
                  }))
                }
              />
            </div>
          </section>

          <section className="bg-white rounded-lg p-6 border">
            <h2 className="text-lg font-medium mb-4">Delivery Type</h2>
            <div className="space-y-3">
              <label className="flex items-start gap-3 p-4 border rounded-lg">
                <input
                  type="radio"
                  name="delivery"
                  checked={delivery === "courier"}
                  onChange={() => setDelivery("courier")}
                />
                <div>
                  <div className="flex items-center gap-2 font-medium">
                    <Truck size={16} /> Courier
                  </div>
                  <div className="text-sm text-gray-500">Delivery 1-2 days</div>
                </div>
              </label>

              <label className="flex items-start gap-3 p-4 border rounded-lg">
                <input
                  type="radio"
                  name="delivery"
                  checked={delivery === "post"}
                  onChange={() => setDelivery("post")}
                />
                <div>
                  <div className="flex items-center gap-2 font-medium">
                    <Box size={16} /> Post
                  </div>
                  <div className="text-sm text-gray-500">Delivery 3-5 days</div>
                </div>
              </label>

              <label className="flex items-start gap-3 p-4 border rounded-lg">
                <input
                  type="radio"
                  name="delivery"
                  checked={delivery === "premium"}
                  onChange={() => setDelivery("premium")}
                />
                <div>
                  <div className="flex items-center gap-2 font-medium">
                    <Zap size={16} /> Premium Delivery
                  </div>
                  <div className="text-sm text-gray-500">Same day delivery</div>
                </div>
              </label>
            </div>
          </section>

          <section className="bg-white rounded-lg p-6 border">
            <h2 className="text-lg font-medium mb-4">Payment Method</h2>
            <div className="space-y-3">
              <label className="flex items-start gap-3 p-4 border rounded-lg">
                <input
                  type="radio"
                  name="payment"
                  checked={payment === "card"}
                  onChange={() => {
                    setPayment("card");
                  }}
                />
                <div>
                  <div className="flex items-center gap-2 font-medium">
                    <CreditCard size={16} /> Card
                  </div>
                  <div className="text-sm text-gray-500">
                    Pay online with card
                  </div>
                </div>
              </label>

              <label className="flex items-start gap-3 p-4 border rounded-lg">
                <input
                  type="radio"
                  name="payment"
                  checked={payment === "cash"}
                  onChange={() => {
                    setPayment("cash");
                  }}
                />
                <div>
                  <div className="flex items-center gap-2 font-medium">
                    <DollarSign size={16} /> Cash after delivery
                  </div>
                  <div className="text-sm text-gray-500">
                    Pay when you receive
                  </div>
                </div>
              </label>
            </div>
          </section>

          <div className="pt-4">
            <div>
              {payment === "cash" ? (
                <button
                  onClick={handleMainAction}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 rounded-lg text-lg font-semibold"
                >
                  Place order
                </button>
              ) : (
                <LiqPayButton
                  amount={total}
                  description={`Order payment ${new Date().toISOString()}`}
                />
              )}
            </div>
          </div>
        </div>

        <aside className="bg-white rounded-lg p-6 border">
          <h3 className="text-lg font-medium mb-4">Order Summary</h3>
          <div className="space-y-4">
            {enrichedItems.length === 0 && (
              <div className="text-gray-500">Your cart is empty</div>
            )}

            {enrichedItems.map((item) => (
              <div
                key={item.id}
                className="flex gap-4 p-4 bg-white border border-gray-200 rounded-xl hover:shadow-md transition-shadow"
              >
                <div className="w-24 h-24 flex-shrink-0 bg-gray-100 rounded-lg overflow-hidden">
                  <img
                    src={item.product?.pictureUrls[0] ?? ""}
                    alt={item.product?.name ?? "product"}
                    className="w-full h-full object-cover"
                    onClick={() => handleImageClick(item.product?.id)}
                  />
                </div>

                <div className="flex-1 min-w-0">
                  <h3 className="mb-1 line-clamp-2">
                    <b>{item.product?.name ?? ""}</b>
                  </h3>

                  <h4 className="text-sm mb-1 line-clamp-2">
                    {item.product?.description ?? ""}
                  </h4>

                  <div className="flex flex-col items-start gap-1">
                    <div className="text-red-600">
                      price: {format(item.product?.price ?? 0)} ₴
                    </div>

                    <div className="text-red-600">
                      quantity: {item.quantity} 
                    </div>

                    <div className="text-red-600">
                      total: {format(item.total)} ₴
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="mt-4 border-t pt-4">
            <div className="flex justify-between text-gray-600 mb-2">
              {" "}
              <span>Subtotal</span> <span>{format(subtotal)}</span>{" "}
            </div>
            <div className="flex justify-between text-gray-600 mb-2">
              {" "}
              <span>Shipping</span> <span>{format(shipping)}</span>{" "}
            </div>
            <div className="flex justify-between font-semibold text-blue-600 text-lg mt-4">
              {" "}
              <span>Total</span> <span>{format(total)}</span>{" "}
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
};

export default Checkout;
