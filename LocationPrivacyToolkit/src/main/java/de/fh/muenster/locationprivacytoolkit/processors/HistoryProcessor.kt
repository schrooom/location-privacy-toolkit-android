package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.Location
import androidx.fragment.app.Fragment
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface
import de.fh.muenster.locationprivacytoolkit.processors.ui.ExclusionZoneFragment
import de.fh.muenster.locationprivacytoolkit.processors.ui.LocationHistoryFragment

class HistoryProcessor(context: Context, listener: LocationPrivacyToolkitListener?): AbstractLocationProcessor(context, listener) {

    override val configKey = LocationPrivacyConfig.History
    override val sort = LocationProcessorSort.Low
    override val values: Array<Int> = emptyArray()
    override val defaultValue: Int = 0
    override val userInterface: LocationPrivacyConfigInterface =
        LocationPrivacyConfigInterface.Fragment
    override val fragment: Fragment = LocationHistoryFragment()
    override val titleId: Int = R.string.historyTitle
    override val subtitleId: Int = R.string.historySubtitle
    override val descriptionId: Int = R.string.historyDescription

    override fun manipulateLocation(location: Location, config: Int): Location {
        // TODO: write location into database
        return location
    }

    fun removeLocations(locations: List<Location>) {
        // TODO: remove locations
        listener?.onRemoveLocations(locations)
    }
}