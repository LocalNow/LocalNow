from flask_sqlalchemy import SQLAlchemy
from flask_login import UserMixin
from werkzeug.security import generate_password_hash, check_password_hash

db = SQLAlchemy()

class User(UserMixin, db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(150), unique=True, nullable=False)
    email = db.Column(db.String(150), unique=True, nullable=True) # Google Login
    password_hash = db.Column(db.String(200), nullable=True) # Nullable for Google Users
    google_id = db.Column(db.String(200), unique=True, nullable=True) # Google ID

    # Notification fields
    fcm_token = db.Column(db.String(255), nullable=True)
    keywords = db.Column(db.String(500), nullable=True) # Comma-separated keywords

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    # Relationship to Bookmark
    bookmarks = db.relationship('Bookmark', backref='user', lazy=True)

class Bookmark(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    event_id = db.Column(db.Integer, db.ForeignKey('event.id'), nullable=False)
    created_at = db.Column(db.DateTime, default=db.func.current_timestamp())

    # Ensure unique bookmark per user/event
    __table_args__ = (db.UniqueConstraint('user_id', 'event_id', name='unique_user_event_bookmark'),)

class Event(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    title = db.Column(db.String(200), nullable=False)
    category = db.Column(db.String(50))
    date = db.Column(db.String(50)) # YYYYMMDD or Range
    start_date = db.Column(db.DateTime, nullable=True) # For deadline checks
    end_date = db.Column(db.DateTime, nullable=True)   # For deadline checks
    location = db.Column(db.String(200))
    lat = db.Column(db.Float)
    lng = db.Column(db.Float)
    description = db.Column(db.Text)
    image = db.Column(db.String(500))
    source = db.Column(db.String(50)) # PublicData, NaverBlog, Instagram
    link = db.Column(db.String(500))
    created_at = db.Column(db.DateTime, default=db.func.current_timestamp())

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "category": self.category,
            "date": self.date,
            "start_date": self.start_date.strftime("%Y-%m-%d") if self.start_date else None,
            "end_date": self.end_date.strftime("%Y-%m-%d") if self.end_date else None,
            "location": self.location,
            "lat": self.lat,
            "lng": self.lng,
            "description": self.description,
            "image": self.image,
            "source": self.source,
            "link": self.link
        }

class NotificationHistory(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    event_id = db.Column(db.Integer, db.ForeignKey('event.id'), nullable=True) # Nullable if generic
    type = db.Column(db.String(50)) # 'keyword', 'deadline_3d', 'deadline_1d', etc.
    sent_at = db.Column(db.DateTime, default=db.func.current_timestamp())
