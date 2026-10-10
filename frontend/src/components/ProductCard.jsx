import { Link } from 'react-router-dom';
import { formatPrice, img } from '../utils/format';

export default function ProductCard({ product }) {
  return (
    <div className="card">
      <img src={img(product.imageUrl, 300)} alt={product.name} />
      <h3>{product.name}</h3>
      <p className="muted">
        {product.collection} | {product.movement}
      </p>
      <p>
        <b>{formatPrice(product.price)}</b>
      </p>
      <p className={product.inStock ? 'green' : 'red'}>{product.inStock ? `In stock (${product.stock})` : 'Out of stock'}</p>
      {product.customizable && <p className="muted">Customizable</p>}
      <Link to={`/product/${product.id}`} className="button">
        View Details
      </Link>
    </div>
  );
}
