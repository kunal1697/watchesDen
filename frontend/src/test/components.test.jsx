import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import ProductCard from '../components/ProductCard';
import { validate } from '../pages/AuthPage';
import { StatusBadge } from '../pages/Orders';

const product = {
  id: 7,
  name: 'Abyss Diver 300',
  collection: 'Abyss',
  price: 112900,
  stock: 30,
  inStock: true,
  customizable: true,
  movement: 'Automatic',
  imageUrl: 'https://images.unsplash.com/photo-a',
};

describe('ProductCard', () => {
  it('shows image, name, price, stock and a link to details', () => {
    render(
      <MemoryRouter>
        <ProductCard product={product} />
      </MemoryRouter>,
    );
    expect(screen.getByRole('img', { name: 'Abyss Diver 300' })).toHaveAttribute('src', expect.stringContaining('photo-a'));
    expect(screen.getByText('₹1,12,900')).toBeInTheDocument();
    expect(screen.getByText('In stock (30)')).toBeInTheDocument();
    expect(screen.getByText('Customizable')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'View Details' })).toHaveAttribute('href', '/product/7');
  });

  it('shows out of stock', () => {
    render(
      <MemoryRouter>
        <ProductCard product={{ ...product, inStock: false, stock: 0, customizable: false }} />
      </MemoryRouter>,
    );
    expect(screen.getByText('Out of stock')).toBeInTheDocument();
    expect(screen.queryByText('Customizable')).toBeNull();
  });
});

describe('StatusBadge', () => {
  it('renders the status with a status-specific class', () => {
    render(<StatusBadge status="SHIPPED" />);
    expect(screen.getByText('SHIPPED')).toHaveClass('status-shipped');
  });
});

describe('auth form validation', () => {
  it('requires name, valid email and a strong password on register', () => {
    const errors = validate('register', { fullName: ' ', email: 'x@', password: 'short' });
    expect(Object.keys(errors).sort()).toEqual(['email', 'fullName', 'password']);
    expect(validate('register', { fullName: 'A', email: 'a@b.co', password: 'longpassword' }).password).toBe(
      'Password must contain letters and digits',
    );
  });

  it('accepts a valid login', () => {
    expect(validate('login', { fullName: '', email: 'a@b.co', password: 'x' })).toEqual({});
  });
});
