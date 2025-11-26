import {useState} from 'react';
import {PlaceOrderRequest} from "../../pages/Checkout.tsx";
import {usePlaceOrderMutation} from "../../api/orderApi.ts";
import PaymentMethod from "../../models/enums/PaymentMethod.ts";

interface LiqPayButtonProps {
  request: PlaceOrderRequest | null;
}

const LiqPayButton = (props: LiqPayButtonProps) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [placeOrder] = usePlaceOrderMutation();

  if (props.request === null) {
    return null;
  }

  const {userId, deliveryAddressId, orderItems, deliveryMethod} = props.request;

  const handlePayment = async () => {
    setLoading(true);
    setError(null);

    try {
      const {data: orderPlacementRes, error} = await placeOrder({
        userId: userId,
        deliveryAddressId: deliveryAddressId,
        orderItems: orderItems,
        paymentMethod: PaymentMethod.Card,
        deliveryMethod: deliveryMethod,
      });

      if (error || !orderPlacementRes) {
        console.error('Order placement error:', error);
        setError('Failed to place order. Please try again.');
        setLoading(false);
        return;
      }

      const {data, signature} = orderPlacementRes.paymentData;

      const form = document.createElement('form');
      form.method = 'POST';
      form.action = 'https://www.liqpay.ua/api/3/checkout';
      form.acceptCharset = 'utf-8';

      const dataInput = document.createElement('input');
      dataInput.type = 'hidden';
      dataInput.name = 'data';
      dataInput.value = data;

      const signatureInput = document.createElement('input');
      signatureInput.type = 'hidden';
      signatureInput.name = 'signature';
      signatureInput.value = signature;

      form.appendChild(dataInput);
      form.appendChild(signatureInput);
      document.body.appendChild(form);
      form.submit();
    } catch (err) {
      console.error('Payment error:', err);
      setError('Failed to initiate payment. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div>
      <button
        onClick={handlePayment}
        disabled={loading}
        className={`w-full py-3 rounded-lg text-lg font-semibold text-white ${
          loading ? 'bg-blue-400 cursor-not-allowed opacity-70' : 'bg-blue-600 hover:bg-blue-700'
        }`}
      >
        {loading ? 'Processing...' : 'Go to payment'}
      </button>
      {error && <p className="text-red-500 mt-2">{error}</p>}
    </div>
  );
};

export default LiqPayButton;
