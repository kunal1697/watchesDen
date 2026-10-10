import { useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { orderApi } from '../api';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useToast } from '../context/ToastContext';
import { formatPrice, uuid } from '../utils/format';

export default function Checkout() {
  const { user } = useAuth();
  const { cart, reset } = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  // Same key is re-sent if the user clicks twice, so the backend never creates two orders.
  const idempotencyKey = useRef(uuid());
  const [form, setForm] = useState({ fullName: user?.fullName ?? '', addressLine: '', city: '', postalCode: '', country: 'India' });
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);

  if (cart.items.length === 0) {
    return (
      <div>
        <h1>Checkout</h1>
        <p>Your cart is empty.</p>
        <Link to="/">Go to products</Link>
      </div>
    );
  }

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setErrors({});
    try {
      const order = await orderApi.checkout({ shipping: form, paymentMethod }, idempotencyKey.current);
      reset();
      toast(`Order placed successfully! Order no: ${order.orderNumber}`, 'success');
      navigate('/orders');
    } catch (err) {
      // Show the validation errors returned by the backend next to each field.
      const fe = {};
      Object.entries(err.fieldErrors ?? {}).forEach(([k, msg]) => (fe[k.replace('shipping.', '')] = msg));
      setErrors(fe);
      toast(err.message, 'error');
      if (err.status && err.status < 500) idempotencyKey.current = uuid();
    } finally {
      setBusy(false);
    }
  };

  const input = (name, label) => (
    <div className="form-row">
      <label htmlFor={name}>{label}</label>
      <input id={name} value={form[name]} onChange={(e) => setForm({ ...form, [name]: e.target.value })} />
      {errors[name] && <span className="error">{errors[name]}</span>}
    </div>
  );

  return (
    <div>
      <h1>Checkout</h1>
      <div className="two-col">
        <form onSubmit={submit} className="box" noValidate>
          <h3>Shipping Address</h3>
          {input('fullName', 'Full Name')}
          {input('addressLine', 'Address')}
          {input('city', 'City')}
          {input('postalCode', 'PIN Code')}
          {input('country', 'Country')}

          <h3>Payment Method</h3>
          {['CARD', 'UPI', 'COD'].map((m) => (
            <label key={m} className="radio">
              <input type="radio" name="payment" checked={paymentMethod === m} onChange={() => setPaymentMethod(m)} /> {m}
            </label>
          ))}
          {errors.paymentMethod && <p className="error">{errors.paymentMethod}</p>}
          <p className="muted">Note: payment is only simulated.</p>

          <button type="submit" disabled={busy}>
            {busy ? 'Placing order...' : 'Place Order'}
          </button>
        </form>

        <div className="box">
          <h3>Order Summary</h3>
          <table className="table">
            <tbody>
              {cart.items
                .filter((i) => i.available)
                .map((i) => (
                  <tr key={i.id}>
                    <td>
                      {i.productName} x {i.quantity}
                      {i.customizationSummary && <div className="muted">{i.customizationSummary}</div>}
                    </td>
                    <td>{formatPrice(i.lineTotal)}</td>
                  </tr>
                ))}
              <tr>
                <th>Total</th>
                <th>{formatPrice(cart.subtotal)}</th>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
