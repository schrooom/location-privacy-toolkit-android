package de.fh.muenster.locationprivacytoolkit

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.lang.ref.WeakReference

enum class LocationPrivacyConfigKey {
    access,
    accuracy,
    interval,
    visibility,
    autoDeletion
}

internal class LocationPrivacyConfig(context: Context) {

    private val preferences: SharedPreferences
    private val contextReference: WeakReference<Context>

    init {
        contextReference = WeakReference(context)
        preferences = context.getSharedPreferences(LOCATION_PRIVACY_PREFERENCES, Context.MODE_PRIVATE)
    }

    fun getPrivacyConfig(key: LocationPrivacyConfigKey): Int? {
        return if (preferences.contains(key.name)) {
            preferences.getInt(key.name, -1)
        } else {
            null
        }
    }

    fun setPrivacyConfig(key: LocationPrivacyConfigKey, value: Int) {
        preferences.edit { putInt(key.name, value) }
    }

    fun removePrivacyConfig(key: LocationPrivacyConfigKey) {
        preferences.edit { remove(key.name) }
    }

    companion object {
        val LOCATION_PRIVACY_PREFERENCES = "location-privacy-preferences"
    }
}