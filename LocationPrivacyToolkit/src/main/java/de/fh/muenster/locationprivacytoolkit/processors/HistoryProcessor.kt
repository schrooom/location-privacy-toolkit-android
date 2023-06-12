package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.Location
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.processors.utils.LocationPrivacyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryProcessor(context: Context, listener: LocationPrivacyToolkitListener?) :
    AbstractLocationProcessor(context, listener) {

    override val configKey = LocationPrivacyConfig.History
    override val sort = LocationProcessorSort.Low

    private val locationDatabase = LocationPrivacyDatabase.sharedInstance(context)

    override fun manipulateLocation(location: Location, config: Int): Location {
        CoroutineScope(Dispatchers.IO).launch {
            locationDatabase.add(location)
        }
        return location
    }
}