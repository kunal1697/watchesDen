import { useEffect, useState } from 'react';
import { productApi } from '../api';
import Loader from '../components/Loader';
import ProductCard from '../components/ProductCard';

const EMPTY_FILTERS = { q: '', collection: '', minPrice: '', maxPrice: '', customizable: false, sort: 'id,asc' };

export default function Products() {
  const [collections, setCollections] = useState([]);
  const [form, setForm] = useState(EMPTY_FILTERS);
  const [filters, setFilters] = useState(EMPTY_FILTERS);
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    productApi.collections().then(setCollections).catch(() => {});
  }, []);

  useEffect(() => {
    setError(null);
    productApi
      .search({ ...filters, customizable: filters.customizable || undefined, page, size: 8 })
      .then(setResult)
      .catch((e) => setError(e.message));
  }, [filters, page]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  const search = (e) => {
    e.preventDefault();
    setPage(0);
    setFilters(form);
  };

  return (
    <div>
      <h1>Products</h1>

      <form className="filters" onSubmit={search}>
        <input placeholder="Search..." value={form.q} onChange={set('q')} aria-label="Search" />
        <select value={form.collection} onChange={set('collection')} aria-label="Collection">
          <option value="">All collections</option>
          {collections.map((c) => (
            <option key={c}>{c}</option>
          ))}
        </select>
        <input type="number" placeholder="Min price" value={form.minPrice} onChange={set('minPrice')} aria-label="Min price" />
        <input type="number" placeholder="Max price" value={form.maxPrice} onChange={set('maxPrice')} aria-label="Max price" />
        <select value={form.sort} onChange={set('sort')} aria-label="Sort">
          <option value="id,asc">Default</option>
          <option value="price,asc">Price low to high</option>
          <option value="price,desc">Price high to low</option>
          <option value="name,asc">Name A-Z</option>
        </select>
        <label>
          <input type="checkbox" checked={form.customizable} onChange={set('customizable')} /> Customizable only
        </label>
        <button type="submit">Search</button>
        <button
          type="button"
          className="secondary"
          onClick={() => {
            setForm(EMPTY_FILTERS);
            setFilters(EMPTY_FILTERS);
            setPage(0);
          }}
        >
          Reset
        </button>
      </form>

      {error && <p className="error">{error}</p>}
      {!result && !error && <Loader />}
      {result && (
        <>
          <p>Total products: {result.totalElements}</p>
          {result.content.length === 0 && <p>No products found.</p>}
          <div className="grid">
            {result.content.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
          <div className="pagination">
            <button disabled={page === 0} onClick={() => setPage(page - 1)}>
              Previous
            </button>
            <span>
              Page {result.totalPages === 0 ? 0 : page + 1} of {result.totalPages}
            </span>
            <button disabled={page + 1 >= result.totalPages} onClick={() => setPage(page + 1)}>
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
}
