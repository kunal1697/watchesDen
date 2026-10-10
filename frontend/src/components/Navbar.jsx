import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();

  return (
    <nav className="navbar">
      <Link to="/" className="brand">
        WatchesDen
      </Link>
      <div className="nav-links">
        <Link to="/">Products</Link>
        <Link to="/customize">Customize</Link>
        {user && <Link to="/cart">Cart ({cart.itemCount})</Link>}
        {user && <Link to="/orders">My Orders</Link>}
        {isAdmin && <Link to="/admin">Admin</Link>}
      </div>
      <div className="nav-user">
        {user ? (
          <>
            <span>
              Hi, {user.fullName} {isAdmin && '(admin)'}
            </span>
            <button
              onClick={() => {
                logout();
                navigate('/login');
              }}
            >
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
