package de.fh.muenster.locationprivacytoolkit

import android.content.Context
import android.location.*

/**
 * Abstract class which processes a location
 * It's meant to be implemented by a subclass
 * which defines its logic in the `process` function
 *
 * @param context The application context
 */
abstract class AbstractLocationProcessor(context: Context) {
    // The configuration the subclass is processing. Must be implemented by subclass
    abstract var configKey: LocationPrivacyConfigKey

    // The actual config from the LocationPrivacyConfig
    private var config: LocationPrivacyConfig = LocationPrivacyConfig(context)

    /**
     * Function can be consumed by the subclass to get
     * the corresponding config value
     *
     * @return Value of the LocationPrivacyConfig
     */
    fun getConfig(): Int? {
        return config.getPrivacyConfig(configKey)
    }

    /**
     * The function that processes a location
     *
     * @param location A location object that will be manipulated
     * @return A manipulated location
     */
    abstract fun process(location: Location): Location
}
