package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import androidx.fragment.app.Fragment
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AutoDeletionProcessor(context: Context, listener: LocationPrivacyToolkitListener?): AbstractLocationProcessor(context, listener) {
    override val configKey = LocationPrivacyConfig.AutoDeletion
    override val sort = LocationProcessorSort.Low
    override val values: Array<Int> = arrayOf(1000, 600, 60, 0)
    override val defaultValue: Int = 0
    override val userInterface: LocationPrivacyConfigInterface =
        LocationPrivacyConfigInterface.Slider
    override val fragment: Fragment? = null
    override val titleId: Int = R.string.autoDeletionTitle
    override val subtitleId: Int = R.string.autoDeletionSubtitle
    override val descriptionId: Int = R.string.autoDeletionDescription

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