package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DelayProcessor(context: Context) : AbstractLocationProcessor(context) {
    override val configKey = LocationPrivacyConfig.Delay
    override val sort = LocationProcessorSort.Low

    override fun manipulateLocation(location: Location, config: Int): Location {
        if (config > 0) {
            val autoDeletionTime = config * 1000L
            MainScope().launch {
                delay(autoDeletionTime)
                // callback to delete
                listener?.onRemoveLocation(location)
            }
        }
        return location
    }
}
