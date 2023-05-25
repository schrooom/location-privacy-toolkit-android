package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import androidx.fragment.app.Fragment
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface
import de.fh.muenster.locationprivacytoolkit.processors.ui.LocationHistoryFragment

class IntervalProcessor(context: Context): AbstractLocationProcessor(context) {
    override val configKey = LocationPrivacyConfig.Interval
    override val sort = LocationProcessorSort.Medium
    override val values: Array<Int> = arrayOf(1000, 600, 60, 0)
    override val defaultValue: Int = 0
    override val userInterface: LocationPrivacyConfigInterface =
        LocationPrivacyConfigInterface.Slider
    override val fragment: Fragment? = null
    override val titleId: Int = R.string.intervalTitle
    override val subtitleId: Int = R.string.intervalSubtitle
    override val descriptionId: Int = R.string.intervalDescription

    private var localLastLocation: Location? = null
    private var lastLocation: Location?
        get() = localLastLocation ?: locationPrivacyConfig.getLastLocation().also {
            localLastLocation = it
        }
        set(value) {
            localLastLocation = value
            locationPrivacyConfig.setLastLocation(value)
        }

    override fun manipulateLocation(location: Location, config: Int): Location? {
        val cachedLocation = lastLocation
        if (config > 0 && cachedLocation != null && cachedLocation.time > 0) {
            val timeDiffToLastLocation = location.time - cachedLocation.time
            if (timeDiffToLastLocation < config * 1000) {
                return cachedLocation
            }
        }
        lastLocation = location
        return location
    }
}