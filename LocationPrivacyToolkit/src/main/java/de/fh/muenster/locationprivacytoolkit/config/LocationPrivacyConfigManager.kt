package de.fh.muenster.locationprivacytoolkit.config

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.processors.AbstractLocationProcessor
import java.lang.ref.WeakReference

internal class LocationPrivacyConfigManager(context: Context) {

    private val preferences: SharedPreferences
    private val contextReference: WeakReference<Context>

    init {
        contextReference = WeakReference(context)
        preferences =
            context.getSharedPreferences(LOCATION_PRIVACY_PREFERENCES, Context.MODE_PRIVATE)
    }

    fun getPrivacyConfig(key: LocationPrivacyConfig): Int? {
        // TODO: remove workaround - dont specify exlcusionzones here
        if (key == LocationPrivacyConfig.ExclusionZone) return -1
        return if (preferences.contains(key.name)) {
            preferences.getInt(key.name, -1)
        } else {
            null
        }
    }

    fun getPrivacyConfigString(key: LocationPrivacyConfig): String? {
        return if (preferences.contains(key.name)) {
            preferences.getString(key.name, "")
        } else {
            null
        }
    }

    fun setPrivacyConfig(key: LocationPrivacyConfig, value: Int) {
        preferences.edit { putInt(key.name, value) }
    }

    fun setPrivacyConfig(key: LocationPrivacyConfig, value: String) {
        preferences.edit { putString(key.name, value) }
    }

    fun removePrivacyConfig(key: LocationPrivacyConfig) {
        preferences.edit { remove(key.name) }
    }

    fun getLastLocation(): Location? {
        if (preferences.contains(LAST_LOCATION_KEY)) {
            val jsonLocation = preferences.getString(LAST_LOCATION_KEY, null)
            val location: Location? = try {
                Gson().fromJson(jsonLocation, Location::class.java)
            } catch (_: JsonSyntaxException) {
                null
            }
            return location
        }
        return null
    }

    fun setLastLocation(location: Location?) {
        val jsonLocation = Gson().toJson(location)
        preferences.edit { putString(LAST_LOCATION_KEY, jsonLocation) }
    }

    companion object {
        const val LOCATION_PRIVACY_PREFERENCES = "location-privacy-preferences"
        const val LAST_LOCATION_KEY = "last-location"

        fun getLocationProcessors(
            context: Context,
            listener: LocationPrivacyToolkitListener?
        ): List<AbstractLocationProcessor> {
            val processors =
                LocationPrivacyConfig.values()
                    .mapNotNull { c -> c.getLocationProcessor(context, listener) }
            return processors.sortedByDescending { p -> p.sort }
        }
    }
}