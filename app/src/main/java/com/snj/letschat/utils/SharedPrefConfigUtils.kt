package com.snj.letschat.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefConfigUtils {
    companion object {
        val MY_PREFERENCES = "MyPrefs"
        val USER_ID = "uIdKey"
        val USER_NAME = "uNameKey"
        val USER_EMAIL = "uEmailKey"
        val USER_IMAGE = "uImageKey"

        fun getUserId(context: Context): Long {
            return context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE).getLong(USER_ID, 1)
        }

        fun getUserEmailId(context: Context): String {
            return context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE).getString(USER_EMAIL, "")
        }

        fun getUserName(context: Context): String {
            return context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE).getString(USER_NAME, "")
        }

        fun getSharedPreference(context: Context): SharedPreferences {
            return context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        }
        fun clear(context: Context) {
            var editor = getSharedPreference(context).edit()
            editor.clear()
            editor.commit()
        }
    }
}