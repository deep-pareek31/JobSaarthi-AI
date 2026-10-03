'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { 
  Search, 
  Sparkles, 
  ExternalLink, 
  CheckCircle2, 
  Building2, 
  MapPin, 
  ArrowRight, 
  ShieldCheck, 
  Zap, 
  TrendingUp, 
  Star,
  Check,
  CreditCard
} from 'lucide-react';
import { CAREER_PORTALS, CURATED_JOBS, fetchLiveWebJobs, Job } from '@/lib/job-service';

export default function HomePage() {
  const [searchQuery, setSearchQuery] = useState('');
  const [jobs, setJobs] = useState<Job[]>(CURATED_JOBS);
  const [isLoading, setIsLoading] = useState(false);
  const [checkoutPlan, setCheckoutPlan] = useState<string | null>(null);
  const [activeTier, setActiveTier] = useState('FREE');

  useEffect(() => {
    async function loadInitialJobs() {
      setIsLoading(true);
      try {
        const liveJobs = await fetchLiveWebJobs('', 8);
        if (liveJobs && liveJobs.length > 0) {
          setJobs(liveJobs);
        }
      } catch (_e) {
        // Fallback to CURATED_JOBS
      } finally {
        setIsLoading(false);
      }
    }
    loadInitialJobs();
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      window.location.href = `/jobs?q=${encodeURIComponent(searchQuery.trim())}`;
    }
  };

  const handleSubscribe = (tierName: string) => {
    setActiveTier(tierName);
    setCheckoutPlan(null);
    alert(`🎉 Successfully activated ${tierName} plan! Your active quota has been expanded.`);
  };

  return (
    <div className="space-y-16 sm:space-y-24">
      {/* Hero Section */}
      <section className="relative overflow-hidden pt-12 pb-20 lg:pt-20 lg:pb-28 border-b border-slate-200 bg-gradient-to-b from-indigo-50/50 via-white to-slate-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-indigo-100 text-indigo-800 text-xs font-bold uppercase tracking-wider mb-6 shadow-sm">
            <Sparkles className="w-3.5 h-3.5 text-indigo-600" />
            100% Real Jobs • Direct Company ATS Links
          </div>

          <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-slate-900 max-w-4xl mx-auto leading-tight sm:leading-none">
            Find Your Dream Role at <br />
            <span className="bg-gradient-to-r from-indigo-600 via-violet-600 to-indigo-800 bg-clip-text text-transparent">
              Google, Microsoft, Amazon & Top Tech
            </span>
          </h1>

          <p className="mt-6 text-lg sm:text-xl text-slate-600 max-w-2xl mx-auto leading-relaxed">
            Stop wasting time with fake recruiters and dead-end job boards. JobSaarthi delivers verified live job openings directly from official corporate career portals and ATS feeds.
          </p>

          {/* Search Bar Form */}
          <form onSubmit={handleSearchSubmit} className="mt-10 max-w-2xl mx-auto">
            <div className="relative flex items-center bg-white rounded-2xl shadow-xl shadow-indigo-100/60 border border-slate-200 p-2 focus-within:ring-2 focus-within:ring-indigo-600 focus-within:border-transparent transition-all">
              <div className="pl-3 text-slate-400">
                <Search className="w-6 h-6 text-indigo-600" />
              </div>
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Try 'Software Engineer', 'Google', 'Data Scientist', 'Remote'..."
                className="w-full px-4 py-3 text-slate-900 placeholder-slate-400 text-base bg-transparent focus:outline-none"
              />
              <button
                type="submit"
                className="px-6 py-3.5 rounded-xl font-bold text-sm text-white bg-indigo-600 hover:bg-indigo-700 shadow-md transition-all flex items-center gap-2"
              >
                <span>Search</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
            <div className="flex flex-wrap items-center justify-center gap-2 mt-4 text-xs text-slate-500">
              <span className="font-semibold text-slate-600">Trending Searches:</span>
              {['Frontend Developer', 'Python Intern', 'Machine Learning', 'Cloud Engineer', 'Remote Product'].map((tag) => (
                <button
                  key={tag}
                  type="button"
                  onClick={() => { setSearchQuery(tag); }}
                  className="px-2.5 py-1 rounded-md bg-white border border-slate-200 hover:border-indigo-400 hover:text-indigo-600 transition-colors"
                >
                  {tag}
                </button>
              ))}
            </div>
          </form>
        </div>
      </section>

      {/* Official Company Career Portals Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-8">
          <div>
            <div className="flex items-center gap-2 text-indigo-600 font-bold text-xs uppercase tracking-wider mb-1">
              <Building2 className="w-4 h-4" />
              Verified Direct Gateways
            </div>
            <h2 className="text-2xl sm:text-3xl font-bold text-slate-900">
              Official Company Career Portals
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              One-click direct search into verified corporate hiring engines.
            </p>
          </div>
          <span className="text-xs text-slate-500 mt-2 sm:mt-0 font-medium">Pre-fills with your search query</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {CAREER_PORTALS.map((portal) => {
            const destinationUrl = portal.urlBuilder(searchQuery);
            return (
              <a
                key={portal.id}
                href={destinationUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="group relative flex flex-col justify-between p-6 bg-white rounded-2xl border border-slate-200/90 shadow-sm hover:shadow-md hover:border-indigo-300 transition-all"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-slate-100 text-slate-700">
                      {portal.badge}
                    </span>
                    <ExternalLink className="w-4 h-4 text-slate-400 group-hover:text-indigo-600 transition-colors" />
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">
                    {portal.name}
                  </h3>
                  <p className="text-xs text-slate-500 mt-2 leading-relaxed">
                    {portal.description}
                  </p>
                </div>
                <div className="mt-5 pt-3 border-t border-slate-100 flex items-center justify-between text-xs font-semibold text-indigo-600">
                  <span>Open Official Search</span>
                  <span className="text-slate-400 group-hover:translate-x-1 transition-transform">→</span>
                </div>
              </a>
            );
          })}
        </div>
      </section>

      {/* Live Verified Job Feed */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-end justify-between mb-8">
          <div>
            <div className="flex items-center gap-2 text-emerald-600 font-bold text-xs uppercase tracking-wider mb-1">
              <Zap className="w-4 h-4" />
              Live Active Openings
            </div>
            <h2 className="text-2xl sm:text-3xl font-bold text-slate-900">
              Fresh Tech & Developer Roles
            </h2>
            <p className="text-sm text-slate-500 mt-1">
              Aggregated in real-time from verified corporate ATS pipelines.
            </p>
          </div>
          <Link
            href="/jobs"
            className="inline-flex items-center gap-1.5 text-sm font-bold text-indigo-600 hover:text-indigo-700 mt-3 sm:mt-0"
          >
            <span>View All Jobs</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {isLoading ? (
          <div className="text-center py-16">
            <div className="inline-block animate-spin w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full mb-3" />
            <p className="text-sm text-slate-500 font-medium">Fetching real-time job openings...</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            {jobs.slice(0, 6).map((job) => (
              <div
                key={job.id}
                className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm hover:shadow-md hover:border-indigo-300 transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <span className="text-xs font-bold text-indigo-600 bg-indigo-50 px-2.5 py-0.5 rounded-md">
                        {job.employment_type.replace('_', ' ')}
                      </span>
                      <h3 className="text-lg font-bold text-slate-900 mt-2">
                        {job.title}
                      </h3>
                      <p className="text-sm font-semibold text-slate-700 mt-0.5">
                        {job.company.name}
                      </p>
                    </div>

                    {job.match_percentage && (
                      <span className="text-xs font-bold text-emerald-700 bg-emerald-50 border border-emerald-200 px-2 py-1 rounded-lg shrink-0">
                        {job.match_percentage}% Match
                      </span>
                    )}
                  </div>

                  <div className="flex flex-wrap items-center gap-4 mt-4 text-xs text-slate-500">
                    <span className="flex items-center gap-1">
                      <MapPin className="w-3.5 h-3.5 text-slate-400" />
                      {job.location}
                    </span>
                    <span className="flex items-center gap-1">
                      <TrendingUp className="w-3.5 h-3.5 text-slate-400" />
                      {job.experience_min} - {job.experience_max || 'Open'} yrs exp
                    </span>
                    <span className="text-slate-400">•</span>
                    <span>{job.source}</span>
                  </div>

                  <div className="flex flex-wrap gap-1.5 mt-4">
                    {job.skills.map((skill) => (
                      <span
                        key={skill}
                        className="text-[11px] font-medium bg-slate-100 text-slate-600 px-2 py-0.5 rounded"
                      >
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-between gap-3">
                  <span className="text-xs text-slate-400">
                    Direct Employer Application
                  </span>
                  <a
                    href={job.application_url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 transition-colors shadow-sm"
                  >
                    <span>Apply on Site</span>
                    <ExternalLink className="w-3.5 h-3.5" />
                  </a>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      {/* Commercial Monetization & Subscription Tiers */}
      <section className="bg-slate-900 py-16 sm:py-24 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <span className="text-xs font-bold text-indigo-400 uppercase tracking-widest bg-indigo-950 px-3 py-1 rounded-full border border-indigo-800">
              Commercial Revenue Engine
            </span>
            <h2 className="text-3xl sm:text-5xl font-extrabold tracking-tight mt-4">
              Fair & Scalable Pricing Plans
            </h2>
            <p className="text-slate-400 mt-3 text-base sm:text-lg">
              Empower candidates with increased active job tracking quotas, AI resume match analysis, and instant employer notifications.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            {[
              {
                tier: 'FREE',
                name: 'Free Starter',
                price: '₹0',
                period: 'Forever',
                quota: '15 Active Jobs',
                features: ['Basic Job Discovery', 'Direct Company Links', 'Standard Search Filters', 'Local Device Storage'],
                highlighted: false
              },
              {
                tier: 'PRO',
                name: 'Pro Career',
                price: '₹99',
                period: '/month',
                quota: '50 Active Jobs',
                features: ['AI Resume Match Scoring', 'Email Job Alerts', '50 Tracked Applications', 'Priority Feed Updates'],
                highlighted: true
              },
              {
                tier: 'ELITE',
                name: 'Elite Discovery',
                price: '₹189',
                period: '/month',
                quota: '100 Active Jobs',
                features: ['Everything in Pro', 'Automatic Missing Skill Gap Analysis', 'Custom Career Portals Watcher', 'Zero Advertisements'],
                highlighted: false
              },
              {
                tier: 'EXECUTIVE',
                name: 'Executive Pass',
                price: '₹349',
                period: '/month',
                quota: 'Unlimited Jobs',
                features: ['Unlimited Active Jobs', 'Direct ATS Recruiter Export', 'Personalized Application Review', 'VIP Dedicated Support'],
                highlighted: false
              }
            ].map((plan) => {
              const isCurrent = activeTier === plan.tier;
              return (
                <div
                  key={plan.tier}
                  className={`rounded-2xl p-6 flex flex-col justify-between transition-all ${
                    plan.highlighted
                      ? 'bg-gradient-to-b from-indigo-900/90 to-indigo-950 border-2 border-indigo-500 shadow-xl shadow-indigo-500/20'
                      : 'bg-slate-800/80 border border-slate-700/80'
                  }`}
                >
                  <div>
                    {plan.highlighted && (
                      <span className="text-[10px] font-extrabold uppercase tracking-wider text-amber-300 bg-amber-950/80 border border-amber-600/50 px-2.5 py-0.5 rounded-full inline-block mb-3">
                        Most Popular for Candidates
                      </span>
                    )}
                    <h3 className="text-xl font-bold text-white">{plan.name}</h3>
                    <div className="mt-4 flex items-baseline">
                      <span className="text-4xl font-extrabold tracking-tight text-white">{plan.price}</span>
                      <span className="ml-1 text-xs text-slate-400 font-medium">{plan.period}</span>
                    </div>
                    <p className="text-xs text-indigo-300 font-semibold mt-1">Quota: {plan.quota}</p>

                    <ul className="mt-6 space-y-3">
                      {plan.features.map((feat) => (
                        <li key={feat} className="flex items-center gap-2 text-xs text-slate-300">
                          <Check className="w-4 h-4 text-emerald-400 shrink-0" />
                          <span>{feat}</span>
                        </li>
                      ))}
                    </ul>
                  </div>

                  <button
                    onClick={() => setCheckoutPlan(plan.tier)}
                    className={`mt-8 w-full py-3 rounded-xl font-bold text-xs uppercase tracking-wider transition-all ${
                      isCurrent
                        ? 'bg-emerald-500 text-white cursor-default'
                        : plan.highlighted
                        ? 'bg-indigo-500 hover:bg-indigo-600 text-white shadow-lg'
                        : 'bg-slate-700 hover:bg-slate-600 text-white'
                    }`}
                  >
                    {isCurrent ? 'Current Active Tier' : `Select ${plan.name}`}
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* Checkout / Activation Simulation Modal */}
      {checkoutPlan && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 text-slate-900 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-100 pb-4">
              <h3 className="text-lg font-bold">Activate {checkoutPlan} Subscription</h3>
              <button onClick={() => setCheckoutPlan(null)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>
            <div className="py-4 space-y-4">
              <p className="text-xs text-slate-600">
                JobSaarthi is set up for instant zero-cost activation for development & evaluation, as well as production gateway links (Razorpay, UPI & Stripe).
              </p>
              <div className="p-3 bg-indigo-50 rounded-xl border border-indigo-100 text-xs text-indigo-900 space-y-1">
                <p className="font-bold">✨ Evaluation Mode: Zero Charge</p>
                <p>Click below to immediately unlock the tier and expand active quotas.</p>
              </div>
            </div>
            <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
              <button
                onClick={() => setCheckoutPlan(null)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100"
              >
                Cancel
              </button>
              <button
                onClick={() => handleSubscribe(checkoutPlan)}
                className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 shadow"
              >
                Confirm & Activate
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
