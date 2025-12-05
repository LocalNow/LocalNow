from scheduler import run_crawling_job
from app import app

if __name__ == "__main__":
    print(">> Manually triggering crawling job...")
    run_crawling_job()
    
    # Trigger Keyword Notifications
    from app import app
    from models import User, NotificationHistory, db
    from notification_service import send_multicast_notification
    from datetime import datetime, timedelta

    print(">> Checking for keyword notifications...")
    with app.app_context():
        # Get new events (created in the last 5 minutes)
        five_mins_ago = datetime.now() - timedelta(minutes=5)
        new_events = Event.query.filter(Event.created_at >= five_mins_ago).all()
        
        if not new_events:
            print(">> No new events found for notifications.")
        else:
            users = User.query.filter(User.fcm_token.isnot(None), User.keywords.isnot(None)).all()
            
            for user in users:
                user_keywords = [k.strip() for k in user.keywords.split(",") if k.strip()]
                if not user_keywords:
                    continue
                
                matched_events = []
                for event in new_events:
                    # Check if any keyword is in title or description
                    for kw in user_keywords:
                        if kw in event.title or (event.description and kw in event.description):
                            matched_events.append(event)
                            break
                
                if matched_events:
                    # Send notification
                    # We can send one per event or a summary. Let's send a summary if multiple.
                    token = user.fcm_token
                    if len(matched_events) == 1:
                        title = f"New Event: {matched_events[0].title}"
                        body = f"Keyword match found! Check it out."
                    else:
                        title = f"New Events Found ({len(matched_events)})"
                        body = f"Events matching your keywords have been added."
                    
                    # Prevent duplicate notifications for same event/user (optional check)
                    # Here we just send.
                    
                    success = send_multicast_notification([token], title, body)
                    if success:
                        print(f"Sent notification to user {user.username}")

    print(">> Job finished.")
