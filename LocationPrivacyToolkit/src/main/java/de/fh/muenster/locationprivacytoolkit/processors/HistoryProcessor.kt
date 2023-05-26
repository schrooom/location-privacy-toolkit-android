package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.Location
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig

class HistoryProcessor(context: Context, listener: LocationPrivacyToolkitListener?): AbstractLocationProcessor(context, listener) {

    override val configKey = LocationPrivacyConfig.History
    override val sort = LocationProcessorSort.Low

    override fun manipulateLocation(location: Location, config: Int): Location {
        // TODO: write location into database
        return location
    }

    fun removeLocations(locations: List<Location>) {
        // TODO: remove locations
        listener?.onRemoveLocations(locations)
    }
}