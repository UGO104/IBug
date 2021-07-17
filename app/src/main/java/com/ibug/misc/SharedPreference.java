package com.ibug.misc;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import static com.ibug.misc.Utils.Tag;

public class SharedPreference {

    private Context c;

    public SharedPreference(Context c) {
        this.c = c;
    }

    public void add(String key, String value) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void add(String key, boolean value) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void add(String key, long value) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public String get(String key) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        return pref.getString(key, null);
    }

    public long get(String key, long... params) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        return pref.getLong(key, 0L);
    }

    public boolean get(String key, boolean... params) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }

    public Map<String, ?> getAll() {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        return pref.getAll();
    }

    public void delete(String key) {
        SharedPreferences pref = c.getSharedPreferences(Tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(key);
        editor.apply();
    }

}
