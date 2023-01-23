package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import de.fh.muenster.locationprivacytoolkit.AbstractLocationProcessor
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyConfigKey

class IntervalProcessor(context: Context): AbstractLocationProcessor(context) {
    override var configKey = LocationPrivacyConfigKey.interval

    override fun manipulateLocation(location: Location, config: Int): Location {
        // TODO: implement interval logic

        return location
    }
}
