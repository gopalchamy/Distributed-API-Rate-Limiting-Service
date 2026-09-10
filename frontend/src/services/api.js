// API Client for Distributed Rate Limiter Backend

const BASE_URL = '';

function getAuthHeader() {
  const token = localStorage.getItem('nexus_auth_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const api = {
  // Auth
  async login(email, password) {
    const res = await fetch(`${BASE_URL}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Invalid email or password');
    }
    const data = await res.json();
    localStorage.setItem('nexus_auth_token', data.token);
    localStorage.setItem('nexus_user', JSON.stringify(data));
    return data;
  },

  async register(email, fullName, password) {
    const res = await fetch(`${BASE_URL}/api/v1/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, fullName, password })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Registration failed');
    }
    const data = await res.json();
    localStorage.setItem('nexus_auth_token', data.token);
    localStorage.setItem('nexus_user', JSON.stringify(data));
    return data;
  },

  logout() {
    localStorage.removeItem('nexus_auth_token');
    localStorage.removeItem('nexus_user');
  },

  getCurrentUser() {
    try {
      return JSON.parse(localStorage.getItem('nexus_user'));
    } catch (e) {
      return null;
    }
  },

  // Applications
  async getApplications() {
    const res = await fetch(`${BASE_URL}/api/v1/apps`, {
      headers: { ...getAuthHeader() }
    });
    if (!res.ok) throw new Error('Failed to fetch applications');
    return res.json();
  },

  async createApplication(name, description, planTier) {
    const res = await fetch(`${BASE_URL}/api/v1/apps`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
      body: JSON.stringify({ name, description, planTier })
    });
    if (!res.ok) throw new Error('Failed to create application');
    return res.json();
  },

  async generateApiKey(appId, keyName) {
    const res = await fetch(`${BASE_URL}/api/v1/apps/${appId}/keys`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
      body: JSON.stringify({ keyName })
    });
    if (!res.ok) throw new Error('Failed to generate key');
    return res.json();
  },

  async toggleApiKey(keyId) {
    const res = await fetch(`${BASE_URL}/api/v1/apps/keys/${keyId}/toggle`, {
      method: 'PATCH',
      headers: { ...getAuthHeader() }
    });
    if (!res.ok) throw new Error('Failed to toggle key');
  },

  async deleteApplication(appId) {
    const res = await fetch(`${BASE_URL}/api/v1/apps/${appId}`, {
      method: 'DELETE',
      headers: { ...getAuthHeader() }
    });
    if (!res.ok) throw new Error('Failed to delete application');
  },

  // Rules
  async getRules(appId = null) {
    const url = appId ? `${BASE_URL}/api/v1/rules?appId=${appId}` : `${BASE_URL}/api/v1/rules`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch rules');
    return res.json();
  },

  async createRule(rule) {
    const res = await fetch(`${BASE_URL}/api/v1/rules`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeader() },
      body: JSON.stringify(rule)
    });
    if (!res.ok) throw new Error('Failed to create rule');
    return res.json();
  },

  async deleteRule(ruleId) {
    const res = await fetch(`${BASE_URL}/api/v1/rules/${ruleId}`, {
      method: 'DELETE',
      headers: { ...getAuthHeader() }
    });
    if (!res.ok) throw new Error('Failed to delete rule');
  },

  // Analytics
  async getAnalyticsSummary() {
    const res = await fetch(`${BASE_URL}/api/v1/analytics/summary`);
    if (!res.ok) throw new Error('Failed to fetch analytics');
    return res.json();
  },

  async getAuditLogs(page = 0, size = 25, appId = null) {
    let url = `${BASE_URL}/api/v1/analytics/logs?page=${page}&size=${size}`;
    if (appId) url += `&appId=${appId}`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('Failed to fetch logs');
    return res.json();
  },

  // Test / Rate Limit Gateway Call
  async invokeGateway(endpoint, method = 'GET', apiKey = '', payload = null) {
    const headers = {};
    if (apiKey) {
      headers['X-API-KEY'] = apiKey;
    }
    if (payload && method !== 'GET') {
      headers['Content-Type'] = 'application/json';
    }

    const startTime = performance.now();
    try {
      const res = await fetch(`${BASE_URL}${endpoint}`, {
        method,
        headers,
        body: payload && method !== 'GET' ? JSON.stringify(payload) : undefined
      });
      const latencyMs = Math.round(performance.now() - startTime);

      const limit = res.headers.get('X-RateLimit-Limit');
      const remaining = res.headers.get('X-RateLimit-Remaining');
      const reset = res.headers.get('X-RateLimit-Reset');
      const engine = res.headers.get('X-RateLimit-Engine') || 'REDIS';
      const retryAfter = res.headers.get('Retry-After');

      const data = await res.json().catch(() => ({}));

      return {
        ok: res.ok,
        status: res.status,
        statusText: res.statusText,
        data,
        latencyMs,
        rateLimit: {
          limit: limit ? parseInt(limit, 10) : null,
          remaining: remaining !== null ? parseInt(remaining, 10) : null,
          reset: reset ? parseInt(reset, 10) : null,
          retryAfter: retryAfter ? parseInt(retryAfter, 10) : null,
          engine
        }
      };
    } catch (err) {
      return {
        ok: false,
        status: 0,
        statusText: 'Network Error',
        data: { message: err.message },
        latencyMs: Math.round(performance.now() - startTime),
        rateLimit: null
      };
    }
  }
};
