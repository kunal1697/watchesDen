import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { productApi } from '../api';
import Loader from '../components/Loader';
import { useCart } from '../context/CartContext';
import { formatPrice, img } from '../utils/format';

const PARTS = ['CASE', 'DIAL', 'STRAP', 'HANDS'];

export default function Customize() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { add, busy } = useCart();
  const [bases, setBases] = useState(null);
  const [options, setOptions] = useState(null);
  const [selected, setSelected] = useState({});
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([productApi.search({ customizable: true, size: 50 }), productApi.customizations()])
      .then(([products, opts]) => {
        setBases(products.content);
        setOptions(opts.parts);
      })
      .catch((e) => setError(e.message));
  }, []);

  useEffect(() => setSelected({}), [id]);

  if (error) return <p className="error">{error}</p>;
  if (!bases || !options) return <Loader />;
  if (bases.length === 0) return <p>No customizable watches available.</p>;

  const base = bases.find((b) => String(b.id) === id) ?? bases[0];
  const chosen = PARTS.map((part) => options[part]?.find((o) => o.id === selected[part])).filter(Boolean);
  const total = Number(base.price) + chosen.reduce((sum, o) => sum + Number(o.priceDelta), 0);

  return (
    <div>
      <h1>Customize Your Watch</h1>
      <div className="detail">
        <img src={img(base.imageUrl, 500)} alt={base.name} />
        <div>
          <p>
            <label>
              Base watch:{' '}
              <select value={base.id} onChange={(e) => navigate(`/customize/${e.target.value}`)}>
                {bases.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.name} ({formatPrice(b.price)})
                  </option>
                ))}
              </select>
            </label>
          </p>

          <table className="table">
            <thead>
              <tr>
                <th>Part</th>
                <th>Option</th>
                <th>Extra cost</th>
              </tr>
            </thead>
            <tbody>
              {PARTS.map((part) => {
                const opt = options[part]?.find((o) => o.id === selected[part]);
                return (
                  <tr key={part}>
                    <td>{part}</td>
                    <td>
                      <select
                        aria-label={part}
                        value={selected[part] ?? ''}
                        onChange={(e) => setSelected({ ...selected, [part]: e.target.value || undefined })}
                      >
                        <option value="">Standard</option>
                        {(options[part] ?? []).map((o) => (
                          <option key={o.id} value={o.id}>
                            {o.name}
                          </option>
                        ))}
                      </select>{' '}
                      {opt && <span className="swatch" style={{ background: opt.colorHex }} />}
                    </td>
                    <td>{opt ? formatPrice(opt.priceDelta) : '-'}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>

          <p>
            Base price: {formatPrice(base.price)}
            <br />
            <b>Total price: {formatPrice(total)}</b>
          </p>
          <button disabled={busy || !base.inStock} onClick={() => add(base.id, 1, chosen.map((o) => o.id))}>
            Add Custom Watch to Cart
          </button>
        </div>
      </div>
    </div>
  );
}
