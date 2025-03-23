package com.example.project_terrarium_prm392.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Session manager to save and fetch user session details
 */
public class UserSessionManager {
    private static final String PREF_NAME = "TerrariumUserSession";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public UserSessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String token, int userId, String username, String role) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    /**
     * Check login status
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get stored session data
     */
    public String getToken() {
        return pref.getString(KEY_TOKEN, null);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, null);
    }

    /**
     * Clear session details
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Get authorization header
     */
    public String getAuthorizationHeader() {
        return "Bearer " + getToken();
    }
} 