from flask import Blueprint, jsonify
from crawler import DataCrawler
from models import db, Event

# 블루프린트 생성 (이름: api, 접두어: /api는 app.py에서 설정함)
api_bp = Blueprint('api', __name__)

# [API 1] 서버 상태 확인용
# 주소: http://localhost:5000/api/
@api_bp.route('/', methods=['GET'])
def health_check():
    """
    서버 상태 확인
    ---
    responses:
      200:
        description: 서버가 정상 작동 중입니다.
    """
    return jsonify({
        "status": "active",
        "message": "LocalNow API Server is running."
    })

# [API 2] 저장된 이벤트 조회 (DB)
# 주소: http://localhost:5000/api/events
@api_bp.route('/events', methods=['GET'])
def get_events():
    """
    저장된 이벤트 목록 조회
    ---
    responses:
      200:
        description: DB에 저장된 이벤트 목록을 반환합니다.
    """
    from datetime import datetime, timedelta
    yesterday = (datetime.now() - timedelta(days=1)).strftime("%Y%m%d")
    
    events = Event.query.filter(Event.date >= yesterday).order_by(Event.date).all()
    result = []
    for event in events:
        result.append({
            "id": event.id,
            "title": event.title,
            "category": event.category,
            "date": event.date,
            "location": event.location,
            "lat": event.lat,
            "lng": event.lng,
            "description": event.description,
            "image": event.image,
            "source": event.source,
            "link": event.link
        })
    return jsonify({
        "status": "success",
        "count": len(result),
        "data": result
    })

# [API 3] 크롤링 테스트 (실시간 - 디버깅용)
# 주소: http://localhost:5000/api/crawl/test
# 설명: 공공데이터와 네이버 블로그 데이터를 실시간으로 긁어와서 보여줍니다.
@api_bp.route('/crawl/test', methods=['GET'])
def test_crawling():
    try:
        # 크롤러 객체 생성
        crawler = DataCrawler()
        
        # 1. 공공데이터(축제) 수집
        print(">> [Request] 공공데이터 수집 요청 시작...")
        public_data = crawler.fetch_public_festivals()
        
        # 2. 네이버 블로그(플리마켓 등) 수집
        # 별도 키워드 없이 호출하면 내부 추천 키워드 리스트 전체 검색
        print(">> [Request] 네이버 블로그 수집 요청 시작...")
        blog_data = crawler.fetch_naver_blogs() 
        
        # 결과 합치기
        result = {
            "status": "success",
            "total_count": len(public_data) + len(blog_data),
            "data": {
                "public_festivals": public_data, # 공식 행사
                "naver_blogs": blog_data,        # 비공식 행사 (플리마켓 등)
            }
        }
        return jsonify(result)

    except Exception as e:
        # 에러 발생 시 처리
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500