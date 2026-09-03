package com.sorobanzen.app.data

import android.content.Context

class AppPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "soroban_zen_preferences",
        Context.MODE_PRIVATE
    )

    var hapticsEnabled: Boolean
        get() = preferences.getBoolean(KEY_HAPTICS, true)
        set(value) = preferences.edit().putBoolean(KEY_HAPTICS, value).apply()


    private companion object {
        const val KEY_HAPTICS = "haptics"
    }
}
