import React from 'react';
import { Activity, ShieldCheck, ShieldAlert, Clock, BarChart3, Layers, ArrowUpRight, CheckCircle2, XCircle } from 'lucide-react';
import TokenBucketVisualizer from './TokenBucketVisualizer';

export default function LiveTrafficDashboard({
  summary,
  activeBucket,
  onNavigateToSimulator,
  onNavigateToApps
}) {
  if (!summary) {
    return (
      <div style={{ textAlign: 'center', padding: '60px 20px' }}>
        <div className="badge animate-pulse-glow" style={{ padding: '8px 16px', fontSize: '0.9rem' }}>
          Loading real-time gateway telemetry...
        </div>
      </div>
    );
  }

  const kpis = [
    {
      title: 'Total Gateway Traffic',
      value: summary.totalRequests?.toLocaleString() || '0',
      subtitle: '24h processed volume',
      icon: Activity,
      color: '#6366f1',
      bgGradient: 'linear-gradient(135deg, rgba(99, 102, 241, 0.15), rgba(99, 102, 241, 0.05))',
      borderColor: 'rgba(99, 102, 241, 0.3)'
    },
    {
      title: 'Allowed Requests (200 OK)',
      value: summary.allowedRequests?.toLocaleString() || '0',
      subtitle: `${100 - (summary.blockPercentage || 0)}% acceptance rate`,
      icon: CheckCircle2,
      color: '#10b981',
      bgGradient: 'linear-gradient(135deg, rgba(16, 185, 129, 0.15), rgba(16, 185, 129, 0.05))',
      borderColor: 'rgba(16, 185, 129, 0.3)'
    },
    {
      title: 'Throttled (429 Too Many)',
      value: summary.blockedRequests?.toLocaleString() || '0',
      subtitle: `${summary.blockPercentage || 0}% traffic throttled`,
      icon: XCircle,
      color: '#f43f5e',
      bgGradient: 'linear-gradient(135deg, rgba(244, 63, 94, 0.15), rgba(244, 63, 94, 0.05))',
      borderColor: 'rgba(244, 63, 94, 0.35)'
    },
    {
      title: 'Avg Gateway Latency',
      value: `${summary.averageLatencyMs || 0} ms`,
      subtitle: 'Redis check overhead: < 1ms',
      icon: Clock,
      color: '#06b6d4',
      bgGradient: 'linear-gradient(135deg, rgba(6, 182, 212, 0.15), rgba(6, 182, 212, 0.05))',
      borderColor: 'rgba(6, 182, 212, 0.3)'
    }
  ];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Top Banner with Quick Actions */}
      <div className="glass-panel glass-panel-glow" style={{
        padding: '24px 28px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: 16
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
            <span className="badge badge-200">System Healthy</span>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Engine Mode: <strong style={{ color: '#38bdf8' }}>{summary.rateLimiterEngine}</strong>
            </span>
          </div>
          <h2 style={{ fontSize: '1.5rem', fontWeight: 800, color: '#fff', letterSpacing: '-0.02em' }}>
            Distributed Traffic & Rate Limiting Overview
          </h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: 4 }}>
            Guarding downstream microservices against traffic spikes and abusive queries using Redis Token Buckets.
          </p>
        </div>

        <div style={{ display: 'flex', gap: 10 }}>
          <button onClick={onNavigateToSimulator} className="btn btn-primary">
            <span>Launch Burst Simulator</span>
            <ArrowUpRight size={15} />
          </button>
          <button onClick={onNavigateToApps} className="btn btn-secondary">
            <Layers size={15} />
            <span>Manage Apps & Keys</span>
          </button>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
        gap: 16
      }}>
        {kpis.map((kpi, idx) => {
          const Icon = kpi.icon;
          return (
            <div
              key={idx}
              className="glass-panel"
              style={{
                padding: '20px',
                background: kpi.bgGradient,
                borderColor: kpi.borderColor,
                position: 'relative',
                overflow: 'hidden'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                <span style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', fontWeight: 500 }}>
                  {kpi.title}
                </span>
                <div style={{
                  width: 32,
                  height: 32,
                  borderRadius: 8,
                  background: `${kpi.color}22`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  <Icon size={18} color={kpi.color} />
                </div>
              </div>

              <div style={{ fontSize: '1.875rem', fontWeight: 800, color: '#fff', fontFamily: 'var(--font-mono)', letterSpacing: '-0.03em' }}>
                {kpi.value}
              </div>

              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 6 }}>
                {kpi.subtitle}
              </div>
            </div>
          );
        })}
      </div>

      {/* Main Row: Token Bucket Visualizer & Traffic Distribution */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'minmax(340px, 1fr) 1.5fr',
        gap: 20
      }}>
        {/* Token Bucket Visualizer */}
        <TokenBucketVisualizer
          capacity={activeBucket?.capacity || 20}
          remaining={activeBucket?.remaining !== undefined ? activeBucket.remaining : 20}
          refillTokens={activeBucket?.refillTokens || 20}
          refillPeriod={activeBucket?.refillPeriod || 60}
          lastAction={activeBucket?.lastAction}
          appName={activeBucket?.appName || 'E-Commerce Storefront'}
        />

        {/* Traffic Timeline Chart & Breakdown */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }}>
            <div>
              <h3 style={{ fontSize: '1.125rem', fontWeight: 700, color: '#fff' }}>
                Recent Traffic Telemetry (Allowed vs Throttled)
              </h3>
              <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                Real-time request distribution in 5-minute sampling buckets
              </p>
            </div>
            <div style={{ display: 'flex', gap: 14, fontSize: '0.75rem' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: 5, color: '#34d399' }}>
                <span style={{ width: 8, height: 8, borderRadius: 2, background: '#10b981', display: 'inline-block' }} /> 200 Allowed
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: 5, color: '#fb7185' }}>
                <span style={{ width: 8, height: 8, borderRadius: 2, background: '#f43f5e', display: 'inline-block' }} /> 429 Throttled
              </span>
            </div>
          </div>

          {/* Bar Chart Visualization */}
          <div style={{
            flex: 1,
            minHeight: 160,
            display: 'flex',
            alignItems: 'flex-end',
            gap: 16,
            paddingTop: 10,
            paddingBottom: 8,
            borderBottom: '1px solid var(--border-subtle)'
          }}>
            {summary.timeline && summary.timeline.length > 0 ? (
              summary.timeline.map((point, idx) => {
                const maxVal = Math.max(1, ...summary.timeline.map(p => p.allowed + p.blocked));
                const total = point.allowed + point.blocked;
                const heightPct = total > 0 ? Math.max(12, Math.round((total / maxVal) * 100)) : 8;
                const allowedPct = total > 0 ? (point.allowed / total) * 100 : 100;
                const blockedPct = total > 0 ? (point.blocked / total) * 100 : 0;

                return (
                  <div key={idx} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', height: '100%', justifyContent: 'flex-end' }}>
                    <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)', marginBottom: 4 }}>
                      {total}
                    </div>
                    <div style={{
                      width: '100%',
                      maxWidth: 36,
                      height: `${heightPct}%`,
                      borderRadius: '4px 4px 0 0',
                      overflow: 'hidden',
                      display: 'flex',
                      flexDirection: 'column-reverse',
                      background: 'rgba(255, 255, 255, 0.04)'
                    }}>
                      <div style={{ height: `${allowedPct}%`, background: '#10b981', transition: 'height 0.3s ease' }} title={`Allowed: ${point.allowed}`} />
                      <div style={{ height: `${blockedPct}%`, background: '#f43f5e', transition: 'height 0.3s ease' }} title={`Throttled: ${point.blocked}`} />
                    </div>
                    <div style={{ fontSize: '0.68rem', color: 'var(--text-muted)', marginTop: 8, fontFamily: 'var(--font-mono)' }}>
                      {point.timestamp}
                    </div>
                  </div>
                );
              })
            ) : (
              <div style={{ margin: 'auto', color: 'var(--text-muted)', fontSize: '0.84rem' }}>
                No traffic recorded yet. Fire requests from the Simulator tab!
              </div>
            )}
          </div>

          {/* Top Endpoints Table */}
          <div style={{ marginTop: 18 }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Top Protected Downstream Routes
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              {summary.topEndpoints && summary.topEndpoints.length > 0 ? (
                summary.topEndpoints.map((ep, idx) => (
                  <div key={idx} style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '6px 10px',
                    background: 'rgba(0, 0, 0, 0.2)',
                    borderRadius: 'var(--radius-sm)',
                    fontSize: '0.8125rem'
                  }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                      <span className="badge" style={{ background: ep.method === 'GET' ? 'rgba(56, 189, 248, 0.15)' : 'rgba(168, 85, 247, 0.15)', color: ep.method === 'GET' ? '#38bdf8' : '#c084fc' }}>
                        {ep.method}
                      </span>
                      <span style={{ fontFamily: 'var(--font-mono)', color: '#f1f5f9' }}>{ep.endpoint}</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                      <span style={{ color: 'var(--text-secondary)' }}>{ep.count} reqs</span>
                      {ep.blocked > 0 && (
                        <span className="badge badge-429">{ep.blocked} blocked</span>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem' }}>No endpoints accessed yet.</div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
