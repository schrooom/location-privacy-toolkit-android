package de.fh.muenster.locationprivacytoolkit.processors

import android.content.Context
import android.location.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.mapbox.mapboxsdk.geometry.LatLng
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface
import de.fh.muenster.locationprivacytoolkit.processors.ui.ExclusionZoneFragment

data class ExclusionZone(val center: LatLng, val radiusMeters: Int)

class ExclusionZoneProcessor(context: Context) : AbstractLocationProcessor(context) {

    init {
        loadExclusionZones()
    }

    private var exclusionZones: List<ExclusionZone>? = null

    override val configKey = LocationPrivacyConfig.ExclusionZone
    override val sort = LocationProcessorSort.Low
    override val values: Array<Int> = emptyArray()
    override val defaultValue: Int = 0
    override val userInterface: LocationPrivacyConfigInterface =
        LocationPrivacyConfigInterface.Fragment
    override val fragment: Fragment = ExclusionZoneFragment()
    override val titleId: Int = R.string.exclusionZoneTitle
    override val subtitleId: Int = R.string.exclusionZoneSubtitle
    override val descriptionId: Int = R.string.exclusionZoneDescription

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

    private fun loadExclusionZones(): List<ExclusionZone>? {
        locationPrivacyConfig.getPrivacyConfigString(LocationPrivacyConfig.ExclusionZone)
            ?.let { zonesJson ->
                val zoneListType = object : TypeToken<List<ExclusionZone>>() {}.type
                exclusionZones = try {
                    Gson().fromJson(zonesJson, zoneListType) as? List<ExclusionZone>
                } catch (_: JsonSyntaxException) {
                    null
                }
            }
        return null
    }
}
