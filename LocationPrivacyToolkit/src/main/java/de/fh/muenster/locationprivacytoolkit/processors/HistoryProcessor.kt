package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.Location
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.processors.utils.LocationPrivacyDatabase

class HistoryProcessor(context: Context, listener: LocationPrivacyToolkitListener?) :
    AbstractLocationProcessor(context, listener) {

    override val configKey = LocationPrivacyConfig.History
    override val sort = LocationProcessorSort.Low

    private val locationDatabase = LocationPrivacyDatabase.sharedInstance

    override fun manipulateLocation(location: Location, config: Int): Location {
        locationDatabase.add(location)
        return location
    }
}