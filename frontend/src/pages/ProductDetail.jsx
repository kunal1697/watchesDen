import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { productApi } from '../api';
import Loader from '../components/Loader';
import { useCart } from '../context/CartContext';
import { formatPrice, img } from '../utils/format';

export default function ProductDetail() {
  const { id } = useParams();
  const { add, busy } = useCart();
  const [product, setProduct] = useState(null);
  const [error, setError] = useState(null);
  const [qty, setQty] = useState(1);

  useEffect(() => {
    setProduct(null);
    setError(null);
    productApi.get(id).then(setProduct).catch((e) => setError(e.message));
  }, [id]);

  if (error) {
    return (
      <div>
        <p className="error">{error}</p>
        <Link to="/">Back to products</Link>
      </div>
    );
  }
  if (!product) return <Loader />;

  return (
    <div>
      <Link to="/">&larr; Back to products</Link>
      <h1>{product.name}</h1>
      <div className="detail">
        <img src={img(product.imageUrl, 500)} alt={product.name} />
        <div>
          <table className="table">
            <tbody>
              <tr><th>Price</th><td>{formatPrice(product.price)}</td></tr>
              <tr><th>Collection</th><td>{product.collection}</td></tr>
              <tr><th>Movement</th><td>{product.movement}</td></tr>
              <tr><th>Case material</th><td>{product.caseMaterial}</td></tr>
              <tr><th>Diameter</th><td>{product.diameterMm} mm</td></tr>
              <tr><th>Water resistance</th><td>{product.waterResistanceM} m</td></tr>
              <tr><th>Stock</th><td>{product.stock}</td></tr>
              <tr><th>Customizable</th><td>{product.customizable ? 'Yes' : 'No'}</td></tr>
            </tbody>
          </table>
          <p>{product.description}</p>

          <label>
            Quantity:{' '}
            <input type="number" min="1" max="10" value={qty} onChange={(e) => setQty(Math.min(10, Math.max(1, Number(e.target.value) || 1)))} style={{ width: 70 }} />
          </label>{' '}
          <button disabled={!product.inStock || busy} onClick={() => add(product.id, qty)}>
            {product.inStock ? 'Add to Cart' : 'Out of Stock'}
          </button>{' '}
          {product.customizable && (
            <Link to={`/customize/${product.id}`} className="button secondary">
              Customize
            </Link>
          )}
        </div>
      </div>
    </div>
  );
}
