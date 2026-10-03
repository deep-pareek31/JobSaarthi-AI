'use client';

import { useState, useEffect } from 'react';
import { User, FileText, Sparkles, Shield, Trash2, CheckCircle2, Bookmark, Star } from 'lucide-react';
import Link from 'next/link';

export default function ProfilePage() {
  const [candidateName, setCandidateName] = useState('Deep Pareek');
  const [email, setEmail] = useState('deep.pareek31@gmail.com');
  const [degree, setDegree] = useState('B.Tech in Computer Science');
  const [graduationYear, setGraduationYear] = useState('2026');
  const [skills, setSkills] = useState(['Python', 'Kotlin', 'React', 'FastAPI', 'PostgreSQL', 'Machine Learning']);
  const [newSkill, setNewSkill] = useState('');
  const [resumeText, setResumeText] = useState('');
  const [isSavedNotice, setIsSavedNotice] = useState(false);

  const handleAddSkill = (e: React.FormEvent) => {
    e.preventDefault();
    if (newSkill.trim() && !skills.includes(newSkill.trim())) {
      setSkills([...skills, newSkill.trim()]);
      setNewSkill('');
    }
  };

  const handleRemoveSkill = (skillToRemove: string) => {
    setSkills(skills.filter(s => s !== skillToRemove));
  };

  const handleSaveProfile = () => {
    setIsSavedNotice(true);
    setTimeout(() => setIsSavedNotice(false), 3000);
  };

  const handlePurgeData = () => {
    if (confirm('Are you sure you want to permanently erase your profile, saved jobs, and tracked data?')) {
      localStorage.clear();
      alert('All local profile, resume, and application data have been permanently erased in compliance with Data Safety guidelines.');
      window.location.reload();
    }
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-12 space-y-10">
      <div>
        <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
          Candidate Profile & AI Matching
        </h1>
        <p className="text-sm text-slate-500 mt-1">
          Manage your verified credentials, skills, and AI resume match configuration.
        </p>
      </div>

      {isSavedNotice && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-xl text-xs font-semibold flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600" />
          Profile updated successfully! AI matching algorithms will use these parameters.
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* Left Column: Account Details & Subscription */}
        <div className="space-y-6">
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-xl bg-indigo-600 flex items-center justify-center text-white font-bold text-lg">
                {candidateName.charAt(0)}
              </div>
              <div>
                <h3 className="text-base font-bold text-slate-900">{candidateName}</h3>
                <p className="text-xs text-slate-500">{email}</p>
              </div>
            </div>

            <div className="pt-3 border-t border-slate-100 flex items-center justify-between text-xs">
              <span className="text-slate-500 font-medium">Subscription Tier:</span>
              <span className="px-2.5 py-1 rounded-md bg-amber-50 border border-amber-200 text-amber-700 font-bold">
                Pro Candidate
              </span>
            </div>

            <Link
              href="/pricing"
              className="w-full inline-block text-center py-2.5 rounded-xl text-xs font-bold text-indigo-600 bg-indigo-50 hover:bg-indigo-100 transition-colors"
            >
              Manage Subscription Plan
            </Link>
          </div>

          {/* Data Safety & Privacy Card */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-3">
            <div className="flex items-center gap-2 text-xs font-bold text-slate-900">
              <Shield className="w-4 h-4 text-indigo-600" />
              Privacy & Data Rights
            </div>
            <p className="text-xs text-slate-500 leading-relaxed">
              In full compliance with Data Protection policies, you maintain 100% control over your credentials and resume content.
            </p>
            <button
              onClick={handlePurgeData}
              className="w-full py-2.5 rounded-xl border border-red-200 text-red-600 hover:bg-red-50 text-xs font-bold transition-colors flex items-center justify-center gap-1.5"
            >
              <Trash2 className="w-3.5 h-3.5" />
              <span>Delete Account & Purge Data</span>
            </button>
          </div>
        </div>

        {/* Right Column: Profile Form & Skills */}
        <div className="md:col-span-2 space-y-6">
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
            <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <User className="w-5 h-5 text-indigo-600" />
              Personal & Academic Details
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Full Name</label>
                <input
                  type="text"
                  value={candidateName}
                  onChange={(e) => setCandidateName(e.target.value)}
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-indigo-600 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Email Address</label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-indigo-600 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Degree / Specialization</label>
                <input
                  type="text"
                  value={degree}
                  onChange={(e) => setDegree(e.target.value)}
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-indigo-600 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Graduation Year</label>
                <input
                  type="text"
                  value={graduationYear}
                  onChange={(e) => setGraduationYear(e.target.value)}
                  className="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-indigo-600 focus:outline-none"
                />
              </div>
            </div>
          </div>

          {/* Verified Skills & Technologies */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-xs space-y-4">
            <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-indigo-600" />
              Verified Skills & AI Tags
            </h3>
            <p className="text-xs text-slate-500">
              These skill tags are used to calculate the real-time match percentage on live job postings.
            </p>

            <div className="flex flex-wrap gap-2">
              {skills.map((skill) => (
                <span
                  key={skill}
                  className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg bg-indigo-50 border border-indigo-100 text-indigo-700 text-xs font-semibold"
                >
                  {skill}
                  <button
                    onClick={() => handleRemoveSkill(skill)}
                    className="text-indigo-400 hover:text-indigo-700 font-bold ml-1"
                  >
                    ×
                  </button>
                </span>
              ))}
            </div>

            <form onSubmit={handleAddSkill} className="flex gap-2 pt-2">
              <input
                type="text"
                value={newSkill}
                onChange={(e) => setNewSkill(e.target.value)}
                placeholder="Add skill (e.g., Docker, TypeScript, Go)..."
                className="flex-1 px-3.5 py-2 rounded-xl border border-slate-200 text-xs focus:ring-2 focus:ring-indigo-600 focus:outline-none"
              />
              <button
                type="submit"
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold"
              >
                Add Skill
              </button>
            </form>
          </div>

          <div className="flex justify-end">
            <button
              onClick={handleSaveProfile}
              className="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold shadow-sm transition-all"
            >
              Save Profile & Update Matching
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
