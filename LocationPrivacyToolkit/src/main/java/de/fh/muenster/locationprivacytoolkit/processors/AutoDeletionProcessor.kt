package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoDeletionProcessor(context: Context, listener: LocationPrivacyToolkitListener?): AbstractLocationProcessor(context, listener) {
    override val configKey = LocationPrivacyConfig.AutoDeletion
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