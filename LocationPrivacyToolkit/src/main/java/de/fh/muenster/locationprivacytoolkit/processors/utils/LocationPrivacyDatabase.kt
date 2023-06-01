package de.fh.muenster.locationprivacytoolkit.processors.utils

import android.location.Location

class LocationPrivacyDatabase {

    // TODO: replace with real database
    private val persistedLocations = mutableListOf<Location>()

    val locations: List<Location>
        get() = persistedLocations

    fun add(location: Location): Boolean {
        return persistedLocations.add(location)
    }

    fun remove(location: Location): Boolean {
        return persistedLocations.remove(location)
    }

    fun remove(locations: List<Location>): Boolean {
        return persistedLocations.removeAll(locations)
    }

    companion object {
        private var instance: LocationPrivacyDatabase? = null
        val sharedInstance: LocationPrivacyDatabase
            get() {
                return instance ?: LocationPrivacyDatabase().also { instance = it }
            }
    }
}