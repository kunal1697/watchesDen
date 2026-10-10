import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('../api', () => ({
  productApi: {
    search: vi.fn(),
    collections: vi.fn(),
  },
}));

const { productApi } = await import('../api');
const { default: Products } = await import('../pages/Products');

const watch = (id, name, extra = {}) => ({
  id,
  name,
  collection: 'Nebula',
  price: 124900,
  stock: 20,
  inStock: true,
  customizable: true,
  movement: 'Automatic',
  imageUrl: 'https://images.unsplash.com/photo-x',
  ...extra,
});

const renderPage = () =>
  render(
    <MemoryRouter>
      <Products />
    </MemoryRouter>,
  );

describe('Products page', () => {
  beforeEach(() => {
    productApi.collections.mockResolvedValue(['Nebula', 'Abyss']);
    productApi.search.mockResolvedValue({
      content: [watch(1, 'Nebula Chrono X1'), watch(2, 'Abyss Trench', { inStock: false, stock: 0 })],
      page: 0,
      size: 8,
      totalElements: 2,
      totalPages: 1,
    });
  });

  it('lists products returned by the API', async () => {
    renderPage();
    expect(await screen.findByText('Nebula Chrono X1')).toBeInTheDocument();
    expect(screen.getByText('Abyss Trench')).toBeInTheDocument();
    expect(screen.getByText('Total products: 2')).toBeInTheDocument();
    expect(screen.getByText('Out of stock')).toBeInTheDocument();
  });

  it('sends search filters to the API when Search is clicked', async () => {
    renderPage();
    await screen.findByText('Nebula Chrono X1');

    await userEvent.type(screen.getByLabelText('Search'), 'diver');
    await userEvent.selectOptions(screen.getByLabelText('Collection'), 'Abyss');
    await userEvent.type(screen.getByLabelText('Max price'), '150000');
    await userEvent.click(screen.getByRole('button', { name: 'Search' }));

    await waitFor(() =>
      expect(productApi.search).toHaveBeenLastCalledWith(
        expect.objectContaining({ q: 'diver', collection: 'Abyss', maxPrice: '150000', page: 0 }),
      ),
    );
  });

  it('shows the error message when the API fails', async () => {
    productApi.search.mockRejectedValue(new Error('Cannot reach the server'));
    renderPage();
    expect(await screen.findByText('Cannot reach the server')).toBeInTheDocument();
  });
});
