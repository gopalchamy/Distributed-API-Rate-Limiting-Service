import React, { useState } from 'react';
import { Layers, Plus, Key, Copy, Check, Trash2, Shield, Power, Sparkles } from 'lucide-react';
import { api } from '../services/api';

export default function AppManager({
  apps = [],
  onRefresh,
  user,
  onOpenAuth
}) {
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [appName, setAppName] = useState('');
  const [appDescription, setAppDescription] = useState('');
  const [appTier, setAppTier] = useState('FREE');
  const [copiedKey, setCopiedKey] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleCopyKey = (keyValue) => {
    navigator.clipboard.writeText(keyValue);
    setCopiedKey(keyValue);
    setTimeout(() => setCopiedKey(null), 2000);
  };

  const handleCreateApp = async (e) => {
    e.preventDefault();
    if (!user) {
      onOpenAuth();
      return;
    }
    setIsSubmitting(true);
    try {
      await api.createApplication(appName, appDescription, appTier);
      setAppName('');
      setAppDescription('');
      setShowCreateModal(false);
      onRefresh();
    } catch (err) {
      alert(err.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleGenerateKey = async (appId) => {
    try {
      const keyName = prompt('Enter a label for the new API Key:', 'Secondary Key');
      if (keyName !== null) {
        await api.generateApiKey(appId, keyName);
        onRefresh();
      }
    } catch (err) {
      alert(err.message);
    }
  };

  const handleToggleKey = async (keyId) => {
    try {
      await api.toggleApiKey(keyId);
      onRefresh();
    } catch (err) {
      alert(err.message);
    }
  };

  const handleDeleteApp = async (appId) => {
    if (confirm('Are you sure you want to delete this application? All its API keys will be immediately revoked.')) {
      try {
        await api.deleteApplication(appId);
        onRefresh();
      } catch (err) {
        alert(err.message);
      }
    }
  };

  const tierColors = {
    FREE: 'badge-tier-free',
    PRO: 'badge-tier-pro',
    ENTERPRISE: 'badge-tier-enterprise'
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div className="glass-panel" style={{ padding: '24px 28px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
            <span className="badge badge-tier-pro">Application Gateway Registry</span>
          </div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff' }}>
            Registered Applications & API Keys
          </h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: 4 }}>
            Manage API clients, rate limit tiers, and secure tokens.
          </p>
        </div>

        <button
          onClick={() => user ? setShowCreateModal(true) : onOpenAuth()}
          className="btn btn-primary"
          style={{ padding: '10px 18px' }}
        >
          <Plus size={16} />
          <span>Register New Application</span>
        </button>
      </div>

      {/* Applications Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(380px, 1fr))',
        gap: 20
      }}>
        {apps.map(app => (
          <div key={app.id} className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: 16 }}>
            {/* App Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                  <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#fff' }}>{app.name}</h3>
                  <span className={`badge ${tierColors[app.planTier] || 'badge-tier-free'}`}>
                    {app.planTier} TIER
                  </span>
                </div>
                <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)' }}>
                  {app.description || 'No description provided'}
                </p>
              </div>

              <button
                onClick={() => handleDeleteApp(app.id)}
                className="btn btn-secondary"
                style={{ padding: '6px', color: '#fb7185' }}
                title="Delete Application"
              >
                <Trash2 size={15} />
              </button>
            </div>

            {/* App Quota Stats Box */}
            <div style={{
              background: 'rgba(0,0,0,0.25)',
              padding: '12px 14px',
              borderRadius: 'var(--radius-md)',
              display: 'flex',
              justifyContent: 'space-between',
              fontSize: '0.8125rem',
              border: '1px solid var(--border-subtle)'
            }}>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Burst Capacity: </span>
                <strong style={{ color: '#fff' }}>{app.rateLimitCapacity} tokens</strong>
              </div>
              <div>
                <span style={{ color: 'var(--text-muted)' }}>Refill Rate: </span>
                <strong style={{ color: '#38bdf8' }}>{app.refillTokens} / {app.refillPeriodSeconds}s</strong>
              </div>
            </div>

            {/* API Keys List */}
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                <span style={{ fontSize: '0.78rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                  API Keys ({app.apiKeys?.length || 0})
                </span>
                <button
                  onClick={() => handleGenerateKey(app.id)}
                  style={{ background: 'none', border: 'none', color: '#818cf8', fontSize: '0.78rem', fontWeight: 600, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 4 }}
                >
                  <Plus size={13} /> Add Key
                </button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {(app.apiKeys || []).map(key => {
                  const isCopied = copiedKey === key.keyValue;
                  return (
                    <div
                      key={key.id}
                      style={{
                        background: 'rgba(15, 23, 42, 0.8)',
                        padding: '10px 12px',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--border-subtle)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        gap: 10
                      }}
                    >
                      <div style={{ overflow: 'hidden' }}>
                        <div style={{ fontSize: '0.78rem', color: '#94a3b8', fontWeight: 500, marginBottom: 2 }}>
                          {key.name || 'API Key'}
                        </div>
                        <div className="key-badge">
                          <Key size={12} />
                          <span>{key.keyValue || key.keyPrefix}</span>
                        </div>
                      </div>

                      <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                        <button
                          onClick={() => handleCopyKey(key.keyValue)}
                          className="btn btn-secondary"
                          style={{ padding: '6px 8px', fontSize: '0.75rem' }}
                          title="Copy Key"
                        >
                          {isCopied ? <Check size={14} color="#10b981" /> : <Copy size={14} />}
                        </button>
                        <button
                          onClick={() => handleToggleKey(key.id)}
                          className="btn btn-secondary"
                          style={{ padding: '6px 8px', fontSize: '0.75rem', color: key.active ? '#10b981' : '#f43f5e' }}
                          title={key.active ? 'Disable Key' : 'Enable Key'}
                        >
                          <Power size={14} />
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Create App Modal */}
      {showCreateModal && (
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
          <div className="glass-panel" style={{ width: '100%', maxWidth: 480, padding: '28px', background: 'var(--bg-surface-elevated)' }}>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fff', marginBottom: 6 }}>
              Register New Application
            </h3>
            <p style={{ fontSize: '0.84rem', color: 'var(--text-secondary)', marginBottom: 20 }}>
              An API key and rate-limiting policy will be automatically provisioned.
            </p>

            <form onSubmit={handleCreateApp}>
              <div className="form-group">
                <label className="form-label">Application Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Analytics Ingestion Worker"
                  className="form-input"
                  value={appName}
                  onChange={(e) => setAppName(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Description (Optional)</label>
                <textarea
                  placeholder="Brief overview of application purpose"
                  className="form-textarea"
                  rows={2}
                  value={appDescription}
                  onChange={(e) => setAppDescription(e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Rate Limiting Plan Tier</label>
                <select
                  className="form-select"
                  value={appTier}
                  onChange={(e) => setAppTier(e.target.value)}
                >
                  <option value="FREE">Free Tier (20 req / 60 sec)</option>
                  <option value="PRO">Pro Tier (120 req / 60 sec)</option>
                  <option value="ENTERPRISE">Enterprise Tier (600 req / 60 sec)</option>
                </select>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 10, marginTop: 24 }}>
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="btn btn-primary"
                >
                  {isSubmitting ? 'Provisioning...' : 'Create Application'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
