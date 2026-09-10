import React, { useState, useEffect } from 'react';
import { Database, RefreshCw, Filter, CheckCircle2, XCircle, ChevronLeft, ChevronRight } from 'lucide-react';
import { api } from '../services/api';

export default function AuditLogViewer({ apps = [] }) {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [selectedAppId, setSelectedAppId] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [isLoading, setIsLoading] = useState(false);

  const fetchLogs = async (pageNumber = 0, appId = selectedAppId) => {
    setIsLoading(true);
    try {
      const data = await api.getAuditLogs(pageNumber, 20, appId ? parseInt(appId, 10) : null);
      setLogs(data.content || []);
      setTotalPages(data.totalPages || 1);
      setTotalElements(data.totalElements || 0);
      setPage(pageNumber);
    } catch (err) {
      console.error('Failed to load logs:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs(0, selectedAppId);
  }, [selectedAppId]);

  const filteredLogs = logs.filter(log => {
    if (filterStatus === 'ALLOWED') return log.allowed;
    if (filterStatus === 'BLOCKED') return !log.allowed;
    return true;
  });

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header & Filter Controls */}
      <div className="glass-panel" style={{ padding: '24px 28px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
            <span className="badge badge-tier-pro">Live Telemetry</span>
          </div>
          <h2 style={{ fontSize: '1.4rem', fontWeight: 800, color: '#fff' }}>
            Request Audit & Rate-Limit Logs
          </h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: 4 }}>
            Historical record of every incoming gateway request, throttling decision, and client IP.
          </p>
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
          {/* Filter by App */}
          <select
            className="form-select"
            style={{ width: 'auto', fontSize: '0.8125rem' }}
            value={selectedAppId}
            onChange={(e) => setSelectedAppId(e.target.value)}
          >
            <option value="">All Applications</option>
            {apps.map(a => (
              <option key={a.id} value={a.id}>{a.name}</option>
            ))}
          </select>

          {/* Filter by Status */}
          <select
            className="form-select"
            style={{ width: 'auto', fontSize: '0.8125rem' }}
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
          >
            <option value="ALL">All Statuses</option>
            <option value="ALLOWED">Allowed (200 OK)</option>
            <option value="BLOCKED">Throttled (429 Too Many)</option>
          </select>

          <button
            onClick={() => fetchLogs(page, selectedAppId)}
            className="btn btn-secondary"
            style={{ fontSize: '0.8125rem' }}
            disabled={isLoading}
          >
            <RefreshCw size={14} className={isLoading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {/* Logs Table */}
      <div className="glass-panel" style={{ padding: '20px', overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.8125rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-subtle)', color: 'var(--text-muted)' }}>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Timestamp</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Decision</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>HTTP Status</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Endpoint</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>API Key Prefix</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Client IP</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Latency</th>
              <th style={{ padding: '12px 14px', fontWeight: 600 }}>Remaining Tokens</th>
            </tr>
          </thead>
          <tbody>
            {filteredLogs.length === 0 ? (
              <tr>
                <td colSpan={8} style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
                  No matching logs recorded.
                </td>
              </tr>
            ) : (
              filteredLogs.map(l => {
                const dateStr = new Date(l.timestamp).toLocaleTimeString();
                return (
                  <tr key={l.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)', transition: 'background 0.15s ease' }}>
                    <td style={{ padding: '12px 14px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                      {dateStr}
                    </td>
                    <td style={{ padding: '12px 14px' }}>
                      {l.allowed ? (
                        <span className="badge badge-200" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                          <CheckCircle2 size={12} /> ALLOWED
                        </span>
                      ) : (
                        <span className="badge badge-429" style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                          <XCircle size={12} /> THROTTLED
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '12px 14px', fontFamily: 'var(--font-mono)' }}>
                      <span className={l.httpStatus === 200 || l.httpStatus === 201 ? 'badge badge-200' : 'badge badge-429'}>
                        {l.httpStatus}
                      </span>
                    </td>
                    <td style={{ padding: '12px 14px' }}>
                      <span className="badge" style={{ background: l.httpMethod === 'GET' ? 'rgba(56, 189, 248, 0.15)' : 'rgba(168, 85, 247, 0.15)', color: l.httpMethod === 'GET' ? '#38bdf8' : '#c084fc', marginRight: 6 }}>
                        {l.httpMethod}
                      </span>
                      <span style={{ fontFamily: 'var(--font-mono)', color: '#f8fafc' }}>{l.endpoint}</span>
                    </td>
                    <td style={{ padding: '12px 14px', fontFamily: 'var(--font-mono)', color: '#94a3b8' }}>
                      {l.apiKeyPrefix || '— (IP only)'}
                    </td>
                    <td style={{ padding: '12px 14px', fontFamily: 'var(--font-mono)', color: '#cbd5e1' }}>
                      {l.clientIp || '127.0.0.1'}
                    </td>
                    <td style={{ padding: '12px 14px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                      {l.latencyMs}ms
                    </td>
                    <td style={{ padding: '12px 14px', fontFamily: 'var(--font-mono)', color: l.remainingTokens > 0 ? '#34d399' : '#fb7185', fontWeight: 600 }}>
                      {l.remainingTokens}
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>

        {/* Pagination Footer */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 18, paddingTop: 14, borderTop: '1px solid var(--border-subtle)', fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
          <div>
            Showing page {page + 1} of {totalPages} ({totalElements} total entries)
          </div>
          <div style={{ display: 'flex', gap: 6 }}>
            <button
              onClick={() => fetchLogs(page - 1)}
              disabled={page === 0 || isLoading}
              className="btn btn-secondary"
              style={{ padding: '5px 10px', fontSize: '0.75rem' }}
            >
              <ChevronLeft size={14} /> Previous
            </button>
            <button
              onClick={() => fetchLogs(page + 1)}
              disabled={page >= totalPages - 1 || isLoading}
              className="btn btn-secondary"
              style={{ padding: '5px 10px', fontSize: '0.75rem' }}
            >
              Next <ChevronRight size={14} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
