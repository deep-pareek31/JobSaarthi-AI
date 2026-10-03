'use client';

import { useState } from 'react';
import { Check, Sparkles, ShieldCheck, Zap, HelpCircle } from 'lucide-react';

export default function PricingPage() {
  const [selectedPlan, setSelectedPlan] = useState<string | null>(null);
  const [currentTier, setCurrentTier] = useState('FREE');

  const handleActivate = (tier: string) => {
    setCurrentTier(tier);
    setSelectedPlan(null);
    alert(`🎉 Successfully upgraded to ${tier}! Your active job quota and AI features are now unlocked.`);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 space-y-16">
      <div className="text-center max-w-3xl mx-auto space-y-4">
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-50 border border-indigo-200 text-indigo-700 text-xs font-bold uppercase tracking-wider">
          <Sparkles className="w-3.5 h-3.5" />
          Candidate Value & Monetization
        </div>
        <h1 className="text-4xl sm:text-5xl font-extrabold text-slate-900 tracking-tight">
          Invest in Your Next Career Leap
        </h1>
        <p className="text-base text-slate-600 leading-relaxed">
          Transparent, low-barrier monthly plans. Designed for campus graduates, tech professionals, and career switchers across India and worldwide.
        </p>
      </div>

      {/* Pricing Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {[
          {
            tier: 'FREE',
            name: 'Free Starter',
            price: '₹0',
            period: 'Forever',
            tag: 'Basic Discovery',
            quota: '15 Active Jobs',
            description: 'Ideal for casual job browsing and manual application tracking.',
            features: [
              '15 Tracked Jobs Quota',
              'Direct Corporate Links',
              'Official Portals Access',
              'Local Browser Storage',
              'Community Support'
            ],
            highlight: false
          },
          {
            tier: 'PRO',
            name: 'Pro Career',
            price: '₹99',
            period: '/month',
            tag: 'Most Popular',
            quota: '50 Active Jobs',
            description: 'For active applicants seeking verified roles and AI resume matches.',
            features: [
              '50 Active Jobs Quota',
              'AI Resume Match Scoring',
              'Missing Skill Gap Analysis',
              'Priority Feed Refresh',
              'Email Status Alerts'
            ],
            highlight: true
          },
          {
            tier: 'ELITE',
            name: 'Elite Discovery',
            price: '₹189',
            period: '/month',
            tag: 'High Performance',
            quota: '100 Active Jobs',
            description: 'Tailored for senior engineers and candidates managing multiple pipelines.',
            features: [
              '100 Active Jobs Quota',
              'Custom Career Portals Watcher',
              'Automated Daily Digestion',
              'Application Stage Timeline',
              'Ad-Free Experience'
            ],
            highlight: false
          },
          {
            tier: 'EXECUTIVE',
            name: 'Executive Pass',
            price: '₹349',
            period: '/month',
            tag: 'Full Concierge',
            quota: 'Unlimited Jobs',
            description: 'Maximum quota and personalized ATS profile review.',
            features: [
              'Unlimited Tracked Applications',
              'Direct Recruiter Resume Export',
              'Personalized Profile Optimization',
              '1-on-1 Support Desk'
            ],
            highlight: false
          }
        ].map((plan) => {
          const isCurrent = currentTier === plan.tier;
          return (
            <div
              key={plan.tier}
              className={`rounded-2xl p-6 flex flex-col justify-between bg-white border transition-all ${
                plan.highlight
                  ? 'border-indigo-600 ring-2 ring-indigo-600 shadow-xl shadow-indigo-100'
                  : 'border-slate-200 shadow-sm hover:border-slate-300'
              }`}
            >
              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider text-indigo-700 bg-indigo-50 px-2.5 py-1 rounded-md inline-block">
                  {plan.tag}
                </span>
                <h3 className="text-xl font-bold text-slate-900 mt-3">{plan.name}</h3>
                <p className="text-xs text-slate-500 mt-1">{plan.description}</p>

                <div className="mt-5 flex items-baseline">
                  <span className="text-4xl font-extrabold text-slate-900 tracking-tight">{plan.price}</span>
                  <span className="ml-1 text-xs text-slate-500 font-medium">{plan.period}</span>
                </div>
                <div className="mt-2 text-xs font-bold text-indigo-600 bg-indigo-50/80 p-2 rounded-lg">
                  ⚡ Quota: {plan.quota}
                </div>

                <ul className="mt-6 space-y-3 border-t border-slate-100 pt-5">
                  {plan.features.map((feat) => (
                    <li key={feat} className="flex items-center gap-2 text-xs text-slate-600">
                      <Check className="w-4 h-4 text-emerald-500 shrink-0" />
                      <span>{feat}</span>
                    </li>
                  ))}
                </ul>
              </div>

              <button
                onClick={() => setSelectedPlan(plan.tier)}
                className={`mt-8 w-full py-3 rounded-xl font-bold text-xs uppercase tracking-wider transition-all ${
                  isCurrent
                    ? 'bg-emerald-600 text-white cursor-default'
                    : plan.highlight
                    ? 'bg-indigo-600 hover:bg-indigo-700 text-white shadow-md'
                    : 'bg-slate-900 hover:bg-slate-800 text-white'
                }`}
              >
                {isCurrent ? 'Current Plan' : `Upgrade to ${plan.name}`}
              </button>
            </div>
          );
        })}
      </div>

      {/* Trust & Guarantee Banner */}
      <div className="bg-slate-900 text-white p-8 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-6">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-indigo-600 flex items-center justify-center shrink-0">
            <ShieldCheck className="w-6 h-6 text-white" />
          </div>
          <div>
            <h4 className="text-lg font-bold">100% Transparent Consumer Guarantee</h4>
            <p className="text-xs text-slate-400 mt-0.5">
              Cancel anytime. No lock-in contracts. Full compliance with Indian DPDP guidelines and global consumer standards.
            </p>
          </div>
        </div>
        <span className="text-xs font-bold text-emerald-400 bg-emerald-950/80 border border-emerald-800 px-3 py-1.5 rounded-lg shrink-0">
          Encrypted TLS 1.3 Security
        </span>
      </div>

      {/* Activation / Payment Modal */}
      {selectedPlan && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 text-slate-900 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-lg font-bold">Confirm Plan Activation</h3>
              <button onClick={() => setSelectedPlan(null)} className="text-slate-400 hover:text-slate-600">✕</button>
            </div>
            <div className="py-4 space-y-3">
              <p className="text-xs text-slate-600">
                You are activating the <strong>{selectedPlan}</strong> tier. In testing and sandbox mode, activation is completed immediately at zero charge.
              </p>
              <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-900">
                ✅ Zero-Cost Sandbox Activation Enabled
              </div>
            </div>
            <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-100">
              <button
                onClick={() => setSelectedPlan(null)}
                className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100"
              >
                Close
              </button>
              <button
                onClick={() => handleActivate(selectedPlan)}
                className="px-5 py-2.5 rounded-xl text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 shadow"
              >
                Confirm & Unlock Quota
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
