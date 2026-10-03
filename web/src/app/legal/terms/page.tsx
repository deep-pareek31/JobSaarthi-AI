import { FileCheck } from 'lucide-react';

export default function TermsPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16 space-y-8 text-slate-800">
      <div className="border-b border-slate-200 pb-6">
        <div className="inline-flex items-center gap-2 text-indigo-600 font-bold text-xs uppercase tracking-wider mb-2">
          <FileCheck className="w-4 h-4" />
          Consumer Terms & Disclaimers
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Terms of Service</h1>
        <p className="text-xs text-slate-500 mt-1">Effective Date: September 2026 • Commercial Release 1.0.0</p>
      </div>

      <div className="prose prose-slate max-w-none text-sm space-y-6 leading-relaxed">
        <section className="space-y-2">
          <h2 className="text-lg font-bold text-slate-900">1. Nature of Service</h2>
          <p className="text-slate-600">
            JobSaarthi is an independent career search discovery platform and pipeline tracker. JobSaarthi aggregates verified job listings and provides direct links to hiring employers and public ATS portals.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold text-slate-900">2. No Guarantee of Employment</h2>
          <p className="text-slate-600">
            JobSaarthi does not guarantee interview selection, job offers, or specific compensation outcomes. Hiring decisions, eligibility criteria, and candidate screenings are solely the responsibility of the respective hiring employers.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold text-slate-900">3. Commercial Subscriptions</h2>
          <p className="text-slate-600">
            Subscriptions provide enhanced search quotas, priority feed access, and AI match analysis. Subscriptions can be canceled at any time. In development and evaluation modes, subscriptions are activated at zero cost.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold text-slate-900">4. Contact & Inquiries</h2>
          <p className="text-slate-600">
            For operational questions or feedback, reach our team at <strong>support@jobsaarthi.com</strong>.
          </p>
        </section>
      </div>
    </div>
  );
}
