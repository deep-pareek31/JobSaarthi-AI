'use client';

import { useState, useEffect } from 'react';
import { 
  Search, 
  MapPin, 
  Building2, 
  ExternalLink, 
  Bookmark, 
  BookmarkCheck, 
  CheckCircle2, 
  Filter, 
  Sparkles,
  TrendingUp,
  Globe
} from 'lucide-react';
import { CAREER_PORTALS, CURATED_JOBS, fetchLiveWebJobs, Job } from '@/lib/job-service';

export default function JobsPage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('ALL');
  const [jobs, setJobs] = useState<Job[]>(CURATED_JOBS);
  const [savedJobs, setSavedJobs] = useState<string[]>([]);
  const [appliedJobs, setAppliedJobs] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Read saved / applied jobs from local storage
    if (typeof window !== 'undefined') {
      const saved = JSON.parse(localStorage.getItem('saved_jobs') || '[]');
      const applied = JSON.parse(localStorage.getItem('applied_jobs') || '[]');
      setSavedJobs(saved);
      setAppliedJobs(applied);

      // Check URL search params
      const params = new URLSearchParams(window.location.search);
      const q = params.get('q');
      if (q) {
        setSearchQuery(q);
        handleLiveSearch(q);
      } else {
        handleLiveSearch('');
      }
    }
  }, []);

  const handleLiveSearch = async (query: string) => {
    setIsLoading(true);
    try {
      const results = await fetchLiveWebJobs(query, 30);
      setJobs(results);
    } catch (_e) {
      setJobs(CURATED_JOBS);
    } finally {
      setIsLoading(false);
    }
  };

  const toggleSaveJob = (id: string) => {
    let updated: string[];
    if (savedJobs.includes(id)) {
      updated = savedJobs.filter(item => item !== id);
    } else {
      updated = [...savedJobs, id];
    }
    setSavedJobs(updated);
    localStorage.setItem('saved_jobs', JSON.stringify(updated));
  };

  const toggleMarkApplied = (id: string) => {
    let updated: string[];
    if (appliedJobs.includes(id)) {
      updated = appliedJobs.filter(item => item !== id);
    } else {
      updated = [...appliedJobs, id];
    }
    setAppliedJobs(updated);
    localStorage.setItem('applied_jobs', JSON.stringify(updated));
  };

  const filteredJobs = jobs.filter(job => {
    if (filterType === 'ALL') return true;
    if (filterType === 'REMOTE') return job.remote_type === 'REMOTE';
    if (filterType === 'FULL_TIME') return job.employment_type === 'FULL_TIME';
    if (filterType === 'INTERNSHIP') return job.employment_type.includes('INTERN');
    return true;
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-8">
      {/* Top Header */}
      <div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
          Explore Live Career Opportunities
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Direct employer openings verified across official ATS feeds and corporate job boards.
        </p>
      </div>

      {/* Official Portals Strip */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200 shadow-sm">
        <div className="flex items-center justify-between mb-3 text-xs font-bold text-slate-700">
          <span className="flex items-center gap-1.5 text-indigo-600">
            <Globe className="w-4 h-4" />
            1-Tap Company Career Portals
          </span>
          <span className="text-slate-400 font-normal">Pre-filled with &quot;{searchQuery || 'Software Engineer'}&quot;</span>
        </div>
        <div className="flex gap-2.5 overflow-x-auto pb-1 scrollbar-none">
          {CAREER_PORTALS.map(portal => (
            <a
              key={portal.id}
              href={portal.urlBuilder(searchQuery)}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-slate-200 hover:border-indigo-400 bg-slate-50 text-xs font-semibold text-slate-800 hover:text-indigo-600 transition-colors whitespace-nowrap shadow-xs"
            >
              <span>{portal.name}</span>
              <ExternalLink className="w-3 h-3 text-slate-400" />
            </a>
          ))}
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="w-5 h-5 absolute left-3.5 top-3.5 text-slate-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') handleLiveSearch(searchQuery);
            }}
            placeholder="Search title, tech stack, or company (e.g., Google, React, Python)..."
            className="w-full pl-11 pr-4 py-3 rounded-xl border border-slate-200 bg-white text-slate-900 placeholder-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-600"
          />
        </div>
        <button
          onClick={() => handleLiveSearch(searchQuery)}
          className="px-6 py-3 rounded-xl font-bold text-sm text-white bg-indigo-600 hover:bg-indigo-700 transition-all shadow-sm shrink-0 flex items-center justify-center gap-2"
        >
          <Sparkles className="w-4 h-4" />
          <span>Live Search</span>
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {[
          { key: 'ALL', label: 'All Openings' },
          { key: 'FULL_TIME', label: 'Full Time' },
          { key: 'INTERNSHIP', label: 'Internships' },
          { key: 'REMOTE', label: '100% Remote' },
        ].map(tab => (
          <button
            key={tab.key}
            onClick={() => setFilterType(tab.key)}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              filterType === tab.key
                ? 'bg-indigo-600 text-white shadow-sm'
                : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Jobs Listing */}
      {isLoading ? (
        <div className="text-center py-20">
          <div className="inline-block animate-spin w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full mb-3" />
          <p className="text-sm text-slate-500 font-medium">Scanning live feeds & open roles...</p>
        </div>
      ) : filteredJobs.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-2xl border border-slate-200">
          <p className="text-base font-semibold text-slate-700">No jobs matched your search criteria.</p>
          <p className="text-xs text-slate-500 mt-1">Try clearing filters or search for another technology.</p>
          <button
            onClick={() => { setSearchQuery(''); handleLiveSearch(''); }}
            className="mt-4 px-4 py-2 rounded-xl text-xs font-bold text-indigo-600 bg-indigo-50 border border-indigo-200"
          >
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredJobs.map(job => {
            const isSaved = savedJobs.includes(job.id);
            const isApplied = appliedJobs.includes(job.id);

            return (
              <div
                key={job.id}
                className="bg-white rounded-2xl p-6 border border-slate-200 shadow-xs hover:shadow-md hover:border-indigo-300 transition-all flex flex-col md:flex-row md:items-center justify-between gap-6"
              >
                <div className="space-y-3 flex-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-[11px] font-bold text-indigo-600 bg-indigo-50 px-2.5 py-0.5 rounded">
                      {job.employment_type.replace('_', ' ')}
                    </span>
                    <span className="text-[11px] font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded">
                      {job.remote_type}
                    </span>
                    {job.match_percentage && (
                      <span className="text-[11px] font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-0.5 rounded">
                        {job.match_percentage}% AI Profile Match
                      </span>
                    )}
                  </div>

                  <div>
                    <h3 className="text-xl font-bold text-slate-900 hover:text-indigo-600 transition-colors">
                      {job.title}
                    </h3>
                    <p className="text-sm font-semibold text-slate-700 mt-0.5">
                      {job.company.name} <span className="text-slate-400 font-normal">• {job.department}</span>
                    </p>
                  </div>

                  <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500">
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-slate-400" />
                      {job.location}
                    </span>
                    <span className="flex items-center gap-1">
                      <TrendingUp className="w-3.5 h-3.5 text-slate-400" />
                      {job.experience_min} - {job.experience_max || 'Open'} yrs
                    </span>
                    <span>Source: {job.source}</span>
                  </div>

                  <div className="flex flex-wrap gap-1.5">
                    {job.skills.map(s => (
                      <span key={s} className="text-[11px] font-medium bg-slate-100 text-slate-600 px-2 py-0.5 rounded">
                        {s}
                      </span>
                    ))}
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="flex md:flex-col items-center md:items-end justify-between gap-3 shrink-0 pt-4 md:pt-0 border-t md:border-t-0 border-slate-100">
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => toggleSaveJob(job.id)}
                      title={isSaved ? 'Unsave job' : 'Save job'}
                      className={`p-2 rounded-xl border transition-colors ${
                        isSaved 
                          ? 'border-indigo-600 text-indigo-600 bg-indigo-50' 
                          : 'border-slate-200 text-slate-400 hover:text-slate-600 hover:bg-slate-50'
                      }`}
                    >
                      {isSaved ? <BookmarkCheck className="w-5 h-5" /> : <Bookmark className="w-5 h-5" />}
                    </button>

                    <button
                      onClick={() => toggleMarkApplied(job.id)}
                      title={isApplied ? 'Applied' : 'Mark as applied'}
                      className={`px-3 py-2 rounded-xl border text-xs font-semibold transition-colors flex items-center gap-1.5 ${
                        isApplied 
                          ? 'border-emerald-500 text-emerald-600 bg-emerald-50' 
                          : 'border-slate-200 text-slate-600 hover:bg-slate-50'
                      }`}
                    >
                      <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                      <span>{isApplied ? 'Applied' : 'I Applied'}</span>
                    </button>
                  </div>

                  <a
                    href={job.application_url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1.5 px-5 py-2.5 rounded-xl font-bold text-xs text-white bg-indigo-600 hover:bg-indigo-700 shadow-sm transition-all"
                  >
                    <span>Apply on Site</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
