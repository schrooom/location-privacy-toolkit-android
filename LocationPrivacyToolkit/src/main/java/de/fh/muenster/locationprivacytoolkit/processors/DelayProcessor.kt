package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig

class DelayProcessor(context: Context) : AbstractLocationProcessor(context) {
    override val configKey = LocationPrivacyConfig.Delay
    override val sort = LocationProcessorSort.Low

    var previousLocations: MutableList<Location> = mutableListOf()

    override fun manipulateLocation(location: Location, config: Int): Location? {
        previousLocations.add(location)
        if (config > 0) {
            val currentTimeSeconds = System.currentTimeMillis() / 1000L
            val delayedLocation = previousLocations.lastOrNull { l ->
                val lTimeSeconds = l.time / 1000L
                (currentTimeSeconds - lTimeSeconds) >= config
            }
            return delayedLocation
        }
        return location
    }
}
