
from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime, timedelta

app = Flask(__name__)

# 1. DB 설정
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///localnow.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['JSON_AS_ASCII'] = False
db = SQLAlchemy(app)

# [상수 정의] 허용된 알림 키워드 (카테고리와 일치)

ALLOWED_KEYWORDS = ['MARKET', 'SHOW', 'EXHIBITION', 'FESTIVAL']

# [DB 정의]

# 1. 사용자 (User)
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    login_id = db.Column(db.String(50), unique=True, nullable=False) # 로그인용 ID
    password_hash = db.Column(db.String(200), nullable=False)
    username = db.Column(db.String(50), nullable=False)            # 닉네임
    bookmarks = db.relationship('Bookmark', backref='user', lazy=True)

# 2. 이벤트 (Event)
class Event(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(100), nullable=False)
    location = db.Column(db.String(100))
    address = db.Column(db.String(200))
    start_date = db.Column(db.String(20))
    end_date = db.Column(db.String(20))
    category = db.Column(db.String(20)) # MARKET, SHOW, EXHIBITION, FESTIVAL, ETC
    image_url = db.Column(db.String(300))
    description = db.Column(db.Text)
    
    lat = db.Column(db.Float, nullable=True) 
    lng = db.Column(db.Float, nullable=True)
    
    creator_user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=True)
    status = db.Column(db.String(20), default='APPROVED') 

