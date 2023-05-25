package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import androidx.fragment.app.Fragment
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface

class AccessProcessor(context: Context) : AbstractLocationProcessor(context) {
    override val configKey = LocationPrivacyConfig.Access
    override val sort = LocationProcessorSort.High
    override val values: Array<Int> = arrayOf(0, 1)
    override val defaultValue: Int = 0
    override val userInterface: LocationPrivacyConfigInterface =
        LocationPrivacyConfigInterface.Switch
    override val fragment: Fragment? = null
    override val titleId: Int = R.string.accessTitle
    override val subtitleId: Int = R.string.accessSubtitle
    override val descriptionId: Int = R.string.accessDescription

    override fun manipulateLocation(location: Location, config: Int): Location? {
        if (config == 1) {
            return location
        }
        return null
    }
}