# LocalNow Backend

인천 연수구 송도 지역의 로컬 이벤트(축제, 공연, 전시, 교육 등)를 수집하고 제공하는 백엔드 서버입니다.

## 🚀 주요 기능

1.  **이벤트 크롤링 (Crawling)**
    *   **공공데이터포털**: 공식 축제 정보
    *   **네이버 블로그**: "송도 축제", "연수구 행사" 등 키워드 검색 (광고 필터링 적용)
    *   **인천문화재단**: 문화/예술 행사
    *   **인천문화예술회관**: 공연/전시 일정
    *   **송도 주민자치센터**: 자치센터 프로그램 (송도 1~5동)
    *   **트리플스트리트**: 쇼핑몰 이벤트
    *   **연수문화재단**: 지역 문화 행사

2.  **데이터 제공 (API)**
    *   수집된 데이터를 JSON 형태로 제공
    *   카카오맵 연동을 위한 좌표(lat, lng) 포함

3.  **사용자 인증 (Auth)**
    *   로컬 회원가입/로그인
    *   구글 로그인 (ID Token 검증)

4.  **북마크 (Bookmark)**
    *   관심 있는 이벤트 저장/삭제/조회

5.  **알림 (Notification)**
    *   **키워드 알림**: 사용자가 등록한 키워드(예: "재즈", "마켓")가 포함된 새 이벤트 발견 시 푸시 알림
    *   **마감 임박 알림**: 북마크한 이벤트 시작 3일 전, 1일 전, 12시간 전, 6시간 전, 1시간 전 알림

## 🛠️ 설치 및 실행 방법

### 1. 환경 설정

Python 3.8 이상이 필요합니다.

```bash
# 가상환경 생성 (선택)
python -m venv venv
source venv/bin/activate  # Mac/Linux
# venv\Scripts\activate  # Windows

# 의존성 설치
pip install -r requirements.txt
```

### 2. 필수 파일 설정

프로젝트 루트(`backend/`)에 다음 파일들이 필요합니다.

*   `.env`: 환경 변수 설정
    ```ini
    PUBLIC_DATA_KEY=your_public_data_key
    NAVER_CLIENT_ID=your_naver_id
    NAVER_CLIENT_SECRET=your_naver_secret
    KAKAO_REST_API_KEY=your_kakao_key
    KAKAO_JS_KEY=your_kakao_js_key
    ```
*   `serviceAccountKey.json`: Firebase Admin SDK 키 (푸시 알림용)
    *   Firebase Console -> Project Settings -> Service accounts -> Generate new private key 에서 다운로드

### 3. 서버 실행

```bash
cd backend
python app.py
```
서버는 기본적으로 `http://0.0.0.0:5002`에서 실행됩니다. (포트 충돌 방지 및 안정성을 위해 5002번 사용)

### 4. 크롤링 수동 실행 (테스트용)

```bash
python trigger_crawl.py
```
*   크롤러가 실행되어 DB를 업데이트하고, 키워드 알림을 체크하여 발송합니다.

### 5. API 문서 (Swagger)

서버 실행 후 다음 주소에서 API 문서를 확인하고 테스트할 수 있습니다.
*   `http://localhost:5002/apidocs`

## 📡 원격 접속 가이드 (프론트엔드 연동)

프론트엔드 개발자와 네트워크가 다른 경우(예: 서로 다른 집), **ngrok**을 사용하면 편리합니다.

1.  [ngrok 다운로드](https://ngrok.com/download) 및 설치
2.  터미널에서 실행: `ngrok http 5002`
3.  생성된 URL (예: `https://xxxx.ngrok-free.app`)을 프론트엔드 개발자에게 전달
4.  Swagger 주소: `https://xxxx.ngrok-free.app/apidocs`

## 📱 프론트엔드 연동 가이드 (Android)

### API 엔드포인트

Base URL: `http://<SERVER_IP>:5002`

#### 1. 이벤트 조회
*   `GET /api/events`
*   응답:
    ```json
    [
      {
        "id": 1,
        "title": "송도 맥주 축제",
        "date": "2025.08.25 ~ 2025.09.02",
        "start_date": "2025-08-25",
        "location": "송도달빛축제공원",
        "lat": 37.405,
        "lng": 126.635,
        "image": "http://...",
        "link": "http://...",
        "description": "...",
        "source": "PublicData"
      },
      ...
    ]
    ```

#### 2. 인증
*   **회원가입**: `POST /auth/register` (`username`, `password`, `email`)
*   **로그인**: `POST /auth/login` (`username`, `password`)
*   **구글 로그인**: `POST /auth/google` (`token`: Google ID Token)
*   **로그아웃**: `POST /auth/logout`

#### 3. 북마크
*   **추가**: `POST /api/bookmarks` (`event_id`)
*   **삭제**: `DELETE /api/bookmarks/<event_id>`
*   **조회**: `GET /api/bookmarks`

#### 4. 알림 설정
*   **FCM 토큰 등록**: `POST /api/user/fcm-token`
    *   Body: `{"fcm_token": "your_device_token"}`
    *   앱 실행 시 또는 로그인 시 호출하여 토큰을 서버에 등록해야 알림을 받을 수 있습니다.
*   **키워드 설정**: `POST /api/user/keywords`
    *   Body: `{"keywords": "재즈,마켓,공연"}` (콤마로 구분된 문자열 또는 리스트)
    *   등록된 키워드가 포함된 새 이벤트가 크롤링되면 알림이 발송됩니다.

## 📂 프로젝트 구조

```
backend/
├── app.py                  # Flask 앱 진입점 & 설정
├── models.py               # DB 모델 (User, Event, Bookmark, NotificationHistory)
├── routes.py               # 이벤트 API 라우트
├── auth_routes.py          # 인증 API 라우트
├── bookmark_routes.py      # 북마크 API 라우트
├── user_routes.py          # 사용자 설정 API 라우트 (FCM, 키워드)
├── crawler.py              # 통합 크롤러 (공공데이터, 네이버 블로그)
├── venue_crawler.py        # 장소별 크롤러 (문화재단, 아트센터 등)
├── scheduler.py            # 스케줄러 (매일 크롤링, 매시간 알림 체크)
├── notification_service.py # Firebase 푸시 알림 서비스
├── trigger_crawl.py        # 수동 크롤링 트리거 스크립트
└── requirements.txt        # 의존성 목록
```

## ⚠️ 주의사항
*   `localnow.db` 파일이 삭제되면 모든 데이터가 초기화됩니다.
*   크롤링 대상 사이트의 구조가 변경되면 `venue_crawler.py`의 수정이 필요할 수 있습니다.