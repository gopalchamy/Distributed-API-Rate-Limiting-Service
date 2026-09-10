import React, { useState } from 'react';
import { Sliders, Plus, Trash2, Shield, ArrowRight, Zap, RefreshCw } from 'lucide-react';
import { api } from '../services/api';

export default function RuleConfigurator({
  rules = [],
  apps = [],
  onRefresh,
  user,
  onOpenAuth
}) {
  const [showAddModal, setShowAddModal] = useState(false);
  const [endpointPattern, setEndpointPattern] = useState('/api/v1/gateway/orders');
  const [httpMethod, setHttpMethod] = useState('POST');
  const [capacity, setCapacity] = useState(10);
  const [refillTokens, setRefillTokens] = useState(10);
  const [refillPeriod, setRefillPeriod] = useState(60);
  const [description, setDescription] = useState('Throttle expensive checkout/order transactions');
  const [selectedAppId, setSelectedAppId] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleCreateRule = async (e) => {
    e.preventDefault();
    if (!user) {
      onOpenAuth();
      return;
    }
    setIsSubmitting(true);
    try {
      await api.createRule({
        applicationId: selectedAppId ? parseInt(selectedAppId, 10) : null,
        endpointPattern,
        httpMethod,
        capacity: parseInt(capacity, 10),
        refillTokens: parseInt(refillTokens, 10),
        refillPeriodSeconds: parseInt(refillPeriod, 10),
        description
      });
      setShowAddModal(false);
      onRefresh();
    } catch (err) {
      alert(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteRule = async (ruleId) => {
    if (confirm('Delete this rate limiting rule?')) {
      try {
        await api.deleteRule(ruleId);
        onRefresh();
      } catch (err) {
        alert(err.message);
      }
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div className="glass-panel" style={{ padding: '24px 28px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
            <span className="badge badge-tier-enterprise">Dynamic Policy Engine</span>
          </div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff' }}>
            Token Bucket Rate-Limit Rules
          </h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: 4 }}>
            Configure granular burst capacities, refill rates, and route matching patterns.
          </p>
        </div>

        <button
          onClick={() => user ? setShowAddModal(true) : onOpenAuth()}
          className="btn btn-primary"
          style={{ padding: '10px 18px' }}
        >
          <Plus size={16} />
          <span>Add Custom Rule</span>
        </button>
      </div>

      {/* Rules Table */}
      <div className="glass-panel" style={{ padding: '20px', overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.84rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Scope / Target</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Method</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Route Pattern</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Bucket Burst Limit</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Refill Policy</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Description</th>
              <th style={{ padding: '12px 14px', fontWeight: 600, textAlign: 'right' }}>Action</th>
            </tr>
          </thead>
          <tbody>
            {rules.length === 0 ? (
              <tr>
                <td colSpan={7} style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
                  No custom rules configured. Plan tier defaults are active.
                </td>
              </tr>
            ) : (
              rules.map(r => {
                const app = apps.find(a => a.id === r.applicationId);
                return (
                  <tr key={r.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)', transition: 'background 0.15s ease' }}>
                    <td style={{ padding: '14px' }}>
                      {app ? (
                        <span className="badge badge-tier-pro">{app.name}</span>
                      ) : (
                        <span className="badge" style={{ background: 'rgba(255,255,255,0.1)', color: '#cbd5e1' }}>GLOBAL DEFAULT</span>
                      )}
                    </td>
                    <td style={{ padding: '14px' }}>
                      <span className="badge" style={{ background: r.httpMethod === 'GET' ? 'rgba(56, 189, 248, 0.15)' : 'rgba(168, 85, 247, 0.15)', color: r.httpMethod === 'GET' ? '#38bdf8' : '#c084fc' }}>
                        {r.httpMethod}
                      </span>
                    </td>
                    <td style={{ padding: '14px', fontFamily: 'var(--font-mono)', color: '#f8fafc', fontWeight: 600 }}>
                      {r.endpointPattern}
                    </td>
                    <td style={{ padding: '14px' }}>
                      <span style={{ fontFamily: 'var(--font-mono)', color: '#34d399', fontWeight: 700 }}>
                        {r.capacity} tokens
                      </span>
                    </td>
                    <td style={{ padding: '14px' }}>
                      <span style={{ fontFamily: 'var(--font-mono)', color: '#38bdf8' }}>
                        +{r.refillTokens} tok / {r.refillPeriodSeconds}s
                      </span>
                    </td>
                    <td style={{ padding: '14px', color: 'var(--text-secondary)', maxWidth: 240, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {r.description || '—'}
                    </td>
                    <td style={{ padding: '14px', textAlign: 'right' }}>
                      <button
                        onClick={() => handleDeleteRule(r.id)}
                        className="btn btn-secondary"
                        style={{ padding: '5px 8px', color: '#fb7185' }}
                        title="Delete Rule"
                      >
                        <Trash2 size={13} />
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Add Rule Modal */}
      {showAddModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0,0,0,0.7)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: 20
        }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: 500, padding: '28px', background: 'var(--bg-surface-elevated)' }}>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fff', marginBottom: 6 }}>
              Add Rate Limiting Rule
            </h3>
            <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)', marginBottom: 20 }}>
              Specify the route pattern and Token Bucket parameters.
            </p>

            <form onSubmit={handleCreateRule}>
              <div className="form-group">
                <label className="form-label">Scope (Application or Global)</label>
                <select
                  className="form-select"
                  value={selectedAppId}
                  onChange={(e) => setSelectedAppId(e.target.value)}
                >
                  <option value="">Global (All Applications & Clients)</option>
                  {apps.map(a => (
                    <option key={a.id} value={a.id}>{a.name} ({a.planTier})</option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 12 }}>
                <div className="form-group">
                  <label className="form-label">HTTP Method</label>
                  <select
                    className="form-select"
                    value={httpMethod}
                    onChange={(e) => setHttpMethod(e.target.value)}
                  >
                    <option value="ALL">ALL Methods</option>
                    <option value="GET">GET</option>
                    <option value="POST">POST</option>
                    <option value="PUT">PUT</option>
                    <option value="DELETE">DELETE</option>
                  </select>
                </div>

                <div className="form-group">
                  <label className="form-label">Endpoint Pattern</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. /api/v1/gateway/orders"
                    className="form-input"
                    value={endpointPattern}
                    onChange={(e) => setEndpointPattern(e.target.value)}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10 }}>
                <div className="form-group">
                  <label className="form-label">Burst Capacity</label>
                  <input
                    type="number"
                    min="1"
                    required
                    className="form-input"
                    value={capacity}
                    onChange={(e) => setCapacity(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Refill Tokens</label>
                  <input
                    type="number"
                    min="1"
                    required
                    className="form-input"
                    value={refillTokens}
                    onChange={(e) => setRefillTokens(e.target.value)}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Period (Sec)</label>
                  <input
                    type="number"
                    min="1"
                    required
                    className="form-input"
                    value={refillPeriod}
                    onChange={(e) => setRefillPeriod(e.target.value)}
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Rule Description</label>
                <input
                  type="text"
                  placeholder="e.g. Checkout throttle to prevent spam"
                  className="form-input"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 24 }}>
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="btn btn-primary"
                >
                  {isSubmitting ? 'Saving...' : 'Create Rule'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
