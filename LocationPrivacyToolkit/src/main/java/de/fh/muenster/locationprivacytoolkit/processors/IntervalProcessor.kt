package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import de.fh.muenster.locationprivacytoolkit.AbstractLocationProcessor
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyConfigKey

class IntervalProcessor(context: Context): AbstractLocationProcessor(context) {
    override var configKey = LocationPrivacyConfigKey.interval

    override fun process(location: Location): Location {
        // get config or return location if config is null
        val config = this.getConfig() ?: return location

        // TODO: implement interval logic

        return location
    }
}
