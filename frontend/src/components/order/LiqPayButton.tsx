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
        style={{
          backgroundColor: '#77CC5D',
          color: 'white',
          border: 'none',
          padding: '12px 24px',
          fontSize: '16px',
          fontWeight: 'bold',
          borderRadius: '4px',
          cursor: loading ? 'not-allowed' : 'pointer',
          opacity: loading ? 0.6 : 1
        }}
      >
        {loading ? 'Processing...' : 'Pay'}
      </button>
      {error && <p style={{ color: 'red', marginTop: '10px' }}>{error}</p>}
    </div>
  );
};

export default LiqPayButton;
