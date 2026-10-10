import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../api';
import Loader from '../components/Loader';
import { useToast } from '../context/ToastContext';
import { formatDate, formatPrice, img } from '../utils/format';
import { StatusBadge } from './Orders';

const BLANK = {
  name: '',
  collection: '',
  description: '',
  price: '',
  stock: 10,
  caseMaterial: '',
  movement: 'Automatic',
  diameterMm: 40,
  waterResistanceM: 100,
  imageUrl: '',
  hoverImageUrl: '',
  caseColor: '#C0C6CF',
  dialColor: '#0B1026',
  strapColor: '#1C1F2A',
  accentColor: '#00F0FF',
  customizable: false,
};
const NEXT_STATUS = { PLACED: ['CONFIRMED', 'CANCELLED'], CONFIRMED: ['SHIPPED', 'CANCELLED'], SHIPPED: ['DELIVERED'] };

export default function Admin() {
  const [tab, setTab] = useState('products');
  return (
    <div>
      <h1>Admin Panel</h1>
      <p>
        <button className={tab === 'products' ? '' : 'secondary'} onClick={() => setTab('products')}>
          Manage Products
        </button>{' '}
        <button className={tab === 'orders' ? '' : 'secondary'} onClick={() => setTab('orders')}>
          Manage Orders
        </button>
      </p>
      {tab === 'products' ? <ProductsAdmin /> : <OrdersAdmin />}
    </div>
  );
}

function ProductsAdmin() {
  const toast = useToast();
  const [products, setProducts] = useState(null);
  const [form, setForm] = useState(null);
  const [errors, setErrors] = useState({});

  const load = useCallback(() => {
    adminApi
      .products()
      .then((r) => setProducts(r.content))
      .catch((e) => toast(e.message, 'error'));
  }, [toast]);

  useEffect(() => {
    load();
  }, [load]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  const save = async (e) => {
    e.preventDefault();
    setErrors({});
    const body = {
      ...form,
      price: Number(form.price),
      stock: Number(form.stock),
      diameterMm: Number(form.diameterMm),
      waterResistanceM: Number(form.waterResistanceM),
    };
    try {
      if (form.id) await adminApi.updateProduct(form.id, body);
      else await adminApi.createProduct(body);
      toast(form.id ? 'Product updated' : 'Product created', 'success');
      setForm(null);
      load();
    } catch (err) {
      setErrors(err.fieldErrors ?? {});
      toast(err.message, 'error');
    }
  };

  const remove = async (p) => {
    if (!window.confirm(`Delete ${p.name}?`)) return;
    try {
      await adminApi.deleteProduct(p.id);
      toast('Product deleted', 'success');
      load();
    } catch (err) {
      toast(err.message, 'error');
    }
  };

  const field = (k, label, type = 'text') => (
    <div className="form-row">
      <label htmlFor={k}>{label}</label>
      {type === 'textarea' ? (
        <textarea id={k} value={form[k]} onChange={set(k)} rows="3" />
      ) : (
        <input id={k} type={type} value={form[k]} onChange={set(k)} />
      )}
      {errors[k] && <span className="error">{errors[k]}</span>}
    </div>
  );

  if (!products) return <Loader />;

  return (
    <div>
      {form ? (
        <form className="box" onSubmit={save}>
          <h3>{form.id ? `Edit Product #${form.id}` : 'Add New Product'}</h3>
          <div className="form-grid">
            {field('name', 'Name')}
            {field('collection', 'Collection')}
            {field('price', 'Price (INR)', 'number')}
            {field('stock', 'Stock', 'number')}
            {field('caseMaterial', 'Case Material')}
            {field('movement', 'Movement')}
            {field('diameterMm', 'Diameter (mm)', 'number')}
            {field('waterResistanceM', 'Water Resistance (m)', 'number')}
            {field('imageUrl', 'Image URL')}
            {field('hoverImageUrl', 'Second Image URL')}
            {field('caseColor', 'Case Color', 'color')}
            {field('dialColor', 'Dial Color', 'color')}
            {field('strapColor', 'Strap Color', 'color')}
            {field('accentColor', 'Accent Color', 'color')}
          </div>
          {field('description', 'Description', 'textarea')}
          <label>
            <input type="checkbox" checked={form.customizable} onChange={set('customizable')} /> Customizable
          </label>
          <p>
            <button type="submit">Save</button>{' '}
            <button type="button" className="secondary" onClick={() => setForm(null)}>
              Cancel
            </button>
          </p>
        </form>
      ) : (
        <p>
          <button onClick={() => setForm(BLANK)}>+ Add Product</button>
        </p>
      )}

      <table className="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Image</th>
            <th>Name</th>
            <th>Collection</th>
            <th>Price</th>
            <th>Stock</th>
            <th>Active</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {products.map((p) => (
            <tr key={p.id} className={p.active ? '' : 'inactive'}>
              <td>{p.id}</td>
              <td>
                <img className="thumb" src={img(p.imageUrl, 100)} alt="" />
              </td>
              <td>{p.name}</td>
              <td>{p.collection}</td>
              <td>{formatPrice(p.price)}</td>
              <td>{p.stock}</td>
              <td>{p.active ? 'Yes' : 'No'}</td>
              <td>
                <button
                  className="secondary"
                  onClick={() => {
                    setErrors({});
                    setForm({ ...BLANK, ...p, imageUrl: p.imageUrl ?? '', hoverImageUrl: p.hoverImageUrl ?? '' });
                    window.scrollTo(0, 0);
                  }}
                >
                  Edit
                </button>{' '}
                {p.active && (
                  <button className="danger" onClick={() => remove(p)}>
                    Delete
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function OrdersAdmin() {
  const toast = useToast();
  const [status, setStatus] = useState('');
  const [orders, setOrders] = useState(null);

  useEffect(() => {
    setOrders(null);
    adminApi
      .orders(status)
      .then((r) => setOrders(r.content))
      .catch((e) => toast(e.message, 'error'));
  }, [status, toast]);

  const changeStatus = async (o, next) => {
    try {
      const updated = await adminApi.setOrderStatus(o.id, next);
      setOrders(orders.map((x) => (x.id === o.id ? updated : x)));
      toast(`Order ${o.orderNumber} is now ${next}`, 'success');
    } catch (e) {
      toast(e.message, 'error');
    }
  };

  return (
    <div>
      <p>
        Filter by status:{' '}
        <select value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">All</option>
          {['PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'].map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
      </p>
      {!orders ? (
        <Loader />
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>Order No</th>
              <th>Date</th>
              <th>Customer</th>
              <th>Total</th>
              <th>Status</th>
              <th>Change Status</th>
            </tr>
          </thead>
          <tbody>
            {orders.length === 0 && (
              <tr>
                <td colSpan="6">No orders found.</td>
              </tr>
            )}
            {orders.map((o) => (
              <tr key={o.id}>
                <td>{o.orderNumber}</td>
                <td>{formatDate(o.createdAt)}</td>
                <td>{o.shipping.fullName}</td>
                <td>{formatPrice(o.totalAmount)}</td>
                <td>
                  <StatusBadge status={o.status} />
                </td>
                <td>
                  {(NEXT_STATUS[o.status] ?? []).map((n) => (
                    <button key={n} className={n === 'CANCELLED' ? 'danger' : 'secondary'} onClick={() => changeStatus(o, n)}>
                      {n}
                    </button>
                  ))}
                  {!NEXT_STATUS[o.status] && '-'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