# 3. 북마크 (Bookmark)
class Bookmark(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    event_id = db.Column(db.Integer, db.ForeignKey('event.id'), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

# 4. 리뷰 (Review)
class Review(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    event_id = db.Column(db.Integer, db.ForeignKey('event.id'), nullable=False)
    rating = db.Column(db.Integer, nullable=False)
    text = db.Column(db.Text)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

# 5. 알림 키워드 (NotificationKeyword)
class NotificationKeyword(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    keyword = db.Column(db.String(50), nullable=False) # MARKET, SHOW 등 고정값만 저장
    created_at = db.Column(db.DateTime, default=datetime.utcnow)

# 6. 알림 수신함 (Notification)
class Notification(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    event_id = db.Column(db.Integer, db.ForeignKey('event.id'), nullable=False)
    message = db.Column(db.String(200))
    is_read = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)



# 카테고리 분류기

def auto_classify_category(title, content):
    combined_text = (str(title) + " " + str(content)).replace(" ", "")
    if any(k in combined_text for k in ['마켓', '플리', '시장', '장터']):
        return "MARKET"
    elif any(k in combined_text for k in ['공연', '콘서트', '버스킹', '라이브']):
        return "SHOW"
    elif any(k in combined_text for k in ['전시', '박람회', '미술']):
        return "EXHIBITION"
    elif any(k in combined_text for k in ['축제', '페스티벌', '행사', '불꽃']):
        return "FESTIVAL"
    else:
        return "ETC"


# [API 엔드포인트]


# 1. 초기화
@app.route('/init')
def init_db():
    with app.app_context():
        db.create_all()
    return "DB 생성 완료"

# 2. 회원가입 (email -> login_id)
@app.route('/api/register', methods=['POST'])
def register():
    data = request.get_json()
    # 중복 ID 체크
    if User.query.filter_by(login_id=data['login_id']).first():
        return jsonify({"message": "이미 존재하는 ID입니다.", "success": False}), 400
    
    hashed_pw = generate_password_hash(data['password'])
    # email 컬럼 대신 login_id 사용
    new_user = User(login_id=data['login_id'], password_hash=hashed_pw, username=data['username'])
    db.session.add(new_user)
    db.session.commit()
    return jsonify({"message": "회원가입 성공", "success": True, "user_id": new_user.id})

# 3. 로그인 (login_id)
@app.route('/api/login', methods=['POST'])
def login():
    data = request.get_json()
    # ID로 유저 찾기
    user = User.query.filter_by(login_id=data['login_id']).first()
    
    if user and check_password_hash(user.password_hash, data['password']):
        return jsonify({
            "message": "로그인 성공", 
            "success": True, 
            "user_id": user.id, 
            "username": user.username
        })
    return jsonify({"message": "아이디 또는 비밀번호가 틀렸습니다.", "success": False}), 401

# 4. 크롤링 데이터 업데이트 + 카테고리 구독 알림 발송
@app.route('/update_data')
def update_db_from_crawler():
    try:
        import crawler
        new_data_list = crawler.crawl_all_events()
        print("실제 크롤러 데이터 사용")
    except:
        new_data_list = [{
            "title": "송도 센트럴파크 불꽃축제", 
            "location": "센트럴파크", 
            "address": "인천 연수구 컨벤시아대로 160",
            "start_date": "2025-12-03", 
            "lat": 37.3935, "lng": 126.6450, 
            "image_url": "img.jpg", "description": "불꽃축제입니다"
        }]

    added_count = 0
    noti_count = 0
    
    for item in new_data_list:
        # 1. 카테고리 자동 분류
        category = auto_classify_category(item['title'], item.get('description', ''))
        
        # 2. 이벤트 저장
        new_event = Event(
            title=item['title'],
            location=item['location'],
            address=item.get('address'),
            start_date=item['start_date'],
            lat=item.get('lat', 0.0),
            lng=item.get('lng', 0.0),
            description=item.get('description', ''),
            image_url=item.get('image_url', ''),
            category=category # 분류된 카테고리 저장
        )
        db.session.add(new_event)
        db.session.flush()

        # 3. 해당 카테고리를 구독한 유저들에게 알림 발송
        # (예: 이 이벤트가 'SHOW'라면, 'SHOW'를 키워드로 등록한 모든 유저 찾기)
        subscribers = NotificationKeyword.query.filter_by(keyword=category).all()
        
        for sub in subscribers:
            # 중복 알림 방지 (같은 이벤트에 대해 이미 알림 갔으면 패스)
            existing = Notification.query.filter_by(user_id=sub.user_id, event_id=new_event.id).first()
            if not existing:
                new_noti = Notification(
                    user_id=sub.user_id,
                    event_id=new_event.id,
                    message=f"관심분야 '{category}' 새 이벤트: {new_event.title}"
                )
                db.session.add(new_noti)
                noti_count += 1
                
        added_count += 1
    
    db.session.commit()
    return f"{added_count}개 이벤트 추가, {noti_count}개 구독 알림 발송 완료!"

# 5. 북마크 임박 알림 (동일)
@app.route('/check_alarms')
def check_upcoming_events():
    today_str = datetime.now().strftime("%Y-%m-%d")
    tomorrow = datetime.now() + timedelta(days=1)
    tomorrow_str = tomorrow.strftime("%Y-%m-%d")
    
    all_bookmarks = Bookmark.query.all()
    count = 0

    for bm in all_bookmarks:
        event = Event.query.get(bm.event_id)
        if event.start_date == tomorrow_str or event.start_date == today_str:
            existing_noti = Notification.query.filter_by(user_id=bm.user_id, event_id=event.id).first()
            if not existing_noti:
                msg = f"[북마크] 곧 행사가 시작됩니다! '{event.title}'"
                new_noti = Notification(
                    user_id=bm.user_id,
                    event_id=event.id,
                    message=msg
                )
                db.session.add(new_noti)
                count += 1
    
    db.session.commit()
    return f"임박 알림 체크 완료! {count}건 발송"

# 6. 이벤트 목록 조회 (동일)
@app.route('/api/events', methods=['GET'])
def get_events():
    events = Event.query.all()
    result = []
    for e in events:
        result.append({
            "id": e.id, "title": e.title, "category": e.category,
            "location": e.location, "date": e.start_date, "image_url": e.image_url,
            "lat": e.lat, "lng": e.lng
        })
    return jsonify({"events": result})

# 7. 북마크 토글 (동일)
@app.route('/api/bookmark', methods=['POST'])
def toggle_bookmark():
    data = request.get_json()
    user_id = data.get('user_id')
    event_id = data.get('event_id')
    
    existing = Bookmark.query.filter_by(user_id=user_id, event_id=event_id).first()
    if existing:
        db.session.delete(existing)
        msg = "북마크 취소"
        status = False
    else:
        new_bm = Bookmark(user_id=user_id, event_id=event_id)
        db.session.add(new_bm)
        msg = "북마크 등록"
        status = True
    db.session.commit()
    return jsonify({"message": msg, "status": status})

# 8. 북마크 목록 조회
@app.route('/api/bookmark/<int:user_id>', methods=['GET'])
def get_my_bookmarks(user_id):
    bookmarks = db.session.query(Event).join(Bookmark).filter(Bookmark.user_id == user_id).all()
    result = []
    for e in bookmarks:
        result.append({
            "id": e.id, "title": e.title, "date": e.start_date, 
            "image_url": e.image_url, "lat": e.lat, "lng": e.lng
        })
    return jsonify({"bookmarks": result})

# 9. 관심 키워드(카테고리) 등록 API (검증 로직 추가)
@app.route('/api/keyword', methods=['POST'])
def add_keyword():
    data = request.get_json()
    requested_keyword = data['keyword'] # 예: 'SHOW', 'MARKET'
    
    # 1. 허용된 키워드인지 검사 (유효성 검사)
    if requested_keyword not in ALLOWED_KEYWORDS:
        return jsonify({
            "message": f"잘못된 카테고리입니다. 허용된 값: {ALLOWED_KEYWORDS}", 
            "success": False
        }), 400

    # 2. 이미 등록했는지 검사
    existing = NotificationKeyword.query.filter_by(user_id=data['user_id'], keyword=requested_keyword).first()
    if existing:
        return jsonify({"message": "이미 구독 중인 카테고리입니다.", "success": False})

    # 3. 저장
    new_kw = NotificationKeyword(user_id=data['user_id'], keyword=requested_keyword)
    db.session.add(new_kw)
    db.session.commit()
    return jsonify({"message": f"'{requested_keyword}' 카테고리 알림을 받습니다.", "success": True})

# 10. 알림 목록 조회
@app.route('/api/notifications/<int:user_id>', methods=['GET'])
def get_notifications(user_id):
    notis = Notification.query.filter_by(user_id=user_id).order_by(Notification.created_at.desc()).all()
    result = []
    for n in notis:
        result.append({
            "id": n.id, "message": n.message, "event_id": n.event_id, 
            "is_read": n.is_read, "date": n.created_at.strftime("%Y-%m-%d %H:%M")
        })
    return jsonify(result)

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)