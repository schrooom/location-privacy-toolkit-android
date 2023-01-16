package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import de.fh.muenster.locationprivacytoolkit.AbstractLocationProcessor
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyConfigKey

class AccuracyProcessor(context: Context): AbstractLocationProcessor(context) {
    override var configKey = LocationPrivacyConfigKey.accuracy

    override fun process(location: Location): Location {
        // get config or return location if config is null
        val config = this.getConfig() ?: return location

        // TODO: translate config to actual desired accuracy in meters

        val randomDirection = (0..359).random()
        val randomDistance =  (0..config).random()

        // TODO: transform translate location by random direction and distance

        location.accuracy = config.toFloat()

        return location
    }
}