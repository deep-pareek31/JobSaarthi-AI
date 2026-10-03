import { ShieldCheck } from 'lucide-react';

export default function PrivacyPage() {
  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16 space-y-8 text-slate-800">
      <div className="border-b border-slate-200 pb-6">
        <div className="inline-flex items-center gap-2 text-indigo-600 font-bold text-xs uppercase tracking-wider mb-2">
          <ShieldCheck className="w-4 h-4" />
          Data Safety & Policy Compliance
        </div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">Privacy Policy</h1>
        <p className="text-xs text-slate-500 mt-1">Effective Date: September 2026 • Commercial Release 1.0.0</p>
      </div>

      <div className="prose prose-slate max-w-none text-sm space-y-6 leading-relaxed">
        <section className="space-y-2">
          <h2 className="text-lg font-bold text-slate-900">1. Information We Collect</h2>
          <p className="text-slate-600">
            JobSaarthi collects only necessary professional credentials to deliver career discovery and job match calculations:
          </p>
          <ul className="list-disc pl-5 space-y-1 text-slate-600">
            <li><strong>Account & Contact:</strong> Full name, verified email address, and academic degree.</li>
            <li><strong>Professional Skills:</strong> User-declared competencies, preferred locations, and target roles.</li>
            <li><strong>Application Tracking:</strong> Locally saved jobs and pipeline stage records.</li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold text-slate-900">2. How Data Is Processed</h2>
          <p className="text-slate-600">
            Your data is used solely to match your qualifications against active employer job listings and provide direct application navigation. We <strong>never</strong> sell candidate information, resumes, or emails to third-party data brokers.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold text-slate-900">3. Direct Employer Links & External Portals</h2>
          <p className="text-slate-600">
            When you tap &quot;Apply on Site&quot;, you are redirected directly to the official employer&apos;s ATS or career portal (such as Greenhouse, Lever, Google Careers, or Amazon Jobs). Any submission on the employer portal is subject to that specific company&apos;s privacy policy.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold text-slate-900">4. Your Right to Data Deletion</h2>
          <p className="text-slate-600">
            In compliance with global privacy regulations and Google Play Data Safety mandates, candidates can purge all stored records at any time using the &quot;Delete Account &amp; Purge Data&quot; button in Profile or by contacting support@jobsaarthi.com.
          </p>
        </section>
      </div>
    </div>
  );
}
