package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import androidx.fragment.app.Fragment
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface

class DelayProcessor(context: Context) : AbstractLocationProcessor(context) {
    override val configKey = LocationPrivacyConfig.Delay
    override val sort = LocationProcessorSort.Low
    override val values: Array<Int> = arrayOf(1000, 300, 60, 10, 0)
    override val defaultValue: Int = 0
    override val userInterface: LocationPrivacyConfigInterface =
        LocationPrivacyConfigInterface.Slider
    override val fragment: Fragment? = null
    override val titleId: Int = R.string.delayTitle
    override val subtitleId: Int = R.string.delaySubtitle
    override val descriptionId: Int = R.string.delayDescription

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
