import sqlite3

conn = sqlite3.connect('backend/localnow.db')
cursor = conn.cursor()

cursor.execute('SELECT COUNT(*) FROM event')
count = cursor.fetchone()[0]
print(f'✅ Total events in DB: {count}')

if count > 0:
    cursor.execute('SELECT title, location, category FROM event LIMIT 5')
    print('\nSample events:')
    for row in cursor.fetchall():
        print(f'  📍 {row[0]} - {row[1]} ({row[2]})')

conn.close()
