import { useState } from 'react';
import axios from 'axios';

const LiqPayButton = ({ amount, description, currency = 'UAH' }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handlePayment = async () => {
    setLoading(true);
    setError(null);

    try {
      // const orderPlacementResponse = await axios.post('/api/order/place', {});
      // const paymentId = orderPlacementResponse.data;
      const paymentId = 2;

      const paymentResponse = await axios.post('https://irrigative-bessie-evidentially.ngrok-free.dev/api/payment/create', {
        amount: amount,
        currency: currency,
        description: description,
        paymentId: paymentId
      });

      const { data, signature } = paymentResponse.data;
      console.log('LiqPay Data:', data);
      console.log('LiqPay Signature:', signature);

      // setLoading(false);
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
        className={`w-full py-3 rounded-lg text-lg font-semibold text-white ${loading ? 'bg-blue-400 cursor-not-allowed opacity-70' : 'bg-blue-600 hover:bg-blue-700'}`}
      >
        {loading ? 'Processing...' : 'Go to payment'}
      </button>
      {error && <p className="text-red-500 mt-2">{error}</p>}
    </div>
  );
};

export default LiqPayButton;
