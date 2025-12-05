from apscheduler.schedulers.background import BackgroundScheduler
from crawler import DataCrawler
from models import db, Event
from app import app
import atexit
import time

def run_crawling_job():
    print(">> [Scheduler] Daily Crawling Started...")
    try:
        # 1. 크롤링 실행
        crawler = DataCrawler()
        public_data = crawler.fetch_public_festivals()
        blog_data = crawler.fetch_naver_blogs()
        # 3. 인스타그램 (삭제됨)
        
        all_data = public_data + blog_data
        
        print(f">> [Scheduler] Crawling Finished. {len(public_data) + len(blog_data)} items found.")
        # 2. DB 저장
        with app.app_context():
            new_count = 0
            for item in all_data:
                # 중복 체크 (제목과 날짜가 같으면 중복으로 간주)
                exists = Event.query.filter_by(title=item['title'], date=item['date']).first()
                if not exists:
                    new_event = Event(
                        title=item['title'],
                        category=item['category'],
                        date=item['date'],
                        start_date=item.get('start_date'),
                        end_date=item.get('end_date'),
                        location=item['location'],
                        lat=item['lat'],
                        lng=item['lng'],
                        description=item['description'],
                        image=item.get('image', ''),
                        source=item['source'],
                        link=item.get('link', '')
                    )
                    db.session.add(new_event)
                    new_count += 1
            
            db.session.commit()
            print(f">> [Scheduler] Crawling Finished. {len(all_data)} items found, {new_count} new items saved.")
        
    except Exception as e:
        print(f">> [Scheduler] Error during crawling: {e}")

def check_deadline_notifications():
    print(">> [Scheduler] Checking Deadline Notifications...")
    from notification_service import send_push_notification
    from models import Bookmark, NotificationHistory
    from datetime import datetime, timedelta

    with app.app_context():
        now = datetime.now()
        
        # Define thresholds
        thresholds = {
            "3d": timedelta(days=3),
            "1d": timedelta(days=1),
            "12h": timedelta(hours=12),
            "6h": timedelta(hours=6),
            "1h": timedelta(hours=1)
        }
        
        # Get all bookmarks where event has start_date
        bookmarks = db.session.query(Bookmark).join(Event).filter(Event.start_date.isnot(None)).all()
        
        for bookmark in bookmarks:
            event = bookmark.event
            user = bookmark.user
            
            if not user.fcm_token:
                continue
                
            time_diff = event.start_date - now
            
            # Check thresholds
            for label, delta in thresholds.items():
                # Allow 30 min buffer
                if delta - timedelta(minutes=30) <= time_diff <= delta + timedelta(minutes=30):
                    # Check if already sent
                    history = NotificationHistory.query.filter_by(
                        user_id=user.id, 
                        event_id=event.id, 
                        type=f"deadline_{label}"
                    ).first()
                    
                    if not history:
                        # Send notification
                        title = f"Upcoming Event: {event.title}"
                        body = f"Event starts in {label}!"
                        success = send_push_notification(user.fcm_token, title, body)
                        
                        if success:
                            # Log history
                            log = NotificationHistory(
                                user_id=user.id,
                                event_id=event.id,
                                type=f"deadline_{label}"
                            )
                            db.session.add(log)
                            print(f"Sent {label} deadline notification to {user.username}")
        
        db.session.commit()

def start_scheduler():
    scheduler = BackgroundScheduler()
    # 매일 자정(00:00)에 실행
    scheduler.add_job(func=run_crawling_job, trigger="cron", hour=0, minute=0)
    
    # Deadline Notification Check (Every hour)
    scheduler.add_job(func=check_deadline_notifications, trigger="interval", hours=1)
    
    # 테스트용: 서버 시작 10초 후 실행 (디버깅용)
    # scheduler.add_job(func=run_crawling_job, trigger="date", run_date=datetime.now() + timedelta(seconds=10))
    
    scheduler.start()
    print(">> [Scheduler] Background Scheduler Started.")
    
    # Flask 종료 시 스케줄러도 종료
    atexit.register(lambda: scheduler.shutdown())
