package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.*
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.geometry.LatLng
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.processors.ui.ExclusionZoneProcessorFragment.Companion.ACTION_EXCLUSION_ZONE_UPDATE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ExclusionZone(val center: LatLng, val radiusMeters: Int)

class ExclusionZoneProcessor(context: Context) : AbstractLocationProcessor(context) {

    init {
        loadExclusionZones()
        ContextCompat.registerReceiver(
            context,
            this,
            IntentFilter(ACTION_EXCLUSION_ZONE_UPDATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

    }

    private var exclusionZones: List<ExclusionZone>? = null

    override val configKey = LocationPrivacyConfig.ExclusionZone
    override val sort = LocationProcessorSort.Low

    override fun manipulateLocation(location: Location, config: Int): Location? {
        val zones = exclusionZones ?: return location

        if (zones.isNotEmpty()) {
            val locationLatLng = LatLng(location)
            val isExcluded = zones.any { z ->
                z.center.distanceTo(locationLatLng) < z.radiusMeters
            }
            if (isExcluded) {
                return null
            }
        }
        return location
    }

    private fun loadExclusionZones() {
        CoroutineScope(Dispatchers.IO).launch {
            locationPrivacyConfig.getPrivacyConfigString(LocationPrivacyConfig.ExclusionZone)
                ?.let { zonesJson ->
                    val zoneListType = object : TypeToken<List<ExclusionZone>>() {}.type
                    exclusionZones = try {
                        Gson().fromJson(zonesJson, zoneListType) as? List<ExclusionZone>
                    } catch (_: JsonSyntaxException) {
                        null
                    }
                }
        }
    }


    // BroadcastReceiver

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_EXCLUSION_ZONE_UPDATE) {
            loadExclusionZones()
        }
    }
}
