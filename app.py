from flask import Flask
from flask_cors import CORS
from flasgger import Swagger
import os


app = Flask(__name__)
swagger = Swagger(app)

@app.route('/')
def index():
    return "LocalNow Backend is Running!"

# 1. CORS 설정
# 안드로이드 앱(클라이언트)이나 팀원들이 외부에서 접속할 수 있게 허용합니다.
CORS(app)

# 2. 데이터베이스 기본 설정 (SQLite)
# PostgreSQL로 바꾸고 싶으면 여기 주소만 바꾸기.
basedir = os.path.abspath(os.path.dirname(__file__))
db_path = os.path.join(basedir, 'localnow.db')
app.config['SQLALCHEMY_DATABASE_URI'] = f'sqlite:///{db_path}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# 3. 기능 연결 (DB 및 API 라우트)
# models.py와 routes.py가 있어야 서버가 켜집니다.
try:
    from models import db, User
    from routes import api_bp
    from auth_routes import auth_bp
    from bookmark_routes import bookmark_bp
    from user_routes import user_bp
    from flask_login import LoginManager

    # DB 초기화
    db.init_app(app)
    with app.app_context():
        db.create_all()
    
    # LoginManager 설정
    app.secret_key = 'super-secret-key-change-this' # 실제 배포 시 환경변수로 변경 필요
    login_manager = LoginManager()
    login_manager.init_app(app)
    login_manager.login_view = 'auth.login'

    @login_manager.user_loader
    def load_user(user_id):
        return User.query.get(int(user_id))
    
    # API 주소 등록 (/api 로 시작하는 주소는 다 routes.py로 보냄)
    app.register_blueprint(api_bp, url_prefix='/api')
    app.register_blueprint(auth_bp, url_prefix='/auth')
    app.register_blueprint(bookmark_bp, url_prefix='/api/bookmarks')
    app.register_blueprint(user_bp, url_prefix='/api')
    print(">> [System] Database & Routes linked successfully.")

except ImportError as e:
    print(f">>파일(models.py, routes.py)이 없거나 에러가 있습니다: {e}")
    print(">> 서버는 켜지지만 API 기능이 동작하지 않을 수 있습니다.")


# 4. 서버 실행
if __name__ == '__main__':
    from scheduler import start_scheduler
    start_scheduler()
    
    print(">> [LocalNow] Server is Starting on Port 5003...")
    # 외부부
    app.run(host='0.0.0.0', port=5003, debug=False)