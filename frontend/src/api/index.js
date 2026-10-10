import { request } from './client';

const qs = (params) => {
  const p = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') p.set(k, v);
  });
  const s = p.toString();
  return s ? `?${s}` : '';
};

export const authApi = {
  login: (email, password) => request('/api/auth/login', { method: 'POST', body: { email, password }, auth: false }),
  register: (fullName, email, password) =>
    request('/api/auth/register', { method: 'POST', body: { fullName, email, password }, auth: false }),
  me: () => request('/api/users/me'),
};

export const productApi = {
  search: (params = {}) => request(`/api/products${qs(params)}`),
  get: (id) => request(`/api/products/${id}`),
  collections: () => request('/api/products/collections'),
  customizations: () => request('/api/customizations'),
};

export const cartApi = {
  get: () => request('/api/cart'),
  add: (productId, quantity, optionIds = []) =>
    request('/api/cart/items', { method: 'POST', body: { productId, quantity, optionIds } }),
  update: (itemId, quantity) => request(`/api/cart/items/${itemId}`, { method: 'PATCH', body: { quantity } }),
  remove: (itemId) => request(`/api/cart/items/${itemId}`, { method: 'DELETE' }),
  clear: () => request('/api/cart', { method: 'DELETE' }),
};

export const orderApi = {
  checkout: (payload, idempotencyKey) =>
    request('/api/orders/checkout', {
      method: 'POST',
      body: payload,
      headers: { 'Idempotency-Key': idempotencyKey },
    }),
  list: (page = 0) => request(`/api/orders${qs({ page, size: 20 })}`),
  get: (id) => request(`/api/orders/${id}`),
  cancel: (id) => request(`/api/orders/${id}/cancel`, { method: 'POST' }),
};

export const adminApi = {
  products: () => request('/api/admin/products?size=50'),
  createProduct: (p) => request('/api/admin/products', { method: 'POST', body: p }),
  updateProduct: (id, p) => request(`/api/admin/products/${id}`, { method: 'PUT', body: p }),
  deleteProduct: (id) => request(`/api/admin/products/${id}`, { method: 'DELETE' }),
  orders: (status) => request(`/api/admin/orders${qs({ status, size: 50 })}`),
  setOrderStatus: (id, status) => request(`/api/admin/orders/${id}/status`, { method: 'PATCH', body: { status } }),
};
