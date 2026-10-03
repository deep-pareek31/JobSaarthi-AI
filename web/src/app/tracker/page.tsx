'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Bookmark, CheckCircle2, Clock, Building2, MapPin, ArrowRight, Trash2 } from 'lucide-react';
import { CURATED_JOBS, Job } from '@/lib/job-service';

interface TrackedApplication {
  id: string;
  jobTitle: string;
  companyName: string;
  location: string;
  appliedDate: string;
  status: 'APPLIED' | 'IN_REVIEW' | 'INTERVIEWING' | 'OFFER' | 'REJECTED';
}

export default function TrackerPage() {
  const [applications, setApplications] = useState<TrackedApplication[]>([]);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const stored = localStorage.getItem('tracked_applications');
      if (stored) {
        try {
          setApplications(JSON.parse(stored));
          return;
        } catch (_e) {}
      }

      // Initial sample seeded from curated jobs
      const initial: TrackedApplication[] = [
        {
          id: 'curated_1',
          jobTitle: 'Graduate Software Engineer - Cloud Platforms',
          companyName: 'Google',
          location: 'Bengaluru',
          appliedDate: 'Yesterday',
          status: 'INTERVIEWING'
        },
        {
          id: 'curated_3',
          jobTitle: 'Full Stack Frontend Developer',
          companyName: 'Stripe',
          location: 'Remote',
          appliedDate: '3 days ago',
          status: 'IN_REVIEW'
        }
      ];
      setApplications(initial);
      localStorage.setItem('tracked_applications', JSON.stringify(initial));
    }
  }, []);

  const updateStatus = (id: string, newStatus: TrackedApplication['status']) => {
    const updated = applications.map(app => app.id === id ? { ...app, status: newStatus } : app);
    setApplications(updated);
    localStorage.setItem('tracked_applications', JSON.stringify(updated));
  };

  const removeApplication = (id: string) => {
    const updated = applications.filter(app => app.id !== id);
    setApplications(updated);
    localStorage.setItem('tracked_applications', JSON.stringify(updated));
  };

  const stages: { key: TrackedApplication['status']; label: string; color: string }[] = [
    { key: 'APPLIED', label: 'Applied', color: 'bg-blue-50 text-blue-700 border-blue-200' },
    { key: 'IN_REVIEW', label: 'In Review', color: 'bg-amber-50 text-amber-700 border-amber-200' },
    { key: 'INTERVIEWING', label: 'Interviewing', color: 'bg-purple-50 text-purple-700 border-purple-200' },
    { key: 'OFFER', label: 'Offer Received', color: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
    { key: 'REJECTED', label: 'Archived', color: 'bg-slate-100 text-slate-600 border-slate-200' },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
            Application Pipeline Tracker
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Keep full visibility on every job application, interview stage, and employer follow-up.
          </p>
        </div>
        <Link
          href="/jobs"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-xs text-white bg-indigo-600 hover:bg-indigo-700 transition-colors shadow-sm self-start sm:self-auto"
        >
          <span>Find More Roles</span>
          <ArrowRight className="w-4 h-4" />
        </Link>
      </div>

      {applications.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <Bookmark className="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-800">No applications tracked yet</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1">
            Browse jobs and tap &quot;I Applied&quot; on any role to monitor its interview stages here.
          </p>
          <Link
            href="/jobs"
            className="mt-5 inline-block px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700"
          >
            Explore Jobs Now
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
          {stages.map(stage => {
            const stageApps = applications.filter(a => a.status === stage.key);
            return (
              <div key={stage.key} className="bg-slate-100/70 rounded-2xl p-4 border border-slate-200 flex flex-col">
                <div className="flex items-center justify-between mb-3 pb-2 border-b border-slate-200/80">
                  <span className="text-xs font-bold text-slate-800">{stage.label}</span>
                  <span className="text-xs font-bold text-slate-500 bg-white px-2 py-0.5 rounded-full border border-slate-200">
                    {stageApps.length}
                  </span>
                </div>

                <div className="space-y-3 flex-1 overflow-y-auto">
                  {stageApps.map(app => (
                    <div key={app.id} className="bg-white p-4 rounded-xl border border-slate-200 shadow-xs space-y-2">
                      <h4 className="text-sm font-bold text-slate-900 leading-snug">{app.jobTitle}</h4>
                      <p className="text-xs font-semibold text-slate-700">{app.companyName}</p>
                      <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1">
                        <span>{app.location}</span>
                        <span>{app.appliedDate}</span>
                      </div>

                      <div className="pt-2 border-t border-slate-100 flex items-center justify-between gap-1">
                        <select
                          value={app.status}
                          onChange={(e) => updateStatus(app.id, e.target.value as TrackedApplication['status'])}
                          className="text-[11px] font-semibold bg-slate-50 border border-slate-200 rounded px-1.5 py-1 text-slate-700 focus:outline-none"
                        >
                          <option value="APPLIED">Applied</option>
                          <option value="IN_REVIEW">In Review</option>
                          <option value="INTERVIEWING">Interviewing</option>
                          <option value="OFFER">Offer</option>
                          <option value="REJECTED">Archived</option>
                        </select>

                        <button
                          onClick={() => removeApplication(app.id)}
                          title="Remove application"
                          className="p-1 text-slate-300 hover:text-red-500 transition-colors"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
