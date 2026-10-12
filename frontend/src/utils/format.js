const money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 });

export const formatPrice = (value) => money.format(Number(value ?? 0));

export const formatDate = (iso) =>
  new Date(iso).toLocaleDateString('en-IN', { year: 'numeric', month: 'short', day: 'numeric' });

export const uuid = () =>
  globalThis.crypto?.randomUUID?.() ??
  'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
  });

const PLACEHOLDER =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 400 400'%3E%3Crect width='400' height='400' fill='%23f4f1ec'/%3E%3Ccircle cx='200' cy='200' r='70' fill='none' stroke='%23c9b48f' stroke-width='8'/%3E%3Cpath d='M200 160v40l28 18' stroke='%23c9b48f' stroke-width='8' fill='none' stroke-linecap='round'/%3E%3C/svg%3E";

// resize unsplash images, other urls are returned as is
export function img(url, width = 600) {
  if (!url) return PLACEHOLDER;
  if (url.startsWith('https://images.unsplash.com/')) {
    return `${url}${url.includes('?') ? '&' : '?'}w=${width}&q=80&auto=format&fit=crop`;
  }
  return url;
}
