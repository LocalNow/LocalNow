import firebase_admin
from firebase_admin import credentials, messaging
import os
from datetime import datetime

# Initialize Firebase Admin SDK
# Note: You need to place your serviceAccountKey.json in the backend directory
# or set the GOOGLE_APPLICATION_CREDENTIALS environment variable.
cred_path = os.path.join(os.path.dirname(__file__), 'serviceAccountKey.json')

if os.path.exists(cred_path):
    cred = credentials.Certificate(cred_path)
    firebase_admin.initialize_app(cred)
else:
    print("Warning: serviceAccountKey.json not found. Push notifications will not work.")
    # For development without keys, we can mock the initialization or just handle errors gracefully

def send_push_notification(token, title, body, data=None):
    """
    Sends a push notification to a single device.
    """
    if not token:
        return False

    if not firebase_admin._apps:
        print("Firebase not initialized. Skipping notification.")
        return False

    try:
        message = messaging.Message(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            data=data if data else {},
            token=token,
        )
        response = messaging.send(message)
        print('Successfully sent message:', response)
        return True
    except Exception as e:
        print('Error sending message:', e)
        return False

def send_multicast_notification(tokens, title, body, data=None):
    """
    Sends a push notification to multiple devices.
    """
    if not tokens:
        return False

    if not firebase_admin._apps:
        print("Firebase not initialized. Skipping notification.")
        return False

    try:
        message = messaging.MulticastMessage(
            notification=messaging.Notification(
                title=title,
                body=body,
            ),
            data=data if data else {},
            tokens=tokens,
        )
        response = messaging.send_multicast(message)
        print(f'{response.success_count} messages were sent successfully')
        return True
    except Exception as e:
        print('Error sending multicast message:', e)
        return False
