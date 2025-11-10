package com.example.weatherappfinals;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "weather_prefs";
    private static final String KEY_CITY = "user_city";
    private static SharedPrefManager instance;
    private final SharedPreferences prefs;

    private SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setUserCity(String city) {
        prefs.edit().putString(KEY_CITY, city).apply();
    }

    public String getUserCity() {
        return prefs.getString(KEY_CITY, "");
    }
}
