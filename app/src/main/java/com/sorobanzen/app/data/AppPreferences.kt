package com.sorobanzen.app.data

import android.content.Context

class AppPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "soroban_zen_preferences",
        Context.MODE_PRIVATE
    )

    var soundEffectsEnabled: Boolean
        get() = preferences.getBoolean(KEY_SOUND_EFFECTS, true)
        set(value) = preferences.edit().putBoolean(KEY_SOUND_EFFECTS, value).apply()

    var hapticsEnabled: Boolean
        get() = preferences.getBoolean(KEY_HAPTICS, true)
        set(value) = preferences.edit().putBoolean(KEY_HAPTICS, value).apply()


    private companion object {
        const val KEY_SOUND_EFFECTS = "sound_effects"
        const val KEY_HAPTICS = "haptics"
    }
}
