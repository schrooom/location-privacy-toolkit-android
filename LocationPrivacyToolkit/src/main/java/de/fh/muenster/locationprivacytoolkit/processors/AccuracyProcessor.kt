package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import android.util.Log
import androidx.fragment.app.Fragment
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface
import gov.nasa.worldwind.geom.LatLon
import gov.nasa.worldwind.geom.LatLon.rhumbEndPosition
import gov.nasa.worldwind.globes.Earth

/**
 * The AccuracyProcessor changes the accuracy of a location.
 *
 * @param context Application context
 */
class AccuracyProcessor(context: Context) : AbstractLocationProcessor(context) {
    override val configKey = LocationPrivacyConfig.Accuracy
    override val sort = LocationProcessorSort.Medium
    override val values: Array<Int> = arrayOf(1000, 500, 100, 0)
    override val defaultValue: Int = 0
    override val userInterface: LocationPrivacyConfigInterface =
        LocationPrivacyConfigInterface.Slider
    override val fragment: Fragment? = null
    override val titleId: Int = R.string.accuracyTitle
    override val subtitleId: Int = R.string.accuracySubtitle
    override val descriptionId: Int = R.string.accuracyDescription

    /**
     * The location will be moved to a random point around the actual
     * point and the `accuracy` metadata will be changed as well
     */
    override fun manipulateLocation(location: Location, config: Int): Location {
        if (location.accuracy >= config) {
            return location
        }

        // TODO: translate config to actual desired accuracy in meters
        val randomDirection = (0..359).random()
        val randomDistance = (0..config).random()

        Log.d("distance", randomDistance.toString())

        val radDistance = randomDistance / Earth.WGS84_EQUATORIAL_RADIUS


        val loc = LatLon.fromDegrees(location.latitude, location.longitude)
        val pos = rhumbEndPosition(loc, Math.toRadians(randomDirection.toDouble()), radDistance)

        val transformedLocation = Location(location)
        transformedLocation.longitude = pos.longitude.degrees
        transformedLocation.latitude = pos.latitude.degrees
        transformedLocation.accuracy = config.toFloat()

        return transformedLocation
    }
}
