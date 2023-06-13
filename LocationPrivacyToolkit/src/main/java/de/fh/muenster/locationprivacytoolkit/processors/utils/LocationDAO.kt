package de.fh.muenster.locationprivacytoolkit.processors.utils

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDAO {
    @Query("SELECT * FROM roomLocation WHERE isExample = :isExample")
    fun getAll(isExample: Boolean = false): List<RoomLocation>

    @Insert
    fun insert(location: RoomLocation)

    @Insert
    fun insertAll(vararg locations: RoomLocation)

    @Delete
    fun delete(location: RoomLocation)

    @Delete
    fun deleteAll(vararg locations: RoomLocation)

    @Query("DELETE FROM roomLocation WHERE isExample = true")
    fun deleteExampleLocations()
}