package de.fh.muenster.locationprivacytoolkit.config

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.processors.AbstractLocationProcessor
import java.lang.Exception
import java.lang.ref.WeakReference

internal class LocationPrivacyConfigManager(context: Context) {

    private val preferences =
        context.getSharedPreferences(LOCATION_PRIVACY_PREFERENCES, Context.MODE_PRIVATE)

    fun getPrivacyConfig(key: LocationPrivacyConfig): Int? {
        return if (preferences.contains(key.name)) {
            try {
                preferences.getInt(key.name, -1)
            } catch (e: Exception) {
                null
            }
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
        preferences.edit(commit = true) { putInt(key.name, value) }
    }

    fun setPrivacyConfig(key: LocationPrivacyConfig, value: String) {
        preferences.edit(commit = true) { putString(key.name, value) }
    }

    fun removePrivacyConfig(key: LocationPrivacyConfig) {
        preferences.edit(commit = true) { remove(key.name) }
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
        preferences.edit(commit = true) { putString(LAST_LOCATION_KEY, jsonLocation) }
    }

    fun setUseExampleData(useExampleData: Boolean) {
        preferences.edit(commit = true) { putBoolean(USE_EXAMPLE_DATA_KEY, useExampleData) }
    }

    fun getUseExampleData(): Boolean {
        return preferences.getBoolean(USE_EXAMPLE_DATA_KEY, false)
    }

    companion object {
        const val LOCATION_PRIVACY_PREFERENCES = "location-privacy-preferences"
        const val LAST_LOCATION_KEY = "last-location"
        const val USE_EXAMPLE_DATA_KEY = "use-example-data"

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