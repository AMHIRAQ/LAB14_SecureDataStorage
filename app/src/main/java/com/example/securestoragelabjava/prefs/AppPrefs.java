package com.example.securestoragelabjava.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPrefs {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_NAME   = "pref_name";
    private static final String KEY_LANG   = "pref_lang";
    private static final String KEY_THEME  = "pref_theme";

    private AppPrefs() {}

    public static boolean save(Context context, String name, String lang, String theme, boolean sync) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit()
                .putString(KEY_NAME, name)
                .putString(KEY_LANG, lang)
                .putString(KEY_THEME, theme);

        if (sync) {
            return editor.commit();
        } else {
            editor.apply();
            return true;
        }
    }

    public static Triple load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String name  = prefs.getString(KEY_NAME,  "");
        String lang  = prefs.getString(KEY_LANG,  "fr");
        String theme = prefs.getString(KEY_THEME, "system");
        return new Triple(name, lang, theme);
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    public static final class Triple {
        public final String name;
        public final String lang;
        public final String theme;

        public Triple(String name, String lang, String theme) {
            this.name  = name;
            this.lang  = lang;
            this.theme = theme;
        }
    }
}