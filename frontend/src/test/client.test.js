import { describe, expect, it, vi } from 'vitest';
import { ApiError, request, setUnauthorizedHandler, tokenStore } from '../api/client';

const respond = (status, body, headers = {}) =>
  vi.spyOn(globalThis, 'fetch').mockResolvedValue(
    new Response(body === undefined ? null : JSON.stringify(body), {
      status,
      headers: { 'Content-Type': 'application/json', ...headers },
    }),
  );

describe('request', () => {
  it('sends the bearer token and JSON body', async () => {
    tokenStore.set('abc');
    const spy = respond(200, { ok: true });

    const res = await request('/api/cart/items', { method: 'POST', body: { productId: 1 } });

    expect(res).toEqual({ ok: true });
    const [url, init] = spy.mock.calls[0];
    expect(url).toBe('/api/cart/items');
    expect(init.headers.Authorization).toBe('Bearer abc');
    expect(init.headers['Content-Type']).toBe('application/json');
    expect(JSON.parse(init.body)).toEqual({ productId: 1 });
  });

  it('omits the token for anonymous calls', async () => {
    tokenStore.set('abc');
    const spy = respond(200, {});
    await request('/api/auth/login', { method: 'POST', body: {}, auth: false });
    expect(spy.mock.calls[0][1].headers.Authorization).toBeUndefined();
  });

  it('maps backend errors to ApiError with field errors and correlation id', async () => {
    respond(400, { message: 'Validation failed', fieldErrors: { email: 'bad' } }, { 'X-Correlation-Id': 'cid-1' });

    const err = await request('/api/auth/register', { method: 'POST', body: {} }).catch((e) => e);

    expect(err).toBeInstanceOf(ApiError);
    expect(err.status).toBe(400);
    expect(err.message).toBe('Validation failed');
    expect(err.fieldErrors).toEqual({ email: 'bad' });
    expect(err.correlationId).toBe('cid-1');
  });

  it('calls the unauthorized handler on 401 when logged in', async () => {
    tokenStore.set('expired');
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    respond(401, { message: 'Authentication required' });

    await expect(request('/api/cart')).rejects.toThrow('Authentication required');
    expect(handler).toHaveBeenCalled();
  });

  it('returns null for 204', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 204 }));
    expect(await request('/api/cart', { method: 'DELETE' })).toBeNull();
  });

  it('reports network failures clearly', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new TypeError('Failed to fetch'));
    const err = await request('/api/products').catch((e) => e);
    expect(err.status).toBe(0);
    expect(err.message).toMatch(/Cannot reach the server/);
  });
});
