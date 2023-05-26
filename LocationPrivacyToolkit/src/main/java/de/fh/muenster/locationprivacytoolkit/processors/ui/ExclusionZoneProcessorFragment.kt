package de.fh.muenster.locationprivacytoolkit.processors.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.MultiPolygon
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfTransformation
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.databinding.FragmentExclusionZoneBinding
import de.fh.muenster.locationprivacytoolkit.processors.ExclusionZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ExclusionZoneProcessorFragment : Fragment() {

    private lateinit var binding: FragmentExclusionZoneBinding
    private var locationPrivacyConfig: LocationPrivacyConfigManager? = null
    private var isInitialView = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        context?.let {
            Mapbox.getInstance(it)
            locationPrivacyConfig = LocationPrivacyConfigManager(it)
        }

        binding = FragmentExclusionZoneBinding.inflate(inflater, container, false)

        binding.mapView.getMapAsync { map ->
            map.setStyle(TILE_SERVER)
            val initialLatLng = LatLng(
                INITIAL_LATITUDE, INITIAL_LONGITUDE
            )
            val camera = CameraUpdateFactory.newLatLngZoom(
                initialLatLng, INITIAL_ZOOM
            )
            map.easeCamera(camera)
        }

        binding.addZoneButton.setOnClickListener {
            createZone()
        }

        binding.removeZonesButton.setOnClickListener {
            removeAllZones()
        }

        reloadExclusionZones()

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    private fun createZone() {
        binding.mapView.getMapAsync { map ->
            map.cameraPosition.target?.let { center ->
                val zone = ExclusionZone(center, 500)
                addExclusionZone(zone)
            }
        }
    }

    private fun removeAllZones() {
        CoroutineScope(Dispatchers.IO).launch {
            val currentZones = loadExclusionZones()
            locationPrivacyConfig?.setPrivacyConfig(LocationPrivacyConfig.ExclusionZone, "")
            withContext(Dispatchers.Main) {
                binding.mapView.getMapAsync { map ->
                    map.getStyle { style ->
                        // remove zones
                        style.removeLayer(ZONE_LAYER)
                        style.removeSource(ZONE_SOURCE)
                    }
                }
                showDeletionToast(currentZones)
            }
        }
        binding.removeZonesButton.visibility = View.GONE
    }

    private fun addZonesToMap(zones: List<ExclusionZone>) {
        binding.mapView.getMapAsync { map ->
            map.getStyle { style ->
                // remove old zones
                style.removeLayer(ZONE_LAYER)
                style.removeSource(ZONE_SOURCE)

                // add new zones
                val zonePolygons = zones.map { z ->
                    TurfTransformation.circle(
                        Point.fromLngLat(z.center.longitude, z.center.latitude),
                        z.radiusMeters.toDouble(),
                        TurfConstants.UNIT_METERS
                    )
                }
                val zonePolygonsGeometry = MultiPolygon.fromPolygons(zonePolygons)
                style.addSource(GeoJsonSource(ZONE_SOURCE).apply {
                    setGeoJson(zonePolygonsGeometry)
                })
                val zoneLayer = FillLayer(ZONE_LAYER, ZONE_SOURCE).withProperties(
                    PropertyFactory.fillColor(Color.RED),
                    PropertyFactory.fillOpacity(0.5f),
                    PropertyFactory.fillOutlineColor(Color.RED),
                )
                style.addLayerBelow(zoneLayer, ZONE_LAYER)

                if (isInitialView) {
                    isInitialView = false
                    val padding = intArrayOf(
                        INITIAL_PADDING,
                        INITIAL_PADDING,
                        INITIAL_PADDING,
                        INITIAL_PADDING
                    )
                    map.getCameraForGeometry(zonePolygonsGeometry, padding)?.let { cameraPosition ->
                        map.cameraPosition = cameraPosition
                    }
                }
            }
        }
    }

    private fun reloadExclusionZones() {
        val zones = loadExclusionZones()
        zones?.let { addZonesToMap(it) } ?: run {
            isInitialView = false
        }
        binding.removeZonesButton.visibility =
            if (zones?.isEmpty() != false) View.GONE else View.VISIBLE
    }

    private fun addExclusionZone(zone: ExclusionZone) {
        CoroutineScope(Dispatchers.IO).launch {
            val zones = loadExclusionZones()?.toMutableList() ?: mutableListOf()
            zones.add(zone)
            val zonesJson = Gson().toJson(zones)
            locationPrivacyConfig?.setPrivacyConfig(LocationPrivacyConfig.ExclusionZone, zonesJson)
            withContext(Dispatchers.Main) {
                reloadExclusionZones()
            }
        }
    }

    private fun loadExclusionZones(): List<ExclusionZone>? {
        locationPrivacyConfig?.getPrivacyConfigString(LocationPrivacyConfig.ExclusionZone)
            ?.let { zonesJson ->
                val zoneListType = object : TypeToken<List<ExclusionZone>>() {}.type
                return try {
                    Gson().fromJson(zonesJson, zoneListType) as? List<ExclusionZone>
                } catch (_: JsonSyntaxException) {
                    null
                }
            }
        return null
    }

    private fun showDeletionToast(deletedZones: List<ExclusionZone>?) {
        val snackbar =
            Snackbar.make(binding.root, R.string.exclusionZonesDeletedMessage, Snackbar.LENGTH_LONG)
        snackbar.setAction(
            R.string.exclusionZonesDeleteUndo
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val zonesJson = Gson().toJson(deletedZones)
                locationPrivacyConfig?.setPrivacyConfig(
                    LocationPrivacyConfig.ExclusionZone,
                    zonesJson
                )
                withContext(Dispatchers.Main) {
                    reloadExclusionZones()
                }
            }
        }
        snackbar.show()
    }

    companion object {

        // roughly Münster Westf.
        private const val INITIAL_LATITUDE = 51.961563
        private const val INITIAL_LONGITUDE = 7.628202
        private const val INITIAL_ZOOM = 3.0
        private const val INITIAL_PADDING = 300

        private const val NEW_ZONE_COLOR = Color.RED
        private const val CREATED_ZONE_COLOR = Color.GRAY
        private const val ZONE_LAYER = "exclusion_zone_layer"
        private const val ZONE_SOURCE = "exclusion_zone_source"

        // replace with proper style, if available
        private const val TILE_SERVER = "https://demotiles.maplibre.org/style.json"
    }
}