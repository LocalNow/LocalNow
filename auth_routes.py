from flask import Blueprint, request, jsonify
from flask_login import login_user, logout_user, login_required, current_user
from models import db, User
from google.oauth2 import id_token
from google.auth.transport import requests as google_requests

auth_bp = Blueprint('auth', __name__)

# [API] 구글 로그인 (Android ID Token 검증)
@auth_bp.route('/google', methods=['POST'])
def google_login():
    """
    구글 로그인 (ID Token 검증)
    ---
    tags:
      - Auth
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            token:
              type: string
              description: "Google ID Token"
    responses:
      200:
        description: "로그인 성공"
      401:
        description: "유효하지 않은 토큰"
    """
    data = request.get_json()
    token = data.get('token')
    
    if not token:
        return jsonify({"message": "Token is required"}), 400
        
    try:
        # 구글 토큰 검증
        # CLIENT_ID는 안드로이드 앱의 클라이언트 ID와 일치해야 함
        # 여기서는 검증만 하고 aud 체크는 생략하거나 나중에 환경변수로 추가
        id_info = id_token.verify_oauth2_token(token, google_requests.Request())
        
        google_id = id_info['sub']
        email = id_info.get('email')
        name = id_info.get('name', email.split('@')[0])
        
        # 사용자 확인
        user = User.query.filter((User.google_id == google_id) | (User.email == email)).first()
        
        if not user:
            # 신규 사용자 생성
            user = User(
                username=name, # 중복 시 처리 필요할 수 있음 (여기선 단순화)
                email=email,
                google_id=google_id
            )
            db.session.add(user)
            db.session.commit()
            
        # 로그인 처리
        login_user(user)
        return jsonify({"message": "Google login successful", "username": user.username}), 200
        
    except ValueError as e:
        return jsonify({"message": f"Invalid token: {str(e)}"}), 401
    except Exception as e:
        return jsonify({"message": f"Login failed: {str(e)}"}), 500

# [API] 회원가입
@auth_bp.route('/register', methods=['POST'])
def register():
    """
    회원가입
    ---
    tags:
      - Auth
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            username:
              type: string
            password:
              type: string
    responses:
      201:
        description: 회원가입 성공
      400:
        description: 사용자 이미 존재함
    """
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if not username or not password:
        return jsonify({"message": "Username and password are required"}), 400

    if User.query.filter_by(username=username).first():
        return jsonify({"message": "User already exists"}), 400

    new_user = User(username=username)
    new_user.set_password(password)
    
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": "User registered successfully"}), 201

# [API] 로그인
@auth_bp.route('/login', methods=['POST'])
def login():
    """
    로그인
    ---
    tags:
      - Auth
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            username:
              type: string
            password:
              type: string
    responses:
      200:
        description: 로그인 성공
      401:
        description: 인증 실패
    """
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    user = User.query.filter_by(username=username).first()

    if user and user.check_password(password):
        login_user(user)
        return jsonify({"message": "Logged in successfully", "username": user.username}), 200
    
    return jsonify({"message": "Invalid credentials"}), 401

# [API] 로그아웃
@auth_bp.route('/logout', methods=['POST'])
@login_required
def logout():
    """
    로그아웃
    ---
    tags:
      - Auth
    responses:
      200:
        description: 로그아웃 성공
    """
    logout_user()
    return jsonify({"message": "Logged out successfully"}), 200

# [API] 내 정보 확인 (세션 체크용)
@auth_bp.route('/me', methods=['GET'])
@login_required
def get_current_user():
    """
    내 정보 확인 (세션 체크)
    ---
    tags:
      - Auth
    responses:
      200:
        description: "현재 로그인된 사용자 정보 반환"
    """
    return jsonify({
        "id": current_user.id,
        "username": current_user.username
    }), 200
