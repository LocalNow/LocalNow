package com.example.localnow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class BookmarkManager {
    private static BookmarkManager instance;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "LocalNowBookmarks";
    private static final String KEY_BOOKMARKS = "bookmarked_event_ids";

    private BookmarkManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized BookmarkManager getInstance(Context context) {
        if (instance == null) {
            instance = new BookmarkManager(context);
        }
        return instance;
    }

    public boolean isBookmarked(int eventId) {
        Set<String> bookmarks = prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>());
        return bookmarks.contains(String.valueOf(eventId));
    }

    public void addBookmark(int eventId) {
        Set<String> bookmarks = new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
        bookmarks.add(String.valueOf(eventId));
        prefs.edit().putStringSet(KEY_BOOKMARKS, bookmarks).apply();
    }

    public void removeBookmark(int eventId) {
        Set<String> bookmarks = new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
        bookmarks.remove(String.valueOf(eventId));
        prefs.edit().putStringSet(KEY_BOOKMARKS, bookmarks).apply();
    }

    public void toggleBookmark(int eventId) {
        if (isBookmarked(eventId)) {
            removeBookmark(eventId);
        } else {
            addBookmark(eventId);
        }
    }

    public Set<String> getAllBookmarks() {
        return prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>());
    }
}
