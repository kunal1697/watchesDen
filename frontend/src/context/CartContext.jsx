import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { cartApi } from '../api';
import { useAuth } from './AuthContext';
import { useToast } from './ToastContext';

const EMPTY = { items: [], itemCount: 0, subtotal: 0 };
const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { user } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [cart, setCart] = useState(EMPTY);
  const [busy, setBusy] = useState(false);

  const refresh = useCallback(() => {
    if (!user) {
      setCart(EMPTY);
      return Promise.resolve();
    }
    return cartApi.get().then(setCart).catch(() => setCart(EMPTY));
  }, [user]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const run = useCallback(
    async (fn, successMsg) => {
      setBusy(true);
      try {
        const next = await fn();
        if (next) setCart(next);
        if (successMsg) toast(successMsg, 'success');
        return true;
      } catch (e) {
        toast(e.message, 'error');
        return false;
      } finally {
        setBusy(false);
      }
    },
    [toast],
  );

  const add = useCallback(
    (productId, quantity = 1, optionIds = []) => {
      if (!user) {
        toast('Please login first', 'info');
        navigate('/login', { state: { from: location.pathname + location.search } });
        return Promise.resolve(false);
      }
      return run(() => cartApi.add(productId, quantity, optionIds), 'Added to cart');
    },
    [user, run, toast, navigate, location],
  );

  const value = useMemo(
    () => ({
      cart,
      busy,
      refresh,
      add,
      update: (id, qty) => run(() => cartApi.update(id, qty)),
      remove: (id) => run(() => cartApi.remove(id)),
      reset: () => setCart(EMPTY),
    }),
    [cart, busy, refresh, add, run],
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export const useCart = () => useContext(CartContext);
