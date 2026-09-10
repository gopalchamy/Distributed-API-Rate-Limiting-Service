import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import LiveTrafficDashboard from './components/LiveTrafficDashboard';
import RateLimitSimulator from './components/RateLimitSimulator';
import AppManager from './components/AppManager';
import RuleConfigurator from './components/RuleConfigurator';
import AuditLogViewer from './components/AuditLogViewer';
import AuthModal from './components/AuthModal';
import { api } from './services/api';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [user, setUser] = useState(api.getCurrentUser());
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [summary, setSummary] = useState(null);
  const [apps, setApps] = useState([]);
  const [rules, setRules] = useState([]);
  const [activeBucket, setActiveBucket] = useState({
    capacity: 20,
    remaining: 20,
    refillTokens: 20,
    refillPeriod: 60,
    appName: 'E-Commerce Storefront (Free Tier)',
    lastAction: null
  });

  const loadData = async () => {
    try {
      const summaryData = await api.getAnalyticsSummary().catch(() => null);
      if (summaryData) setSummary(summaryData);

      const rulesData = await api.getRules().catch(() => []);
      setRules(rulesData);

      if (user) {
        const appsData = await api.getApplications().catch(() => []);
        setApps(appsData);
      }
    } catch (err) {
      console.error('Error loading initial data:', err);
    }
  };

  useEffect(() => {
    loadData();
    // Periodic telemetry refresh
    const interval = setInterval(loadData, 5000);
    return () => clearInterval(interval);
  }, [user]);

  // When a test request is executed in the simulator, update live token bucket visualizer
  const handleTrafficEvent = (response) => {
    if (response && response.rateLimit) {
      setActiveBucket(prev => ({
        ...prev,
        capacity: response.rateLimit.limit || prev.capacity,
        remaining: response.rateLimit.remaining !== null ? response.rateLimit.remaining : prev.remaining,
        lastAction: response.status === 429 ? 'BLOCKED' : 'CONSUMED'
      }));
    }
    // Refresh analytics summary after request
    api.getAnalyticsSummary().then(s => setSummary(s)).catch(() => {});
  };

  const handleLogout = () => {
    api.logout();
    setUser(null);
    setApps([]);
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        user={user}
        onOpenAuth={() => setIsAuthOpen(true)}
        onLogout={handleLogout}
        engine={summary?.rateLimiterEngine}
      />

      <main className="app-container" style={{ flex: 1, paddingTop: 32 }}>
        {activeTab === 'dashboard' && (
          <LiveTrafficDashboard
            summary={summary}
            activeBucket={activeBucket}
            onNavigateToSimulator={() => setActiveTab('simulator')}
            onNavigateToApps={() => setActiveTab('apps')}
          />
        )}

        {activeTab === 'simulator' && (
          <RateLimitSimulator
            apps={apps}
            onTrafficEvent={handleTrafficEvent}
          />
        )}

        {activeTab === 'apps' && (
          <AppManager
            apps={apps}
            onRefresh={loadData}
            user={user}
            onOpenAuth={() => setIsAuthOpen(true)}
          />
        )}

        {activeTab === 'rules' && (
          <RuleConfigurator
            rules={rules}
            apps={apps}
            onRefresh={loadData}
            user={user}
            onOpenAuth={() => setIsAuthOpen(true)}
          />
        )}

        {activeTab === 'logs' && (
          <AuditLogViewer apps={apps} />
        )}
      </main>

      {/* Footer */}
      <footer style={{
        borderTop: '1px solid var(--border-subtle)',
        padding: '20px',
        textAlign: 'center',
        fontSize: '0.78rem',
        color: 'var(--text-muted)',
        background: 'rgba(9, 13, 22, 0.9)'
      }}>
        NexusLimit Distributed Rate Limiting & Traffic Throttling Service &bull; Token Bucket Algorithm &bull; Spring Boot &amp; Redis
      </footer>

      {/* Auth Modal */}
      <AuthModal
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        onAuthSuccess={(authData) => {
          setUser(authData);
          loadData();
        }}
      />
    </div>
  );
}
