import { Navigate, Route, Routes } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Admin from './pages/Admin';
import AuthPage from './pages/AuthPage';
import Cart from './pages/Cart';
import Checkout from './pages/Checkout';
import Customize from './pages/Customize';
import NotFound from './pages/NotFound';
import Orders from './pages/Orders';
import ProductDetail from './pages/ProductDetail';
import Products from './pages/Products';

export default function App() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/" element={<Products />} />
          <Route path="/shop" element={<Navigate to="/" replace />} />
          <Route path="/product/:id" element={<ProductDetail />} />
          <Route path="/customize" element={<Customize />} />
          <Route path="/customize/:id" element={<Customize />} />
          <Route path="/login" element={<AuthPage mode="login" />} />
          <Route path="/register" element={<AuthPage mode="register" />} />
          <Route path="/cart" element={<ProtectedRoute><Cart /></ProtectedRoute>} />
          <Route path="/checkout" element={<ProtectedRoute><Checkout /></ProtectedRoute>} />
          <Route path="/orders" element={<ProtectedRoute><Orders /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute admin><Admin /></ProtectedRoute>} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <footer className="footer">WatchesDen - Full Stack Assessment Project (React + Spring Boot Microservices)</footer>
    </>
  );
}
