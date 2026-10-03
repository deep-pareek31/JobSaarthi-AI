def test_get_jobs_and_today(client):
    res = client.get("/api/v1/jobs")
    assert res.status_code == 200
    data = res.json()["data"]
    assert "items" in data
    assert data["total"] >= 5

    res_today = client.get("/api/v1/jobs/today")
    assert res_today.status_code == 200
    assert len(res_today.json()["data"]) >= 1

def test_recommended_jobs_and_subscription_limit(client):
    reg = client.post("/api/v1/auth/register", json={
        "email": "freeuser@example.com",
        "password": "Password123!",
        "full_name": "Free Candidate"
    })
    token = reg.json()["data"]["tokens"]["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    # Free plan should return at most 3 recommended active jobs
    rec_res = client.get("/api/v1/jobs/recommended", headers=headers)
    assert rec_res.status_code == 200
    recs = rec_res.json()["data"]
    assert len(recs) <= 3
    for r in recs:
        assert r["match_percentage"] is not None
        assert "why_matches" in r

def test_save_and_apply_job(client):
    reg = client.post("/api/v1/auth/register", json={
        "email": "trackeruser@example.com",
        "password": "Password123!",
        "full_name": "Tracker User"
    })
    token = reg.json()["data"]["tokens"]["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    # Get a job ID
    jobs_res = client.get("/api/v1/jobs")
    job_id = jobs_res.json()["data"]["items"][0]["id"]

    # Save job
    save_res = client.post(f"/api/v1/jobs/{job_id}/save?category=High%20Priority&notes=Urgent", headers=headers)
    assert save_res.status_code == 200
    assert save_res.json()["success"] is True

    # Check saved list
    saved_list = client.get("/api/v1/jobs/saved", headers=headers)
    assert saved_list.status_code == 200
    assert len(saved_list.json()["data"]) >= 1

    # Mark as applied
    apply_res = client.post("/api/v1/applications", json={
        "job_id": job_id,
        "status": "Applied",
        "notes": "Applied via official employer portal"
    }, headers=headers)
    assert apply_res.status_code == 201

    # Check applications
    apps_res = client.get("/api/v1/applications", headers=headers)
    assert apps_res.status_code == 200
    assert len(apps_res.json()["data"]) >= 1

    # Check stats
    stats_res = client.get("/api/v1/applications/stats", headers=headers)
    assert stats_res.status_code == 200
    assert stats_res.json()["data"]["total_applied"] >= 1

def test_ai_search(client):
    reg = client.post("/api/v1/auth/register", json={
        "email": "aiuser@example.com",
        "password": "Password123!",
        "full_name": "AI User"
    })
    token = reg.json()["data"]["tokens"]["access_token"]
    headers = {"Authorization": f"Bearer {token}"}

    ai_res = client.post("/api/v1/ai/search", json={
        "query": "Show me software internships in Bangalore with Python"
    }, headers=headers)
    assert ai_res.status_code == 200
    ai_data = ai_res.json()["data"]
    assert "summary" in ai_data
    assert "results" in ai_data
    assert ai_data["structured_filters"]["employment_type"] == "INTERNSHIP"

def test_get_plans(client):
    plans_res = client.get("/api/v1/plans")
    assert plans_res.status_code == 200
    plans = plans_res.json()["data"]
    assert len(plans) == 4
    tier_names = [p["plan_tier"] for p in plans]
    assert "FREE" in tier_names
    assert "PRO_99" in tier_names
    assert "ELITE_189" in tier_names
    assert "PREMIUM_CONFIGURABLE" in tier_names
