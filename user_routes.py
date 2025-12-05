from flask import Blueprint, request, jsonify
from flask_login import login_required, current_user
from models import db

user_bp = Blueprint('user', __name__)

@user_bp.route('/user/fcm-token', methods=['POST'])
@login_required
def update_fcm_token():
    """
    FCM 토큰 등록 (알림용)
    ---
    tags:
      - User
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            fcm_token:
              type: string
    responses:
      200:
        description: 토큰 등록 성공
      400:
        description: 토큰 누락
    """
    data = request.get_json()
    token = data.get('fcm_token')
    
    if not token:
        return jsonify({"error": "Token is required"}), 400
        
    current_user.fcm_token = token
    db.session.commit()
    
    return jsonify({"message": "FCM token updated successfully"})

@user_bp.route('/user/keywords', methods=['POST'])
@login_required
def update_keywords():
    """
    알림 키워드 설정
    ---
    tags:
      - User
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            keywords:
              type: string
              description: "콤마로 구분된 키워드 (예: 재즈,마켓)"
    responses:
      200:
        description: 키워드 설정 성공
    """
    data = request.get_json()
    keywords = data.get('keywords') # Expecting comma-separated string or list
    
    if keywords is None:
        return jsonify({"error": "Keywords are required"}), 400
        
    if isinstance(keywords, list):
        keywords = ",".join(keywords)
        
    current_user.keywords = keywords
    db.session.commit()
    
    return jsonify({"message": "Keywords updated successfully", "keywords": current_user.keywords})

@user_bp.route('/user/keywords', methods=['GET'])
@login_required
def get_keywords():
    """
    내 알림 키워드 조회
    ---
    tags:
      - User
    responses:
      200:
        description: 키워드 목록 반환
    """
    return jsonify({
        "keywords": current_user.keywords.split(",") if current_user.keywords else []
    })
