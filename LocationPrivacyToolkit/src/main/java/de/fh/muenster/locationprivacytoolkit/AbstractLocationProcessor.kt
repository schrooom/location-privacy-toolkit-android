package de.fh.muenster.locationprivacytoolkit

import android.content.Context
import android.location.*

/**
 * Abstract class which processes Location data
 */
abstract class AbstractLocationProcessor(context: Context) {
    // must be implemented by subclass
    abstract var configKey: LocationPrivacyConfigKey

    private var config: LocationPrivacyConfig = LocationPrivacyConfig(context)

    fun getConfig(): Int? {
        return config.getPrivacyConfig(configKey)
    }
    abstract fun process(location: Location): Location
}
