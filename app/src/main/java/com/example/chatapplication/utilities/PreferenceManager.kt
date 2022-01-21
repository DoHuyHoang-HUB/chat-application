package com.example.chatapplication.utilities

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreference: SharedPreferences =
        context.getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val editor = sharedPreference.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreference.getBoolean(key, false);
    }

    fun putString(key: String, value: String) {
        val editor = sharedPreference.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        return sharedPreference.getString(key, null)
    }

    fun clear() {
        val editor = sharedPreference.edit()
        editor.clear()
        editor.apply()
    }

}