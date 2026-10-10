import { describe, expect, it } from 'vitest';
import { formatPrice, img, uuid } from '../utils/format';

describe('formatPrice', () => {
  it('formats rupees with Indian digit grouping', () => {
    expect(formatPrice(124900)).toBe('₹1,24,900');
    expect(formatPrice('59900.00')).toBe('₹59,900');
  });
});

describe('img', () => {
  it('adds sizing parameters to Unsplash URLs', () => {
    expect(img('https://images.unsplash.com/photo-1', 400)).toBe(
      'https://images.unsplash.com/photo-1?w=400&q=80&auto=format&fit=crop',
    );
  });

  it('leaves other URLs untouched and falls back to a placeholder', () => {
    expect(img('https://cdn.example.com/a.jpg')).toBe('https://cdn.example.com/a.jpg');
    expect(img(null)).toMatch(/^data:image\/svg\+xml/);
  });
});

describe('uuid', () => {
  it('generates distinct ids', () => {
    expect(uuid()).not.toBe(uuid());
  });
});
