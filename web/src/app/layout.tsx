import './globals.css';
import Link from 'next/link';
import { Briefcase, Compass, ShieldCheck, Sparkles, User, Bookmark, Bell } from 'lucide-react';

export const metadata = {
  title: 'JobSaarthi - AI-Guided Career Discovery & Real Job Search Platform',
  description: 'Search live jobs from Google, Microsoft, Amazon, and top tech companies with zero fluff, direct ATS application links, and AI resume match scoring.',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="min-h-screen flex flex-col bg-slate-50 text-slate-900 antialiased selection:bg-indigo-500 selection:text-white">
        {/* Navigation Bar */}
        <header className="sticky top-0 z-50 backdrop-blur-md bg-white/90 border-b border-slate-200">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
            {/* shaadi.com-style typographic wordmark logo */}
            <Link href="/" className="flex items-center gap-2 group">
              <div className="px-3.5 py-1.5 rounded-xl bg-[#0B192C] border border-slate-800 shadow-sm group-hover:scale-105 transition-transform flex items-center gap-1">
                <span className="font-black text-xl tracking-tight text-white">
                  job<span className="text-sky-400">saarthi</span>
                </span>
                <span className="w-2 h-2 rounded-full bg-amber-400 mb-2"></span>
              </div>
              <span className="hidden sm:inline-block text-[9px] text-slate-500 font-bold tracking-widest uppercase bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
                PORTAL
              </span>
            </Link>

            <nav className="hidden md:flex items-center gap-7 text-sm font-semibold text-slate-600">
              <Link href="/jobs" className="hover:text-indigo-600 transition-colors flex items-center gap-1.5">
                <Compass className="w-4 h-4" />
                Live Jobs
              </Link>
              <Link href="/alerts" className="hover:text-indigo-600 transition-colors flex items-center gap-1.5">
                <Bell className="w-4 h-4 text-sky-500" />
                Job Alerts
              </Link>
              <Link href="/tracker" className="hover:text-indigo-600 transition-colors flex items-center gap-1.5">
                <Bookmark className="w-4 h-4" />
                Tracker
              </Link>
              <Link href="/pricing" className="hover:text-indigo-600 transition-colors flex items-center gap-1.5 text-amber-600 hover:text-amber-700">
                <Sparkles className="w-4 h-4" />
                Pricing & Plans
              </Link>
              <Link href="/profile" className="hover:text-indigo-600 transition-colors flex items-center gap-1.5">
                <User className="w-4 h-4" />
                Profile & AI Resume
              </Link>
            </nav>

            <div className="flex items-center gap-3">
              <Link
                href="/pricing"
                className="hidden sm:inline-flex items-center justify-center px-4 py-2 text-xs font-bold uppercase tracking-wider text-indigo-700 bg-indigo-50 hover:bg-indigo-100 rounded-lg border border-indigo-200 transition-all"
              >
                Pro Access
              </Link>
              <Link
                href="/jobs"
                className="inline-flex items-center justify-center px-4 py-2 text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-sm hover:shadow transition-all"
              >
                Explore Real Jobs
              </Link>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1">
          {children}
        </main>

        {/* Global Footer */}
        <footer className="bg-slate-900 text-slate-400 border-t border-slate-800 mt-20">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
              <div className="space-y-4">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white">
                    <Briefcase className="w-4 h-4" />
                  </div>
                  <span className="font-bold text-lg text-white">JobSaarthi</span>
                </div>
                <p className="text-sm leading-relaxed text-slate-400">
                  India’s premier AI-guided career discovery and application tracker. Direct employer links, verified ATS job feeds, and zero deceptive fees.
                </p>
                <div className="flex items-center gap-2 text-xs text-emerald-400 font-medium">
                  <ShieldCheck className="w-4 h-4" />
                  100% Policy Compliant & Verified Source Engine
                </div>
              </div>

              <div>
                <h4 className="text-white font-semibold text-sm mb-4">Official Portals</h4>
                <ul className="space-y-2 text-sm">
                  <li><a href="https://careers.google.com" target="_blank" rel="noopener" className="hover:text-white">Google Careers</a></li>
                  <li><a href="https://careers.microsoft.com" target="_blank" rel="noopener" className="hover:text-white">Microsoft Careers</a></li>
                  <li><a href="https://amazon.jobs" target="_blank" rel="noopener" className="hover:text-white">Amazon Jobs</a></li>
                  <li><a href="https://linkedin.com/jobs" target="_blank" rel="noopener" className="hover:text-white">LinkedIn Live Feed</a></li>
                </ul>
              </div>

              <div>
                <h4 className="text-white font-semibold text-sm mb-4">Product & Platform</h4>
                <ul className="space-y-2 text-sm">
                  <li><Link href="/jobs" className="hover:text-white">Search Active Roles</Link></li>
                  <li><Link href="/pricing" className="hover:text-white">Subscription Tiers</Link></li>
                  <li><Link href="/tracker" className="hover:text-white">Application Tracker</Link></li>
                  <li><Link href="/profile" className="hover:text-white">Resume Matching</Link></li>
                </ul>
              </div>

              <div>
                <h4 className="text-white font-semibold text-sm mb-4">Trust & Compliance</h4>
                <ul className="space-y-2 text-sm">
                  <li><Link href="/legal/privacy" className="hover:text-white">Privacy Policy</Link></li>
                  <li><Link href="/legal/terms" className="hover:text-white">Terms of Service</Link></li>
                  <li><span className="text-slate-400">Support: support@jobsaarthi.com</span></li>
                  <li><span className="text-slate-400">v1.0.0 Production Release</span></li>
                </ul>
              </div>
            </div>

            <div className="border-t border-slate-800 mt-10 pt-6 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500">
              <p>© 2026 JobSaarthi Technologies. All rights reserved.</p>
              <p className="mt-2 sm:mt-0">Independent career aggregator. Trademarks belong to their respective corporate owners.</p>
            </div>
          </div>
        </footer>
      </body>
    </html>
  );
}
