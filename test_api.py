import requests
import json

BASE_URL = "http://localhost:5002"
SESSION = requests.Session()

def print_res(response):
    try:
        print(f"Status: {response.status_code}")
        print(json.dumps(response.json(), indent=2, ensure_ascii=False))
    except:
        print(response.text)
    print("-" * 30)

def test_health():
    print("1. Health Check")
    res = requests.get(f"{BASE_URL}/api/")
    print_res(res)

def test_register():
    print("2. Register")
    data = {
        "username": "testuser_v2",
        "password": "password123"
    }
    res = requests.post(f"{BASE_URL}/auth/register", json=data)
    print_res(res)

def test_login():
    print("3. Login")
    data = {
        "username": "testuser_v2",
        "password": "password123"
    }
    res = SESSION.post(f"{BASE_URL}/auth/login", json=data)
    print_res(res)
    return res.status_code == 200

def test_get_events():
    print("4. Get Events")
    res = requests.get(f"{BASE_URL}/api/events")
    print_res(res)
    data = res.json()
    if data.get('data'):
        return data['data'][0]['id']
    return None

def test_bookmarks(event_id):
    if not event_id:
        print("Skipping bookmark test (no event found)")
        return

    print(f"5. Add Bookmark (Event ID: {event_id})")
    res = SESSION.post(f"{BASE_URL}/api/bookmarks/", json={"event_id": event_id})
    print_res(res)

    print("6. Get Bookmarks")
    res = SESSION.get(f"{BASE_URL}/api/bookmarks/")
    print_res(res)

    print(f"7. Delete Bookmark (Event ID: {event_id})")
    res = SESSION.delete(f"{BASE_URL}/api/bookmarks/{event_id}")
    print_res(res)

def test_user_settings():
    print("8. Update FCM Token")
    res = SESSION.post(f"{BASE_URL}/api/user/fcm-token", json={"fcm_token": "dummy_token_123"})
    print_res(res)

    print("9. Update Keywords")
    res = SESSION.post(f"{BASE_URL}/api/user/keywords", json={"keywords": "재즈,마켓"})
    print_res(res)

    print("10. Get Keywords")
    res = SESSION.get(f"{BASE_URL}/api/user/keywords")
    print_res(res)

if __name__ == "__main__":
    test_health()
    test_register()
    if test_login():
        event_id = test_get_events()
        test_bookmarks(event_id)
        test_user_settings()
    else:
        print("Login failed, skipping authenticated tests.")
