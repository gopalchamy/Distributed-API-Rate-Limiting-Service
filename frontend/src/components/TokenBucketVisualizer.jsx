import React, { useEffect, useState } from 'react';
import { RefreshCw, Zap, Clock, ShieldCheck, AlertTriangle } from 'lucide-react';

export default function TokenBucketVisualizer({
  capacity = 20,
  remaining = 20,
  refillTokens = 20,
  refillPeriod = 60,
  lastAction = null, // 'CONSUMED' | 'REFILLED' | 'BLOCKED'
  appName = 'E-Commerce Storefront'
}) {
  const [displayRemaining, setDisplayRemaining] = useState(remaining);

  useEffect(() => {
    setDisplayRemaining(remaining);
  }, [remaining]);

  const percentage = Math.min(100, Math.max(0, Math.round((displayRemaining / capacity) * 100)));
  const refillRatePerSec = (refillTokens / refillPeriod).toFixed(1);

  // Status color scheme
  let colorHex = '#10b981'; // green
  let glowClass = 'var(--shadow-emerald-glow)';
  let statusText = 'Normal Traffic';
  if (percentage <= 20) {
    colorHex = '#f43f5e'; // red
    glowClass = 'var(--shadow-rose-glow)';
    statusText = 'Throttling Active (Bucket Empty)';
  } else if (percentage <= 55) {
    colorHex = '#f59e0b'; // amber
    glowClass = '0 0 25px rgba(245, 158, 11, 0.25)';
    statusText = 'Moderate Load';
  }

  // Generate visual token dots
  const maxDots = Math.min(24, capacity);
  const activeDots = Math.round((displayRemaining / capacity) * maxDots);

  return (
    <div className="glass-panel" style={{ padding: '24px', position: 'relative', overflow: 'hidden' }}>
      {/* Top Banner */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 20 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
            <span style={{ fontSize: '0.78rem', textTransform: 'uppercase', letterSpacing: '0.08em', color: 'var(--text-muted)', fontWeight: 600 }}>
              Live Algorithm State
            </span>
            <span className="badge" style={{ background: 'rgba(99, 102, 241, 0.12)', color: '#818cf8', border: '1px solid rgba(99, 102, 241, 0.25)' }}>
              Token Bucket (Redis Lua)
            </span>
          </div>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, color: '#fff' }}>
            {appName}
          </h3>
        </div>

        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '1.5rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: colorHex }}>
            {displayRemaining} <span style={{ fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 500 }}>/ {capacity}</span>
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', display: 'flex', alignItems: 'center', gap: 4, justifyContent: 'flex-end' }}>
            <span style={{ width: 6, height: 6, borderRadius: '50%', background: colorHex, display: 'inline-block' }} />
            {statusText}
          </div>
        </div>
      </div>

      {/* Visual Token Bucket Container */}
      <div style={{
        position: 'relative',
        height: 140,
        background: 'rgba(15, 23, 42, 0.9)',
        border: '2px solid rgba(255, 255, 255, 0.08)',
        borderTop: 'none',
        borderRadius: '0 0 20px 20px',
        overflow: 'hidden',
        boxShadow: `inset 0 -15px 30px rgba(0,0,0,0.6), ${glowClass}`,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'flex-end',
        padding: 12
      }}>
        {/* Fill level animation */}
        <div style={{
          position: 'absolute',
          bottom: 0,
          left: 0,
          right: 0,
          height: `${percentage}%`,
          background: `linear-gradient(180deg, ${colorHex}33 0%, ${colorHex}15 100%)`,
          borderTop: `2px solid ${colorHex}`,
          transition: 'height 0.3s cubic-bezier(0.4, 0, 0.2, 1), border-color 0.3s ease',
          pointerEvents: 'none'
        }}>
          {/* Animated top water ripple line */}
          <div style={{
            width: '100%',
            height: 2,
            background: colorHex,
            boxShadow: `0 0 10px ${colorHex}`
          }} />
        </div>

        {/* Floating Token Icons */}
        <div style={{
          position: 'relative',
          zIndex: 2,
          display: 'flex',
          flexWrap: 'wrap',
          gap: 6,
          justifyContent: 'center',
          alignItems: 'center',
          maxHeight: 110,
          overflow: 'hidden'
        }}>
          {Array.from({ length: maxDots }).map((_, idx) => {
            const isFilled = idx < activeDots;
            return (
              <div
                key={idx}
                style={{
                  width: 20,
                  height: 20,
                  borderRadius: '50%',
                  background: isFilled ? colorHex : 'rgba(255, 255, 255, 0.05)',
                  border: isFilled ? `1px solid #fff` : '1px dashed rgba(255, 255, 255, 0.1)',
                  boxShadow: isFilled ? `0 0 8px ${colorHex}` : 'none',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '0.65rem',
                  color: '#fff',
                  fontWeight: 700,
                  transition: 'all 0.25s ease'
                }}
              >
                {isFilled ? '⚡' : ''}
              </div>
            );
          })}
        </div>

        {/* Action flash banner */}
        {lastAction && (
          <div style={{
            position: 'absolute',
            top: 10,
            right: 12,
            zIndex: 10,
            fontSize: '0.72rem',
            fontWeight: 700,
            padding: '2px 8px',
            borderRadius: 4,
            background: lastAction === 'BLOCKED' ? 'rgba(244, 63, 94, 0.9)' : (lastAction === 'CONSUMED' ? 'rgba(99, 102, 241, 0.9)' : 'rgba(16, 185, 129, 0.9)'),
            color: '#fff',
            boxShadow: '0 2px 8px rgba(0,0,0,0.4)',
            animation: 'drop-token 0.3s ease'
          }}>
            {lastAction === 'BLOCKED' ? '🚫 429 REJECTED' : (lastAction === 'CONSUMED' ? '-1 TOKEN CONSUMED' : '+ REFILLED')}
          </div>
        )}
      </div>

      {/* Telemetry Metrics Row */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: 12,
        marginTop: 18,
        paddingTop: 16,
        borderTop: '1px solid var(--border-subtle)'
      }}>
        <div style={{ background: 'rgba(0,0,0,0.2)', padding: '10px 12px', borderRadius: 'var(--radius-md)' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 5, marginBottom: 2 }}>
            <Zap size={13} color="#818cf8" /> Burst Capacity
          </div>
          <div style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', fontFamily: 'var(--font-mono)' }}>
            {capacity} <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>reqs</span>
          </div>
        </div>

        <div style={{ background: 'rgba(0,0,0,0.2)', padding: '10px 12px', borderRadius: 'var(--radius-md)' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 5, marginBottom: 2 }}>
            <RefreshCw size={13} color="#06b6d4" /> Refill Rate
          </div>
          <div style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', fontFamily: 'var(--font-mono)' }}>
            {refillTokens} <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>/ {refillPeriod}s</span>
          </div>
        </div>

        <div style={{ background: 'rgba(0,0,0,0.2)', padding: '10px 12px', borderRadius: 'var(--radius-md)' }}>
          <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: 5, marginBottom: 2 }}>
            <Clock size={13} color="#34d399" /> Replenish Speed
          </div>
          <div style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', fontFamily: 'var(--font-mono)' }}>
            {refillRatePerSec} <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>tok/s</span>
          </div>
        </div>
      </div>
    </div>
  );
}
