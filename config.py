import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    # API Keys
    PUBLIC_DATA_KEY = os.getenv("PUBLIC_DATA_KEY")
    NAVER_CLIENT_ID = os.getenv("NAVER_CLIENT_ID")
    NAVER_CLIENT_SECRET = os.getenv("NAVER_CLIENT_SECRET")
    KAKAO_REST_API_KEY = os.getenv("KAKAO_REST_API_KEY")
    

    # DB Config (SQLite)
    SQLALCHEMY_DATABASE_URI = os.getenv("DATABASE_URL", "sqlite:///링크")
    SQLALCHEMY_TRACK_MODIFICATIONS = False