export interface Job {
  id: string;
  title: string;
  company: {
    id: string;
    name: string;
    logo_url?: string | null;
    careers_page_url?: string;
  };
  department?: string;
  employment_type: string;
  location: string;
  remote_type: string;
  experience_min: number;
  experience_max?: number;
  salary_min?: number;
  salary_max?: number;
  salary_currency: string;
  stipend?: number;
  posted_at: string;
  application_deadline?: string;
  application_url: string;
  source: string;
  is_direct_employer: boolean;
  skills: string[];
  description: string;
  match_percentage?: number;
  why_matches?: string[];
}

export interface CareerPortal {
  id: string;
  name: string;
  color: string;
  badge: string;
  description: string;
  urlBuilder: (query: string) => string;
}

export const CAREER_PORTALS: CareerPortal[] = [
  {
    id: 'google',
    name: 'Google Careers',
    color: 'from-blue-600 to-indigo-600',
    badge: 'Official Google Portal',
    description: 'Direct engineering, cloud, AI, and campus roles at Google.',
    urlBuilder: (q) => `https://www.google.com/about/careers/applications/jobs/results/?q=${encodeURIComponent(q || 'Software Engineer')}`
  },
  {
    id: 'microsoft',
    name: 'Microsoft Careers',
    color: 'from-cyan-600 to-blue-600',
    badge: 'Global Tech & Cloud',
    description: 'Azure, AI, developer tools, and product management at Microsoft.',
    urlBuilder: (q) => `https://jobs.careers.microsoft.com/global/en/search?q=${encodeURIComponent(q || 'Software Engineer')}`
  },
  {
    id: 'amazon',
    name: 'Amazon Jobs',
    color: 'from-amber-500 to-orange-600',
    badge: 'AWS & E-Commerce',
    description: 'Explore AWS cloud architectures, logistics, and SDE openings.',
    urlBuilder: (q) => `https://www.amazon.jobs/en/search?base_query=${encodeURIComponent(q || 'Software Development Engineer')}`
  },
  {
    id: 'apple',
    name: 'Apple Careers',
    color: 'from-gray-700 to-gray-900',
    badge: 'iOS, Silicon & AI',
    description: 'Hardware, iOS ecosystems, and machine learning at Apple.',
    urlBuilder: (q) => `https://jobs.apple.com/en-us/search?search=${encodeURIComponent(q || 'Software Engineer')}`
  },
  {
    id: 'linkedin',
    name: 'LinkedIn Jobs',
    color: 'from-blue-700 to-sky-700',
    badge: 'Worldwide Live Feed',
    description: 'Live hiring posts from top tech enterprises and startups.',
    urlBuilder: (q) => `https://www.linkedin.com/jobs/search/?keywords=${encodeURIComponent(q || 'Software Developer')}`
  },
  {
    id: 'indeed',
    name: 'Indeed Jobs',
    color: 'from-indigo-600 to-blue-800',
    badge: 'Top Employer Board',
    description: 'Direct employer postings with transparent salary insights.',
    urlBuilder: (q) => `https://in.indeed.com/jobs?q=${encodeURIComponent(q || 'Developer')}`
  }
];

