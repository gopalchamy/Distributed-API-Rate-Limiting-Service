import React, { useState } from 'react';
import { Play, Flame, RefreshCw, Send, CheckCircle2, XCircle, Terminal, Layers, Info } from 'lucide-react';
import { api } from '../services/api';

export default function RateLimitSimulator({
  apps = [],
  onTrafficEvent
}) {
  const [selectedEndpoint, setSelectedEndpoint] = useState('/api/v1/gateway/products');
  const [selectedMethod, setSelectedMethod] = useState('GET');
  const [selectedApiKey, setSelectedApiKey] = useState('rl_live_ecommerce_demo_key_771');
  const [burstCount, setBurstCount] = useState(10);
  const [isRunning, setIsRunning] = useState(false);
  const [results, setResults] = useState([]);
  const [latestResponse, setLatestResponse] = useState(null);

  const endpoints = [
    { path: '/api/v1/gateway/products', method: 'GET', label: 'GET /products (Catalogue Listing)' },
    { path: '/api/v1/gateway/products/101', method: 'GET', label: 'GET /products/101 (Single Product)' },
    { path: '/api/v1/gateway/orders', method: 'POST', label: 'POST /orders (Order Placement Throttle)' },
    { path: '/api/v1/gateway/search?query=macbook', method: 'GET', label: 'GET /search (Query Throttle)' },
    { path: '/api/v1/gateway/status', method: 'GET', label: 'GET /status (Health Check)' }
  ];

  const handleEndpointChange = (path) => {
    setSelectedEndpoint(path);
    const ep = endpoints.find(e => e.path === path);
    if (ep) setSelectedMethod(ep.method);
  };

  const executeSingle = async () => {
    setIsRunning(true);
    try {
      const res = await api.invokeGateway(
        selectedEndpoint,
        selectedMethod,
        selectedApiKey,
        selectedMethod === 'POST' ? { productId: 101, quantity: 1, shipping: 'express' } : null
      );
      setLatestResponse(res);
      setResults(prev => [res, ...prev.slice(0, 49)]);
      if (onTrafficEvent) onTrafficEvent(res);
    } finally {
      setIsRunning(false);
    }
  };

  const executeBurst = async (count) => {
    setIsRunning(true);
    const burstResults = [];

    for (let i = 0; i < count; i++) {
      const res = await api.invokeGateway(
        selectedEndpoint,
        selectedMethod,
        selectedApiKey,
        selectedMethod === 'POST' ? { productId: 101, quantity: 1, index: i } : null
      );
      burstResults.unshift(res);
      setLatestResponse(res);
      if (onTrafficEvent) onTrafficEvent(res);

      // Short delay between requests in burst to simulate microsecond client stream
      await new Promise(r => setTimeout(r, 40));
    }

    setResults(prev => [...burstResults, ...prev].slice(0, 50));
    setIsRunning(false);
  };

  const allowedCount = results.filter(r => r.status === 200 || r.status === 201).length;
  const blockedCount = results.filter(r => r.status === 429).length;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Top Controller Panel */}
      <div className="glass-panel" style={{ padding: '24px 28px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
              <span className="badge badge-tier-pro">Interactive Testing</span>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                Test Token Bucket & 429 Throttling In Real-Time
              </span>
            </div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff' }}>
              Traffic Throttling & Burst Load Simulator
            </h2>
          </div>

          <div style={{ display: 'flex', gap: 8 }}>
            <button
              onClick={() => setResults([])}
              className="btn btn-secondary"
              style={{ fontSize: '0.8rem' }}
            >
              <RefreshCw size={14} />
              <span>Clear History</span>
            </button>
          </div>
        </div>

        {/* Configuration Controls */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1.5fr 1.5fr 1fr',
          gap: 16,
          marginBottom: 20
        }}>
          {/* Target Endpoint */}
          <div className="form-group">
            <label className="form-label">Target Protected Endpoint</label>
            <select
              className="form-select"
              value={selectedEndpoint}
              onChange={(e) => handleEndpointChange(e.target.value)}
            >
              {endpoints.map(e => (
                <option key={e.path} value={e.path}>{e.label}</option>
              ))}
            </select>
          </div>

          {/* Client API Key */}
          <div className="form-group">
            <label className="form-label">Client Identity (API-Key or IP)</label>
            <select
              className="form-select"
              value={selectedApiKey}
              onChange={(e) => setSelectedApiKey(e.target.value)}
            >
              <option value="rl_live_ecommerce_demo_key_771">E-Commerce Web Store (Free Tier: 20 req/min)</option>
              <option value="rl_live_mobile_app_demo_key_882">Mobile App (Pro Tier: 120 req/min)</option>
              <option value="rl_live_enterprise_demo_key_993">Partner Logistics (Enterprise: 600 req/min)</option>
              {apps.flatMap(a => (a.apiKeys || []).map(k => (
                <option key={k.keyValue} value={k.keyValue}>{a.name} - {k.name} ({k.keyPrefix})</option>
              )))}
              <option value="">Anonymous IP Client (No API Key)</option>
              <option value="invalid_key_xyz_999">Invalid API Key (Test Failure Handling)</option>
            </select>
          </div>

          {/* Burst Count Selector */}
          <div className="form-group">
            <label className="form-label">Burst Volume</label>
            <div style={{ display: 'flex', gap: 6 }}>
              {[1, 10, 25, 50].map(cnt => (
                <button
                  key={cnt}
                  type="button"
                  onClick={() => setBurstCount(cnt)}
                  className={`btn ${burstCount === cnt ? 'btn-primary' : 'btn-secondary'}`}
                  style={{ flex: 1, padding: '7px 0', fontSize: '0.8rem' }}
                >
                  {cnt}x
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Action Triggers */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <button
            onClick={executeSingle}
            disabled={isRunning}
            className="btn btn-primary"
            style={{ padding: '10px 20px', fontWeight: 600 }}
          >
            <Send size={16} />
            <span>Send 1 Request</span>
          </button>

          <button
            onClick={() => executeBurst(burstCount)}
            disabled={isRunning}
            className="btn btn-danger"
            style={{ padding: '10px 20px', fontWeight: 600 }}
          >
            <Flame size={16} />
            <span>Fire Burst of {burstCount} Requests</span>
          </button>

          <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginLeft: 'auto' }}>
            Tip: Fire 25x with Free Tier (20 req/min) to trigger <strong style={{ color: '#fb7185' }}>HTTP 429 Too Many Requests</strong>!
          </div>
        </div>
      </div>

      {/* Results Inspector & Waterfall Stream */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1.2fr 1fr',
        gap: 20
      }}>
        {/* Live Waterfall Stream */}
        <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <Terminal size={18} color="#818cf8" />
              <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#fff' }}>
                Recent Responses Stream
              </h3>
            </div>

            <div style={{ display: 'flex', gap: 8, fontSize: '0.78rem' }}>
              <span className="badge badge-200">{allowedCount} 200 OK</span>
              <span className="badge badge-429">{blockedCount} 429 Throttled</span>
            </div>
          </div>

          <div style={{
            flex: 1,
            maxHeight: 380,
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            gap: 6,
            paddingRight: 4
          }}>
            {results.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                No requests fired yet. Click "Send 1 Request" or "Fire Burst" above to observe rate limit decisions.
              </div>
            ) : (
              results.map((r, idx) => {
                const is200 = r.status === 200 || r.status === 201;
                return (
                  <div
                    key={idx}
                    onClick={() => setLatestResponse(r)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      padding: '8px 12px',
                      borderRadius: 'var(--radius-sm)',
                      background: is200 ? 'rgba(16, 185, 129, 0.06)' : 'rgba(244, 63, 94, 0.08)',
                      border: `1px solid ${is200 ? 'rgba(16, 185, 129, 0.2)' : 'rgba(244, 63, 94, 0.3)'}`,
                      cursor: 'pointer',
                      transition: 'all 0.15s ease'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <span className={is200 ? 'badge badge-200' : 'badge badge-429'}>
                        {r.status} {is200 ? 'OK' : 'Too Many'}
                      </span>
                      <span style={{ fontSize: '0.8125rem', fontFamily: 'var(--font-mono)', color: '#f1f5f9' }}>
                        {selectedEndpoint.split('?')[0]}
                      </span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: 12, fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                      {r.rateLimit && (
                        <span style={{ fontFamily: 'var(--font-mono)' }}>
                          Tokens: <strong style={{ color: r.rateLimit.remaining > 0 ? '#34d399' : '#fb7185' }}>{r.rateLimit.remaining}</strong>/{r.rateLimit.limit}
                        </span>
                      )}
                      <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>{r.latencyMs}ms</span>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Latest Response Headers & Payload Inspector */}
        <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 14 }}>
            <Info size={18} color="#06b6d4" />
            <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#fff' }}>
              HTTP Rate Limit Headers & Payload
            </h3>
          </div>

          {latestResponse ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              {/* HTTP Headers Box */}
              <div>
                <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: 6, textTransform: 'uppercase' }}>
                  Response Headers (IETF RFC Rate-Limiting Standard)
                </div>
                <div style={{
                  background: 'rgba(0,0,0,0.4)',
                  padding: '12px',
                  borderRadius: 'var(--radius-md)',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.78rem',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 4
                }}>
                  <div style={{ color: '#94a3b8' }}>HTTP/1.1 {latestResponse.status} {latestResponse.statusText}</div>
                  <div style={{ color: '#38bdf8' }}>X-RateLimit-Limit: <span style={{ color: '#fff' }}>{latestResponse.rateLimit?.limit ?? 'N/A'}</span></div>
                  <div style={{ color: '#38bdf8' }}>X-RateLimit-Remaining: <span style={{ color: '#fff' }}>{latestResponse.rateLimit?.remaining ?? 'N/A'}</span></div>
                  <div style={{ color: '#38bdf8' }}>X-RateLimit-Reset: <span style={{ color: '#fff' }}>{latestResponse.rateLimit?.reset ? `${latestResponse.rateLimit.reset}s` : 'N/A'}</span></div>
                  <div style={{ color: '#38bdf8' }}>X-RateLimit-Engine: <span style={{ color: '#a855f7' }}>{latestResponse.rateLimit?.engine || 'REDIS'}</span></div>
                  {latestResponse.rateLimit?.retryAfter && (
                    <div style={{ color: '#fb7185', fontWeight: 700 }}>Retry-After: {latestResponse.rateLimit.retryAfter}s</div>
                  )}
                </div>
              </div>

              {/* JSON Body Box */}
              <div>
                <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', marginBottom: 6, textTransform: 'uppercase' }}>
                  Response Body (JSON)
                </div>
                <pre style={{
                  background: 'rgba(0,0,0,0.5)',
                  padding: '12px',
                  borderRadius: 'var(--radius-md)',
                  fontFamily: 'var(--font-mono)',
                  fontSize: '0.75rem',
                  color: '#e2e8f0',
                  maxHeight: 180,
                  overflowY: 'auto',
                  border: '1px solid var(--border-subtle)'
                }}>
                  {JSON.stringify(latestResponse.data, null, 2)}
                </pre>
              </div>
            </div>
          ) : (
            <div style={{ margin: 'auto', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.84rem' }}>
              Select a request from the stream to inspect headers and JSON payload.
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
