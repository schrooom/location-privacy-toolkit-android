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
     * Function to get the corresponding config value
     *
     * @return Value of the LocationPrivacyConfig
     */
    private fun getConfig(): Int? {
        return config.getPrivacyConfig(configKey)
    }

    /**
     * A guard that checks the input parameters. If there is no location or no
     * configuration, it either returns null or the given location
     * Otherwise it returns the result of the processor
     *
     * @param location A location object or null
     * @return A manipulated location, the original location or null
     */
    fun process(location: Location?): Location? {
        // return null if no location is provided
        if (location == null) {
            return null
        }
         // get config or return location if config is null
        val config = this.getConfig() ?: return location

        return this.manipulateLocation(location, config)
    }

    /**
     * The function that manipulates a location
     *
     * @param location A location object that will be manipulated
     * @param config Value of the LocationPrivacyConfig
     * @return A manipulated location
     */
    abstract fun manipulateLocation(location: Location, config: Int): Location
}
