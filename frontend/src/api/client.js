const BASE = import.meta.env.VITE_API_BASE ?? '';
const TOKEN_KEY = 'aeon.token';

export class ApiError extends Error {
  constructor(status, message, fieldErrors, correlationId) {
    super(message);
    this.status = status;
    this.fieldErrors = fieldErrors ?? {};
    this.correlationId = correlationId;
  }
}

export const tokenStore = {
  get() {
    try {
      return localStorage.getItem(TOKEN_KEY);
    } catch {
      return null;
    }
  },
  set(token) {
    try {
      if (token) localStorage.setItem(TOKEN_KEY, token);
      else localStorage.removeItem(TOKEN_KEY);
    } catch {
      // localStorage not available
    }
  },
};

let onUnauthorized = () => {};
export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn;
}

export async function request(path, { method = 'GET', body, headers = {}, auth = true } = {}) {
  const h = { Accept: 'application/json', ...headers };
  if (body !== undefined) h['Content-Type'] = 'application/json';
  const token = tokenStore.get();
  if (auth && token) h.Authorization = `Bearer ${token}`;

  let res;
  try {
    res = await fetch(`${BASE}${path}`, {
      method,
      headers: h,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Is the backend running?');
  }

  if (res.status === 204) return null;
  const text = await res.text();
  const data = text ? safeJson(text) : null;

  if (!res.ok) {
    if (res.status === 401 && token) onUnauthorized();
    throw new ApiError(
      res.status,
      data?.message ?? `Request failed (${res.status})`,
      data?.fieldErrors,
      res.headers.get('X-Correlation-Id') ?? data?.correlationId,
    );
  }
  return data;
}

function safeJson(text) {
  try {
    return JSON.parse(text);
  } catch {
    return { message: text };
  }
}
