const API_BASE = import.meta.env.VITE_API_URL || '';

function getToken() {
  return localStorage.getItem('token');
}

export async function api(path, options = {}) {
  const token = getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  const url = API_BASE + path;
  const res = await fetch(url, { ...options, headers });
  if (res.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/';
    return;
  }
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function googleLogin(idToken) {
  return api('/api/auth/google', {
    method: 'POST',
    body: JSON.stringify({ idToken }),
  });
}

export async function getMe() {
  return api('/api/me');
}

export async function getExpenses(params = {}) {
  const q = new URLSearchParams(params).toString();
  return api('/api/expenses' + (q ? '?' + q : ''));
}

export async function getExpense(id) {
  return api('/api/expenses/' + id);
}

export async function getExpenseSummary(month) {
  return api('/api/expenses/summary' + (month ? '?month=' + encodeURIComponent(month) : ''));
}

export async function createExpense(data) {
  return api('/api/expenses', { method: 'POST', body: JSON.stringify(data) });
}

export async function updateExpense(id, data) {
  return api('/api/expenses/' + id, { method: 'PUT', body: JSON.stringify(data) });
}

export async function deleteExpense(id) {
  return api('/api/expenses/' + id, { method: 'DELETE' });
}

export async function getAdminUsers() {
  return api('/api/admin/users');
}

export async function getAdminLogins(limit = 50, offset = 0) {
  return api(`/api/admin/logins?limit=${limit}&offset=${offset}`);
}
