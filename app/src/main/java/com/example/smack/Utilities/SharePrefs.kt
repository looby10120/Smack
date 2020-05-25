package com.example.smack.Utilities

import android.content.Context
import com.android.volley.toolbox.Volley

class SharePrefs(context: Context) {

    companion object {
        private const val PREFS_FILENAMES = "prefs"
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val AUTH_TOKEN = "authToken"
        private const val USER_EMAIL = "userEmail"
    }

    val prefs = context.getSharedPreferences(PREFS_FILENAMES, 0)
//    var isLoggedIn: Boolean = prefs.getBoolean(IS_LOGGED_IN, false)
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(IS_LOGGED_IN, value).apply()

    var authToken: String?
        get() = prefs.getString(AUTH_TOKEN, "")
        set(value) = prefs.edit().putString(AUTH_TOKEN, value).apply()

    var userEmail: String?
        get() = prefs.getString(USER_EMAIL, "")
        set(value) = prefs.edit().putString(USER_EMAIL, value).apply()

    val requestQueue = Volley.newRequestQueue(context)
}
