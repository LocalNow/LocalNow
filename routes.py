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
    today = datetime.now()
    
    # Filter events whose end_date is today or in the future
    events = Event.query.filter(
        (Event.end_date >= today) | (Event.end_date == None)
    ).order_by(Event.start_date).all()
    
    result = [event.to_dict() for event in events]
    
    return jsonify({
        "status": "success",
        "count": len(result),
        "data": result
    })

# [API 3] 크롤링 테스트 (실시간 + DB 저장)
# 주소: http://localhost:5000/api/crawl/test
# 설명: 공공데이터와 네이버 블로그 데이터를 실시간으로 긁어와서 DB에 저장합니다.
@api_bp.route('/crawl/test', methods=['GET'])
def test_crawling():
    try:
        # 크롤러 객체 생성
        crawler = DataCrawler()
        
        # 1. 공공데이터(축제) 수집
        print(">> [Request] 공공데이터 수집 요청 시작...")
        public_data = crawler.fetch_public_festivals()
        
        # 2. 네이버 블로그(플리마켓 등) 수집
        print(">> [Request] 네이버 블로그 수집 요청 시작...")
        blog_data = crawler.fetch_naver_blogs() 
        
        all_data = public_data + blog_data
        
        # 3. DB에 저장
        new_count = 0
        for item in all_data:
            # 중복 체크 (제목과 날짜가 같으면 중복으로 간주)
            exists = Event.query.filter_by(title=item['title'], date=item.get('date')).first()
            if not exists:
                new_event = Event(
                    title=item['title'],
                    category=item.get('category'),
                    date=item.get('date'),
                    start_date=item.get('start_date'),
                    end_date=item.get('end_date'),
                    location=item.get('location'),
                    lat=item.get('lat', 0),
                    lng=item.get('lng', 0),
                    description=item.get('description', ''),
                    image=item.get('image', ''),
                    source=item.get('source', ''),
                    link=item.get('link', '')
                )
                db.session.add(new_event)
                new_count += 1
        
        db.session.commit()
        print(f">> [Request] 크롤링 완료. {len(all_data)}개 중 {new_count}개 신규 저장.")
        
        # 결과 반환
        result = {
            "status": "success",
            "total_count": len(all_data),
            "new_saved": new_count,
            "data": {
                "public_festivals": public_data,
                "naver_blogs": blog_data,
            }
        }
        return jsonify(result)

    except Exception as e:
        db.session.rollback()
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500