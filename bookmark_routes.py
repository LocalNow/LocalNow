from flask import Blueprint, request, jsonify
from flask_login import login_required, current_user
from models import db, Bookmark, Event

bookmark_bp = Blueprint('bookmark', __name__)

# [API] 북마크 추가
@bookmark_bp.route('/', methods=['POST'])
@login_required
def add_bookmark():
    """
    북마크 추가
    ---
    tags:
      - Bookmark
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          properties:
            event_id:
              type: integer
    responses:
      201:
        description: 북마크 추가 성공
      400:
        description: 이미 북마크됨
      404:
        description: 이벤트 없음
    """
    data = request.get_json()
    event_id = data.get('event_id')

    if not event_id:
        return jsonify({"message": "Event ID is required"}), 400

    # 이벤트 존재 여부 확인
    event = Event.query.get(event_id)
    if not event:
        return jsonify({"message": "Event not found"}), 404

    # 이미 북마크했는지 확인
    existing_bookmark = Bookmark.query.filter_by(user_id=current_user.id, event_id=event_id).first()
    if existing_bookmark:
        return jsonify({"message": "Already bookmarked"}), 400

    new_bookmark = Bookmark(user_id=current_user.id, event_id=event_id)
    db.session.add(new_bookmark)
    db.session.commit()

    return jsonify({"message": "Bookmark added successfully"}), 201

# [API] 북마크 삭제
@bookmark_bp.route('/<int:event_id>', methods=['DELETE'])
@login_required
def remove_bookmark(event_id):
    """
    북마크 삭제
    ---
    tags:
      - Bookmark
    parameters:
      - name: event_id
        in: path
        type: integer
        required: true
    responses:
      200:
        description: 북마크 삭제 성공
      404:
        description: 북마크 없음
    """
    bookmark = Bookmark.query.filter_by(user_id=current_user.id, event_id=event_id).first()

    if not bookmark:
        return jsonify({"message": "Bookmark not found"}), 404

    db.session.delete(bookmark)
    db.session.commit()

    return jsonify({"message": "Bookmark removed successfully"}), 200

# [API] 내 북마크 목록 조회
@bookmark_bp.route('/', methods=['GET'])
@login_required
def get_bookmarks():
    """
    내 북마크 목록 조회
    ---
    tags:
      - Bookmark
    responses:
      200:
        description: 북마크된 이벤트 목록 반환
    """
    # User.bookmarks 관계를 통해 가져오거나 직접 쿼리
    # 여기서는 직접 쿼리하여 Event 정보까지 조인해서 가져옴
    bookmarks = db.session.query(Event).join(Bookmark).filter(Bookmark.user_id == current_user.id).all()
    
    result = []
    for event in bookmarks:
        result.append(event.to_dict())

    return jsonify({
        "status": "success",
        "count": len(result),
        "data": result
    })
