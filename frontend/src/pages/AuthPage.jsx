import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validate(mode, form) {
  const errors = {};
  if (mode === 'register' && !form.fullName.trim()) errors.fullName = 'Name is required';
  if (!EMAIL.test(form.email.trim())) errors.email = 'Enter a valid email';
  if (mode === 'register') {
    if (form.password.length < 8) errors.password = 'Password must be at least 8 characters';
    else if (!/[A-Za-z]/.test(form.password) || !/\d/.test(form.password)) errors.password = 'Password must contain letters and digits';
  } else if (!form.password) {
    errors.password = 'Password is required';
  }
  return errors;
}

export default function AuthPage({ mode = 'login' }) {
  const { login, register } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ fullName: '', email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const isRegister = mode === 'register';

  const submit = async (e) => {
    e.preventDefault();
    const v = validate(mode, form);
    setErrors(v);
    if (Object.keys(v).length) return;
    setBusy(true);
    try {
      if (isRegister) await register(form.fullName.trim(), form.email.trim(), form.password);
      else await login(form.email.trim(), form.password);
      toast(isRegister ? 'Registration successful' : 'Login successful', 'success');
      navigate(location.state?.from ?? '/', { replace: true });
    } catch (err) {
      setErrors(err.fieldErrors ?? {});
      toast(err.message, 'error');
    } finally {
      setBusy(false);
    }
  };

  const field = (name, label, type = 'text') => (
    <div className="form-row">
      <label htmlFor={name}>{label}</label>
      <input id={name} type={type} value={form[name]} onChange={(e) => setForm({ ...form, [name]: e.target.value })} />
      {errors[name] && <span className="error">{errors[name]}</span>}
    </div>
  );

  return (
    <div className="auth-box box">
      <h1>{isRegister ? 'Register' : 'Login'}</h1>
      <form onSubmit={submit} noValidate>
        {isRegister && field('fullName', 'Full Name')}
        {field('email', 'Email', 'email')}
        {field('password', 'Password', 'password')}
        <button type="submit" disabled={busy}>
          {busy ? 'Please wait...' : isRegister ? 'Register' : 'Login'}
        </button>
      </form>
      <p>
        {isRegister ? (
          <>
            Already registered? <Link to="/login" state={location.state}>Login here</Link>
          </>
        ) : (
          <>
            New user? <Link to="/register" state={location.state}>Register here</Link>
          </>
        )}
      </p>
      {!isRegister && <p className="muted">Admin demo login: admin@watchesden.com / Admin@123</p>}
    </div>
  );
}
