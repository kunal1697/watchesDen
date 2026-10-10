import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { formatPrice } from '../utils/format';

export default function Cart() {
  const { cart, update, remove, refresh, busy } = useCart();

  useEffect(() => {
    refresh();
  }, [refresh]);

  if (cart.items.length === 0) {
    return (
      <div>
        <h1>My Cart</h1>
        <p>Your cart is empty.</p>
        <Link to="/">Go to products</Link>
      </div>
    );
  }

  return (
    <div>
      <h1>My Cart</h1>
      <table className="table">
        <thead>
          <tr>
            <th>Product</th>
            <th>Customization</th>
            <th>Price</th>
            <th>Quantity</th>
            <th>Total</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {cart.items.map((item) => (
            <tr key={item.id}>
              <td>
                {item.productName}
                {!item.available && <span className="red"> (not available)</span>}
              </td>
              <td>{item.customizationSummary ?? '-'}</td>
              <td>{formatPrice(item.unitPrice)}</td>
              <td>
                <button disabled={busy || item.quantity <= 1} onClick={() => update(item.id, item.quantity - 1)}>
                  -
                </button>{' '}
                {item.quantity}{' '}
                <button disabled={busy || item.quantity >= 10} onClick={() => update(item.id, item.quantity + 1)}>
                  +
                </button>
              </td>
              <td>{formatPrice(item.lineTotal)}</td>
              <td>
                <button className="danger" disabled={busy} onClick={() => remove(item.id)}>
                  Remove
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <h3>
        Subtotal ({cart.itemCount} items): {formatPrice(cart.subtotal)}
      </h3>
      <Link to="/checkout" className="button">
        Proceed to Checkout
      </Link>
    </div>
  );
}
