def test_health_check(client):
    response = client.get("/api/v1/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"

def test_register_user_success(client):
    payload = {
        "email": "testcandidate@example.com",
        "password": "Password123!",
        "full_name": "Test Candidate",
        "phone": "+919876543210"
    }
    response = client.post("/api/v1/auth/register", json=payload)
    assert response.status_code == 201
    data = response.json()
    assert data["success"] is True
    assert data["data"]["user"]["email"] == "testcandidate@example.com"
    assert "access_token" in data["data"]["tokens"]
    assert "refresh_token" in data["data"]["tokens"]

def test_register_duplicate_email_fails(client):
    payload = {
        "email": "duplicate@example.com",
        "password": "Password123!",
        "full_name": "Candidate One"
    }
    res1 = client.post("/api/v1/auth/register", json=payload)
    assert res1.status_code == 201

    res2 = client.post("/api/v1/auth/register", json=payload)
    assert res2.status_code == 409

def test_login_success_and_me_endpoint(client):
    register_payload = {
        "email": "loginuser@example.com",
        "password": "CorrectPassword123!",
        "full_name": "Login User"
    }
    client.post("/api/v1/auth/register", json=register_payload)

    login_res = client.post("/api/v1/auth/login", json={
        "email": "loginuser@example.com",
        "password": "CorrectPassword123!"
    })
    assert login_res.status_code == 200
    login_data = login_res.json()["data"]
    token = login_data["tokens"]["access_token"]

    # Test /auth/me
    me_res = client.get("/api/v1/auth/me", headers={"Authorization": f"Bearer {token}"})
    assert me_res.status_code == 200
    assert me_res.json()["data"]["email"] == "loginuser@example.com"

def test_login_invalid_password_fails(client):
    register_payload = {
        "email": "wrongpass@example.com",
        "password": "RealPassword123!",
        "full_name": "Wrong Pass User"
    }
    client.post("/api/v1/auth/register", json=register_payload)

    login_res = client.post("/api/v1/auth/login", json={
        "email": "wrongpass@example.com",
        "password": "WrongPassword!"
    })
    assert login_res.status_code == 401

def test_refresh_token(client):
    register_payload = {
        "email": "refresher@example.com",
        "password": "Password123!",
        "full_name": "Refresher User"
    }
    reg_res = client.post("/api/v1/auth/register", json=register_payload)
    refresh_token = reg_res.json()["data"]["tokens"]["refresh_token"]

    refresh_res = client.post("/api/v1/auth/refresh", json={"refresh_token": refresh_token})
    assert refresh_res.status_code == 200
    assert "access_token" in refresh_res.json()["data"]
