package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig

class IntervalProcessor(context: Context): AbstractLocationProcessor(context) {
    override val configKey = LocationPrivacyConfig.Interval

    private var lastLocation: Location?
        get() = locationPrivacyConfig.getLastLocation()
        set(value) = locationPrivacyConfig.setLastLocation(value)
    private val currentTime: Long
        get() = System.currentTimeMillis()

    override fun manipulateLocation(location: Location, config: Int): Location {
        val cachedLocation = lastLocation
        if (config > 0 && cachedLocation != null) {
            val timeDiffToLastLocation = currentTime - cachedLocation.time
            if (timeDiffToLastLocation < config) {
                return cachedLocation
            }
        }
        lastLocation = location
        return location
    }
}