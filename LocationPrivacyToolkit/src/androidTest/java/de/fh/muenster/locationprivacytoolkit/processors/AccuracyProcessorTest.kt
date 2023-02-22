package de.fh.muenster.locationprivacytoolkit.processors

import android.location.Location
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkit
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AccuracyProcessorTest {
    @Test
    fun manipulateAccuracy() {
        var appContext = InstrumentationRegistry.getInstrumentation().targetContext
        var config = LocationPrivacyConfigManager(appContext)
        config.setPrivacyConfig(LocationPrivacyConfig.Accuracy, 50)

        val lpt = LocationPrivacyToolkit(appContext)


        // TODO: use a proper mock location
        var location = Location("")
        location.setLatitude(52.0)
        location.setLongitude(7.0)
        location.accuracy = 2f

        var accuracyLocation = lpt.processLocation(location)


        Log.d("Origin location\t", location.toString())
        Log.d("New location\t\t", accuracyLocation.toString())

        val distance = accuracyLocation?.distanceTo(location)!!
        val maxDist = config.getPrivacyConfig(LocationPrivacyConfig.Accuracy)!!

        Log.d("Distance\t\t", distance.toString())

        val success = distance <= maxDist

        assertTrue("Distance $distance <= $maxDist", success)
        assertTrue("Distance ${accuracyLocation.accuracy} <= $maxDist", success)

    }
}