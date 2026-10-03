'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  Bell, 
  Plus, 
  CheckCircle2, 
  Trash2, 
  Clock, 
  Mail, 
  RefreshCw, 
  ShieldCheck, 
  Sparkles,
  ArrowRight
} from 'lucide-react';
import { CURATED_JOBS, fetchLiveWebJobs, Job } from '@/lib/job-service';

interface WebJobAlert {
  id: string;
  query: string;
  location: string;
  frequency: 'INSTANT' | 'DAILY' | 'WEEKLY';
  email: string;
  isActive: boolean;
  createdAt: string;
  lastMatchCount: number;
}

interface CronNotificationReceipt {
  alertId: string;
  query: string;
  recipientEmail: string;
  matches: string[];
  dispatchedAt: string;
}

export default function JobAlertsPage() {
  const [alerts, setAlerts] = useState<WebJobAlert[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [cronLogs, setCronLogs] = useState<CronNotificationReceipt[]>([]);
  const [isSimulatingCron, setIsSimulatingCron] = useState(false);
  const [availableJobs, setAvailableJobs] = useState<Job[]>(CURATED_JOBS);

  // Form states
  const [newQuery, setNewQuery] = useState('');
  const [newLocation, setNewLocation] = useState('Any Location');
  const [newFrequency, setNewFrequency] = useState<'INSTANT' | 'DAILY' | 'WEEKLY'>('DAILY');
  const [newEmail, setNewEmail] = useState('deep.pareek31@gmail.com');

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const storedAlerts = localStorage.getItem('jobsaarthi_alerts');
      if (storedAlerts) {
        try {
          setAlerts(JSON.parse(storedAlerts));
        } catch (_e) {}
      } else {
        const initial: WebJobAlert[] = [
          {
            id: 'alert_1',
            query: 'Software Engineer',
            location: 'Bengaluru',
            frequency: 'DAILY',
            email: 'deep.pareek31@gmail.com',
            isActive: true,
            createdAt: '3 days ago',
            lastMatchCount: 4
          },
          {
            id: 'alert_2',
            query: 'Frontend React',
            location: 'Remote',
            frequency: 'INSTANT',
            email: 'deep.pareek31@gmail.com',
            isActive: true,
            createdAt: '1 day ago',
            lastMatchCount: 2
          }
        ];
        setAlerts(initial);
        localStorage.setItem('jobsaarthi_alerts', JSON.stringify(initial));
      }

      fetchLiveWebJobs('', 25).then(jobs => {
        if (jobs && jobs.length > 0) setAvailableJobs(jobs);
      });
    }
  }, []);

  const saveAlertsToStorage = (updated: WebJobAlert[]) => {
    setAlerts(updated);
    if (typeof window !== 'undefined') {
      localStorage.setItem('jobsaarthi_alerts', JSON.stringify(updated));
    }
  };

  const handleCreateAlert = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newQuery.trim()) return;

    const alertItem: WebJobAlert = {
      id: `alert_${Date.now()}`,
      query: newQuery.trim(),
      location: newLocation.trim() || 'Any Location',
      frequency: newFrequency,
      email: newEmail.trim() || 'deep.pareek31@gmail.com',
      isActive: true,
      createdAt: 'Just now',
      lastMatchCount: 0
    };

    const updated = [alertItem, ...alerts];
    saveAlertsToStorage(updated);
    setShowCreateModal(false);
    setNewQuery('');
  };

  const toggleAlert = (id: string) => {
    const updated = alerts.map(a => a.id === id ? { ...a, isActive: !a.isActive } : a);
    saveAlertsToStorage(updated);
  };

  const deleteAlert = (id: string) => {
    const updated = alerts.filter(a => a.id !== id);
    saveAlertsToStorage(updated);
  };

  // Simulates future backend cron worker execution
  const runCronJobSimulation = () => {
    setIsSimulatingCron(true);
    setTimeout(() => {
      const dispatchedList: CronNotificationReceipt[] = [];
      const updatedAlerts = alerts.map(alert => {
        if (!alert.isActive) return alert;

        const words = alert.query.toLowerCase().split(' ').filter(w => w.length > 2);
        const matches = availableJobs.filter(j => {
          const matchTitle = words.length === 0 || words.some(w => j.title.toLowerCase().includes(w) || j.company.name.toLowerCase().includes(w));
          const matchLoc = alert.location === 'Any Location' || j.location.toLowerCase().includes(alert.location.toLowerCase());
          return matchTitle && matchLoc;
        });

        if (matches.length > 0) {
          dispatchedList.push({
            alertId: alert.id,
            query: alert.query,
            recipientEmail: alert.email,
            matches: matches.slice(0, 4).map(m => `${m.title} at ${m.company.name}`),
            dispatchedAt: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          });
        }

        return { ...alert, lastMatchCount: matches.length };
      });

      saveAlertsToStorage(updatedAlerts);
      setCronLogs(dispatchedList);
      setIsSimulatingCron(false);
    }, 600);
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-10">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-200 pb-6">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-sky-50 border border-sky-200 text-sky-800 text-xs font-bold uppercase tracking-wider mb-2">
            <Bell className="w-3.5 h-3.5 text-sky-600" />
            Automated Career Dispatch Engine
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
            Job Alerts & Email Delivery
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Save queries and receive scheduled notification digests. Designed for backend cron-job systems.
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-xs text-white bg-indigo-600 hover:bg-indigo-700 transition-all shadow-sm self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Create New Alert</span>
        </button>
      </div>

      {/* Cron Runner Simulation Banner */}
      <div className="p-6 rounded-2xl bg-[#0B192C] text-white border border-slate-800 shadow-md flex flex-col md:flex-row items-center justify-between gap-6">
        <div className="space-y-1 max-w-xl">
          <div className="flex items-center gap-2 text-sky-400 text-xs font-bold uppercase tracking-wider">
            <Clock className="w-4 h-4" />
            Backend Cron-Job Compatible Architecture
          </div>
          <h3 className="text-lg font-bold">Simulate Celery / Cron Scheduled Trigger</h3>
          <p className="text-xs text-slate-400 leading-relaxed">
            Evaluates your active saved search alerts against live indexing feeds and generates email delivery receipts.
          </p>
        </div>

        <button
          onClick={runCronJobSimulation}
          disabled={isSimulatingCron}
          className="px-6 py-3 rounded-xl bg-sky-400 hover:bg-sky-500 text-slate-950 font-bold text-xs uppercase tracking-wider transition-all flex items-center gap-2 shrink-0 shadow-md"
        >
          <RefreshCw className={`w-4 h-4 ${isSimulatingCron ? 'animate-spin' : ''}`} />
          <span>{isSimulatingCron ? 'Scanning Roles...' : 'Simulate Cron Dispatch'}</span>
        </button>
      </div>

      {/* Cron Dispatched Logs Preview */}
      {cronLogs.length > 0 && (
        <div className="p-5 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-950 space-y-3">
          <div className="flex items-center gap-2 text-xs font-bold text-emerald-800 uppercase tracking-wider">
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            Cron Execution Complete: {cronLogs.length} Email Alerts Dispatched
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 pt-1">
            {cronLogs.map((log, i) => (
              <div key={i} className="p-3 bg-white rounded-xl border border-emerald-200 text-xs space-y-1">
                <div className="flex items-center justify-between font-bold text-slate-900">
                  <span>To: {log.recipientEmail}</span>
                  <span className="text-[11px] text-slate-400">{log.dispatchedAt}</span>
                </div>
                <p className="text-slate-600 text-[11px]">Query: <strong>&ldquo;{log.query}&rdquo;</strong></p>
                <div className="text-[11px] text-slate-500 pt-1 border-t border-slate-100">
                  {log.matches.join(' • ')}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Saved Alerts List */}
      <div className="space-y-4">
        <h3 className="text-base font-bold text-slate-900">Active Saved Queries ({alerts.length})</h3>

        {alerts.length === 0 ? (
          <div className="p-12 text-center bg-white rounded-2xl border border-slate-200">
            <Bell className="w-10 h-10 text-slate-300 mx-auto mb-2" />
            <h4 className="font-bold text-slate-800">No Job Alerts Created</h4>
            <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
              Save your preferred roles to receive automated notifications when new jobs are indexed.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {alerts.map((alert) => (
              <div
                key={alert.id}
                className="p-5 rounded-2xl bg-white border border-slate-200 shadow-xs hover:border-slate-300 transition-all flex flex-col justify-between space-y-4"
              >
                <div>
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-[10px] font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded">
                        {alert.frequency}
                      </span>
                      <h4 className="text-base font-bold text-slate-900 mt-1.5">{alert.query}</h4>
                      <p className="text-xs text-slate-500 mt-0.5">Location: {alert.location}</p>
                    </div>

                    <button
                      onClick={() => toggleAlert(alert.id)}
                      className={`text-xs px-2.5 py-1 rounded-full font-bold transition-colors ${
                        alert.isActive
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-slate-100 text-slate-500'
                      }`}
                    >
                      {alert.isActive ? 'Active' : 'Paused'}
                    </button>
                  </div>

                  <div className="mt-4 flex items-center gap-2 text-xs text-slate-500">
                    <Mail className="w-3.5 h-3.5 text-slate-400" />
                    <span>{alert.email}</span>
                  </div>
                </div>

                <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
                  <span className="text-slate-400">{alert.lastMatchCount} roles matched</span>
                  <button
                    onClick={() => deleteAlert(alert.id)}
                    className="p-1.5 text-slate-400 hover:text-red-500 transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Create Alert Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <form onSubmit={handleCreateAlert} className="bg-white rounded-2xl max-w-md w-full p-6 text-slate-900 shadow-2xl space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-bold flex items-center gap-2">
                <Bell className="w-4 h-4 text-indigo-600" />
                Create Job Alert
              </h3>
              <button type="button" onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold mb-1 text-slate-700">Role Query / Keyword</label>
                <input
                  type="text"
                  required
                  value={newQuery}
                  onChange={e => setNewQuery(e.target.value)}
                  placeholder="e.g. Software Engineer, React, Python"
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-600 text-xs"
                />
              </div>

              <div>
                <label className="block font-semibold mb-1 text-slate-700">Location</label>
                <input
                  type="text"
                  value={newLocation}
                  onChange={e => setNewLocation(e.target.value)}
                  placeholder="e.g. Bengaluru, Remote, Any Location"
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-600 text-xs"
                />
              </div>

              <div>
                <label className="block font-semibold mb-1 text-slate-700">Frequency</label>
                <div className="grid grid-cols-3 gap-2">
                  {(['INSTANT', 'DAILY', 'WEEKLY'] as const).map(freq => (
                    <button
                      key={freq}
                      type="button"
                      onClick={() => setNewFrequency(freq)}
                      className={`py-2 rounded-xl border font-bold text-center transition-all ${
                        newFrequency === freq 
                          ? 'border-indigo-600 bg-indigo-50 text-indigo-700' 
                          : 'border-slate-200 text-slate-600 hover:bg-slate-50'
                      }`}
                    >
                      {freq}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block font-semibold mb-1 text-slate-700">Recipient Email</label>
                <input
                  type="email"
                  required
                  value={newEmail}
                  onChange={e => setNewEmail(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-600 text-xs"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-5 py-2 rounded-xl text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 shadow-sm"
              >
                Save Alert
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
