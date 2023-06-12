package de.fh.muenster.locationprivacytoolkit.processors.utils

import android.content.Context
import android.location.Location
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RoomLocation::class], version = 1)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDAO
}

class LocationPrivacyDatabase private constructor(context: Context) {

    private val database = Room.databaseBuilder(
        context,
        LocationDatabase::class.java, LOCATION_DATABASE_NAME
    ).build()

    fun loadLocations(): List<Location> {
        return database.locationDao().getAll().map { rl -> rl.location }
    }

    fun add(location: Location) {
        database.locationDao().insert(RoomLocation(location))
    }

    fun add(locations: List<Location>) {
        database.locationDao().insertAll(*locations.map { l -> RoomLocation(l) }.toTypedArray())
    }

    fun remove(location: Location) {
        database.locationDao().delete(RoomLocation(location))
    }

    fun remove(locations: List<Location>) {
        database.locationDao().deleteAll(*locations.map { l -> RoomLocation(l) }.toTypedArray())
    }

    fun removeAll() {
        // TODO: remove all
    }

    companion object {
        const val LOCATION_DATABASE_NAME = "location-storage"
        private var instance: LocationPrivacyDatabase? = null
        fun sharedInstance(context: Context): LocationPrivacyDatabase {
            return instance ?: LocationPrivacyDatabase(context).also { instance = it }
        }
    }
}