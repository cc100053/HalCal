package com.sorobanzen.app.data

import android.content.Context

class AppPreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "soroban_zen_preferences",
        Context.MODE_PRIVATE
    )

    var rodsCount: Int
        get() = preferences.getInt(KEY_RODS_COUNT, 13).coerceIn(7, 17)
        set(value) = preferences.edit().putInt(KEY_RODS_COUNT, value.coerceIn(7, 17)).apply()

    var soundEffectsEnabled: Boolean
        get() = preferences.getBoolean(KEY_SOUND_EFFECTS, true)
        set(value) = preferences.edit().putBoolean(KEY_SOUND_EFFECTS, value).apply()

    var hapticsEnabled: Boolean
        get() = preferences.getBoolean(KEY_HAPTICS, true)
        set(value) = preferences.edit().putBoolean(KEY_HAPTICS, value).apply()

    var ttsEnabled: Boolean
        get() = preferences.getBoolean(KEY_TTS, true)
        set(value) = preferences.edit().putBoolean(KEY_TTS, value).apply()

    private companion object {
        const val KEY_RODS_COUNT = "rods_count"
        const val KEY_SOUND_EFFECTS = "sound_effects"
        const val KEY_HAPTICS = "haptics"
        const val KEY_TTS = "tts"
    }
}