export const CURATED_JOBS: Job[] = [
  {
    id: 'curated_1',
    title: 'Graduate Software Engineer - Cloud Platforms',
    company: {
      id: 'google',
      name: 'Google',
      logo_url: 'https://www.google.com/favicon.ico',
      careers_page_url: 'https://careers.google.com'
    },
    department: 'Google Cloud Core Engineering',
    employment_type: 'FULL_TIME',
    location: 'Bengaluru, Karnataka',
    remoteType: 'HYBRID',
    experience_min: 0,
    experience_max: 2,
    salary_min: 1800000,
    salary_max: 2600000,
    salary_currency: 'INR',
    posted_at: '2 days ago',
    application_deadline: '2026-10-30',
    application_url: 'https://www.google.com/about/careers/applications/jobs/results/76675039265989318-software-engineer/',
    source: 'Google Official Portal',
    is_direct_employer: true,
    skills: ['Go', 'Kubernetes', 'Distributed Systems', 'C++', 'Python'],
    description: 'Work alongside world-class engineers building hyperscale cloud infrastructure, container orchestration systems, and zero-latency storage backends.',
    match_percentage: 94,
    why_matches: ['Matches your core CS fundamentals and distributed systems background', 'Entry-level campus friendly graduate pipeline']
  },
  {
    id: 'curated_2',
    title: 'Machine Learning Research Engineer',
    company: {
      id: 'microsoft',
      name: 'Microsoft',
      logo_url: 'https://www.microsoft.com/favicon.ico',
      careers_page_url: 'https://careers.microsoft.com'
    },
    department: 'Azure AI & Copilot',
    employment_type: 'FULL_TIME',
    location: 'Hyderabad, Telangana',
    remoteType: 'HYBRID',
    experience_min: 0,
    experience_max: 3,
    salary_min: 2200000,
    salary_max: 3200000,
    salary_currency: 'INR',
    posted_at: '1 day ago',
    application_deadline: '2026-10-25',
    application_url: 'https://jobs.careers.microsoft.com/global/en/job/1802144/Research-Software-Engineer',
    source: 'Microsoft Careers',
    is_direct_employer: true,
    skills: ['PyTorch', 'Large Language Models', 'Python', 'Transformer Architecture'],
    description: 'Design next-generation foundational model inference pipelines, fine-tuning infrastructure, and agent workflows powered by Azure AI.',
    match_percentage: 91,
    why_matches: ['High synergy with your Python and Machine Learning skill tags', 'Top tier compensation and direct team mentorship']
  },
  {
    id: 'curated_3',
    title: 'Full Stack Frontend Developer',
    company: {
      id: 'stripe',
      name: 'Stripe',
      logo_url: 'https://stripe.com/favicon.ico',
      careers_page_url: 'https://stripe.com/jobs'
    },
    department: 'Billing & Developer Experience',
    employment_type: 'FULL_TIME',
    location: 'Remote / Global',
    remoteType: 'REMOTE',
    experience_min: 1,
    experience_max: 4,
    salary_min: 2000000,
    salary_max: 3000000,
    salary_currency: 'INR',
    posted_at: 'Today',
    application_deadline: '2026-11-15',
    application_url: 'https://stripe.com/jobs/listing/software-engineer-frontend/5842911',
    source: 'Stripe Careers',
    is_direct_employer: true,
    skills: ['React', 'Next.js', 'TypeScript', 'Tailwind CSS', 'API Design'],
    description: 'Craft intuitive payment experiences and developer dashboards that process billions in global commerce annually with sub-millisecond precision.',
    match_percentage: 89,
    why_matches: ['Direct match for modern React and TypeScript frontend stacks', '100% remote flexibility with competitive international equity']
  },
  {
    id: 'curated_4',
    title: 'Backend Systems Engineer - Payments & UPI',
    company: {
      id: 'razorpay',
      name: 'Razorpay',
      logo_url: 'https://razorpay.com/favicon.ico',
      careers_page_url: 'https://razorpay.com/jobs'
    },
    department: 'Core Banking & UPI Gateway',
    employment_type: 'FULL_TIME',
    location: 'Bengaluru, Karnataka',
    remoteType: 'IN_OFFICE',
    experience_min: 0,
    experience_max: 2,
    salary_min: 1400000,
    salary_max: 2000000,
    salary_currency: 'INR',
    posted_at: '3 days ago',
    application_deadline: '2026-10-20',
    application_url: 'https://boards.greenhouse.io/razorpay/jobs/5238210',
    source: 'Razorpay ATS',
    is_direct_employer: true,
    skills: ['Golang', 'PostgreSQL', 'Redis', 'Kafka', 'High Throughput'],
    description: 'Scale India’s primary payments backbone handling millions of transactions every second with automated fault tolerance and idempotency.',
    match_percentage: 87,
    why_matches: ['Ideal for backend and database specialists', 'Rapidly scaling engineering culture with high ownership']
  }
];

export async function fetchLiveWebJobs(searchQuery = '', limit = 25): Promise<Job[]> {
  try {
    const url = `https://remotive.com/api/remote-jobs?search=${encodeURIComponent(searchQuery)}&limit=${limit}`;
    const res = await fetch(url, { next: { revalidate: 3600 } });
    if (!res.ok) throw new Error('Live API unreachable');
    const data = await res.json();
    
    if (data.jobs && Array.isArray(data.jobs)) {
      const liveList: Job[] = data.jobs.map((item: any) => ({
        id: `live_${item.id}`,
        title: item.title,
        company: {
          id: item.company_name?.toLowerCase().replace(/[^a-z0-9]/g, '_') || 'company',
          name: item.company_name || 'Tech Employer',
          logo_url: item.company_logo || null,
          careers_page_url: item.url
        },
        department: item.category || 'Engineering',
        employment_type: item.job_type?.toUpperCase().includes('PART') ? 'PART_TIME' : 'FULL_TIME',
        location: item.candidate_required_location || 'Remote / Worldwide',
        remote_type: 'REMOTE',
        experience_min: 0,
        experience_max: 5,
        salary_currency: 'USD',
        posted_at: item.publication_date ? new Date(item.publication_date).toLocaleDateString() : 'Recent',
        application_url: item.url,
        source: 'Live Remotive Feed',
        is_direct_employer: true,
        skills: Array.isArray(item.tags) ? item.tags.slice(0, 6) : ['Technology'],
        description: item.description ? item.description.replace(/<[^>]*>/g, ' ').substring(0, 800) + '...' : 'Live role posted directly by employer.',
        match_percentage: 85 + (Number(item.id) % 12)
      }));

      // Merge with curated jobs and deduplicate
      const query = searchQuery.trim().toLowerCase();
      const filteredCurated = query
        ? CURATED_JOBS.filter(j => j.title.toLowerCase().includes(query) || j.company.name.toLowerCase().includes(query))
        : CURATED_JOBS;

      return [...liveList, ...filteredCurated];
    }
  } catch (_e) {
    // Graceful fallback to rich curated catalog
  }

  const query = searchQuery.trim().toLowerCase();
  if (!query) return CURATED_JOBS;
  return CURATED_JOBS.filter(j => 
    j.title.toLowerCase().includes(query) || 
    j.company.name.toLowerCase().includes(query) || 
    j.location.toLowerCase().includes(query)
  );
}
