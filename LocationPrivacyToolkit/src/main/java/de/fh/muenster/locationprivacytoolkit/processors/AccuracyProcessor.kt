package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import de.fh.muenster.locationprivacytoolkit.AbstractLocationProcessor
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyConfigKey

/**
 * The AccuracyProcessor changes the accuracy of a location.
 *
 * @param context Application context
 */
class AccuracyProcessor(context: Context): AbstractLocationProcessor(context) {
    override var configKey = LocationPrivacyConfigKey.accuracy

    /**
     * The location will be moved to a random point around the actual
     * point and the `accuracy` metadata will be changed as well
     */
    override fun manipulateLocation(location: Location, config: Int): Location {
        // TODO: translate config to actual desired accuracy in meters

        val randomDirection = (0..359).random()
        val randomDistance =  (0..config).random()

        // TODO: transform translate location by random direction and distance

        location.accuracy = config.toFloat()

        return location
    }
}
