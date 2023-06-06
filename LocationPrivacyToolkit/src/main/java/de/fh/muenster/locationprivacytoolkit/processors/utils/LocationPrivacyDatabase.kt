package de.fh.muenster.locationprivacytoolkit.processors.utils

import android.content.Context
import android.location.Location
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationPrivacyDatabase(context: Context?) {

    // TODO: placeholder storage, replace with real database
    private val preferences = context?.getSharedPreferences(
        LOCATION_STORAGE_PREFERENCES, Context.MODE_PRIVATE
    )

    fun loadLocations(): List<Location> {
        val json = preferences?.getString(LOCATIONS_KEY, null)
        val locationListToken = object : TypeToken<List<Location>>() {}.type
        return try {
            val l = Gson().fromJson<List<Location>>(json, locationListToken) ?: emptyList()
            print("DEBUG: got $json")
            l
        } catch (_: JsonSyntaxException) {
            emptyList()
        }
    }

    fun add(location: Location): Boolean {
        val persistedLocations = loadLocations().toMutableList()
        val success = persistedLocations.add(location)
        if (success) {
            persistLocations(persistedLocations)
        }
        return success
    }

    fun add(locations: List<Location>): Boolean {
        val persistedLocations = loadLocations().toMutableList()
        val success = persistedLocations.addAll(locations)
        if (success) {
            persistLocations(persistedLocations)
        }
        return success
    }

    fun remove(location: Location): Boolean {
        val persistedLocations = loadLocations().toMutableList()
        val success = persistedLocations.remove(location)
        if (success) {
            persistLocations(persistedLocations)
        }
        return success
    }

    fun remove(locations: List<Location>): Boolean {
        val persistedLocations = loadLocations().toMutableList()
        val success = persistedLocations.removeAll(locations)
        if (success) {
            persistLocations(persistedLocations)
        }
        return success
    }

    fun removeAll() {
        preferences?.edit { remove(LOCATIONS_KEY) }
    }

    private fun persistLocations(locations: List<Location>) {
        CoroutineScope(Dispatchers.IO).launch {
            val json = Gson().toJson(locations)
            preferences?.edit {
                putString(LOCATIONS_KEY, json)
            }
        }
    }

    companion object {
        const val LOCATION_STORAGE_PREFERENCES = "location-storage-preferences"
        const val LOCATIONS_KEY = "location-storage-locations"
    }
}