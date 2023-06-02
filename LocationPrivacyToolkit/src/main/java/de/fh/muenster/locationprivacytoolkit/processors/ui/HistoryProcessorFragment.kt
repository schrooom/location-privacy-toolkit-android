package de.fh.muenster.locationprivacytoolkit.processors.ui

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.geojson.MultiPoint
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.databinding.FragmentLocationHistoryBinding
import de.fh.muenster.locationprivacytoolkit.processors.utils.LocationPrivacyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class HistoryMapMode {
    Points,
    Heatmap
}

class HistoryProcessorFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private var locationPrivacyConfig: LocationPrivacyConfigManager? = null
    private val locationDatabase = LocationPrivacyDatabase.sharedInstance
    private var lastLocations: List<Location>? = null
    private var isLayersFabExtended = false
    private var mapMode = HistoryMapMode.Points

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        context?.let {
            Mapbox.getInstance(it)
            locationPrivacyConfig = LocationPrivacyConfigManager(it)
        }

        binding = FragmentLocationHistoryBinding.inflate(inflater, container, false)
        binding.mapView.getMapAsync { map ->
            map.setStyle(TILE_SERVER)
            loadLocations()
        }

        binding.timeLineLayerFab.visibility = View.GONE
        binding.heatmapLayerFab.visibility = View.GONE
        binding.layersFab.setOnClickListener {
            if (isLayersFabExtended) {
                isLayersFabExtended = false
                binding.timeLineLayerFab.hide()
                binding.heatmapLayerFab.hide()
                binding.layersFab.shrink()
            } else {
                isLayersFabExtended = true
                binding.timeLineLayerFab.show()
                binding.heatmapLayerFab.show()
                binding.layersFab.extend()
            }
        }

        binding.timeLineLayerFab.setOnClickListener {
            val locations = lastLocations ?: return@setOnClickListener
            if (mapMode != HistoryMapMode.Points) {
                mapMode = HistoryMapMode.Points
                addLocationsToMap(locations)
            }
        }

        binding.heatmapLayerFab.setOnClickListener {
            val locations = lastLocations ?: return@setOnClickListener
            if (mapMode != HistoryMapMode.Heatmap) {
                mapMode = HistoryMapMode.Heatmap
                addLocationsToMap(locations)
            }
        }

        return binding.root
    }

    private fun loadLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            lastLocations = locationDatabase.locations
            withContext(Dispatchers.Main) {
                lastLocations?.let { locations ->
                    if (locations.isNotEmpty()) {
                        addLocationsToMap(locations)
                        return@withContext
                    }
                }
                // fallback: default initial zoom
                binding.mapView.getMapAsync { map ->
                    val initialLatLng = LatLng(
                        INITIAL_LATITUDE, INITIAL_LONGITUDE
                    )
                    val camera = CameraUpdateFactory.newLatLngZoom(
                        initialLatLng, INITIAL_ZOOM
                    )
                    map.easeCamera(camera)
                }
            }
        }
    }

    private fun addLocationsToMap(locations: List<Location>) {
        binding.mapView.getMapAsync { map ->
            map.style?.let { style ->
                // add locations  layer
                val locationPoints = locations.map { l ->
                    Point.fromLngLat(
                        l.longitude, l.latitude
                    )
                }
                val locationMultiPoint = MultiPoint.fromLngLats(locationPoints)
                style.getSourceAs<GeoJsonSource>(LOCATIONS_SOURCE)?.setGeoJson(locationMultiPoint)
                    ?: run {
                        style.addSource(GeoJsonSource(LOCATIONS_SOURCE).apply {
                            setGeoJson(locationMultiPoint)
                        })
                    }
                style.removeLayer(LOCATIONS_LAYER)
                val locationsLayer = createLocationsLayer()
                style.addLayer(locationsLayer)
                val bounds = LatLngBounds.fromLatLngs(locations.map { l -> LatLng(l) })
                val update = CameraUpdateFactory.newLatLngBounds(bounds, LOCATIONS_PADDING)
                map.easeCamera(update)
            }
        }
    }

    private fun createLocationsLayer(): Layer {
        return when (mapMode) {
            HistoryMapMode.Points -> CircleLayer(LOCATIONS_LAYER, LOCATIONS_SOURCE).withProperties(
                PropertyFactory.circleColor(LOCATIONS_COLOR),
                PropertyFactory.circleRadius(LOCATIONS_SIZE),
                PropertyFactory.circleOpacity(LOCATIONS_OPACITY),
                PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
                PropertyFactory.circleStrokeColor(LOCATIONS_STROKE_COLOR),
                PropertyFactory.circleStrokeWidth(LOCATIONS_STROKE_SIZE)
            )

            HistoryMapMode.Heatmap -> HeatmapLayer(
                LOCATIONS_LAYER,
                LOCATIONS_SOURCE
            )
        }
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

    companion object {

        // roughly Münster Westf.
        private const val INITIAL_LATITUDE = 51.961563
        private const val INITIAL_LONGITUDE = 7.628202
        private const val INITIAL_ZOOM = 3.0

        private const val LOCATIONS_LAYER = "exclusion_zone_layer"
        private const val LOCATIONS_SOURCE = "exclusion_zone_source"
        private const val LOCATIONS_COLOR = Color.BLUE
        private const val LOCATIONS_STROKE_COLOR = Color.WHITE
        private const val LOCATIONS_SIZE = 8f
        private const val LOCATIONS_OPACITY = 0.7f
        private const val LOCATIONS_STROKE_SIZE = 2f
        private const val LOCATIONS_PADDING = 100

        // replace with proper style, if available
        private const val TILE_SERVER = "https://demotiles.maplibre.org/style.json"
    }
}