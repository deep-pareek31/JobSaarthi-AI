# JobSaarthi Ingestion Framework & Connector Architecture

## 1. Compliance & Ethical Ingestion Principles

JobSaarthi strictly adheres to ethical data discovery:
1. **Never bypass authentication, paywalls, or CAPTCHAs.**
2. **Never scrape private or protected APIs.**
3. **Respect `robots.txt` and explicit crawling directives.**
4. **Use official and public ATS endpoints (e.g. Greenhouse public board API, Lever public postings API).**
5. **Enforce adaptive rate limits and exponential backoff** to prevent load on employer servers.

## 2. Connector Interface (`JobSourceConnector`)

Every connector extends `app.workers.connectors.base.JobSourceConnector`:
- `fetch_jobs(since_timestamp) -> List[RawJobListing]`
- `fetch_job_details(external_id) -> JobDetails`
- `validate_job(external_id) -> JobValidationStatus`
- `detect_closed_job(external_id) -> bool`
- `normalize_job(raw_job) -> NormalizedJob`
- `get_source_name() -> str`

## 3. Deduplication Pipeline

Before inserting or updating a job:
1. Compute **canonical key**: `hash(company_domain + "_" + clean_title + "_" + location_city)`
2. Perform fuzzy string matching on description text if canonical key is ambiguous.
3. If duplicate detected: Link the new source URL and external ID under the existing canonical job record in `job_sources` junction, updating `last_verified_at`.
