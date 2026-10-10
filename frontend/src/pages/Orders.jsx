import { Fragment, useEffect, useState } from 'react';
import { orderApi } from '../api';
import Loader from '../components/Loader';
import { useToast } from '../context/ToastContext';
import { formatDate, formatPrice } from '../utils/format';

export function StatusBadge({ status }) {
  return <span className={`status status-${status.toLowerCase()}`}>{status}</span>;
}

export default function Orders() {
  const toast = useToast();
  const [orders, setOrders] = useState(null);
  const [openId, setOpenId] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    orderApi
      .list()
      .then((r) => setOrders(r.content))
      .catch((e) => setError(e.message));
  }, []);

  const cancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this order?')) return;
    try {
      const updated = await orderApi.cancel(id);
      setOrders(orders.map((o) => (o.id === id ? updated : o)));
      toast('Order cancelled', 'success');
    } catch (e) {
      toast(e.message, 'error');
    }
  };

  if (error) return <p className="error">{error}</p>;
  if (!orders) return <Loader />;

  return (
    <div>
      <h1>My Orders</h1>
      {orders.length === 0 ? (
        <p>You have not placed any orders yet.</p>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Order No</th>
              <th>Date</th>
              <th>Items</th>
              <th>Total</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <Fragment key={o.id}>
                <tr>
                  <td>{o.orderNumber}</td>
                  <td>{formatDate(o.createdAt)}</td>
                  <td>{o.items.reduce((s, i) => s + i.quantity, 0)}</td>
                  <td>{formatPrice(o.totalAmount)}</td>
                  <td>
                    <StatusBadge status={o.status} />
                  </td>
                  <td>
                    <button className="secondary" onClick={() => setOpenId(openId === o.id ? null : o.id)}>
                      {openId === o.id ? 'Hide' : 'Details'}
                    </button>{' '}
                    {['PLACED', 'CONFIRMED'].includes(o.status) && (
                      <button className="danger" onClick={() => cancel(o.id)}>
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
                {openId === o.id && (
                  <tr className="details-row">
                    <td colSpan="6">
                      <ul>
                        {o.items.map((i) => (
                          <li key={`${i.productId}-${i.optionIds.join()}`}>
                            {i.productName} x {i.quantity} = {formatPrice(i.lineTotal)}
                            {i.customizationSummary && ` (Custom: ${i.customizationSummary})`}
                          </li>
                        ))}
                      </ul>
                      <p>
                        Ship to: {o.shipping.fullName}, {o.shipping.addressLine}, {o.shipping.city} - {o.shipping.postalCode},{' '}
                        {o.shipping.country}
                        <br />
                        Payment: {o.paymentMethod}
                      </p>
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
