from flask import Blueprint, request, jsonify
from flask_login import login_required, current_user
from models import db

user_bp = Blueprint('user', __name__)

@user_bp.route('/user/fcm-token', methods=['POST'])
@login_required
def update_fcm_token():
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
    return jsonify({
        "keywords": current_user.keywords.split(",") if current_user.keywords else []
    })
