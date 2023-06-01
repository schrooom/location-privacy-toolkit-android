package de.fh.muenster.locationprivacytoolkit.processors.ui

import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.databinding.FragmentLocationHistoryBinding
import de.fh.muenster.locationprivacytoolkit.processors.utils.LocationPrivacyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class HistoryProcessorFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private var locationPrivacyConfig: LocationPrivacyConfigManager? = null
    private val locationDatabase = LocationPrivacyDatabase.sharedInstance

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        context?.let {
            Mapbox.getInstance(it)
            locationPrivacyConfig = LocationPrivacyConfigManager(it)
        }

        binding = FragmentLocationHistoryBinding.inflate(inflater, container, false)

        loadLocations()

        return binding.root
    }

    private fun loadLocations() {
        GlobalScope.launch {
            val locations = locationDatabase.locations
            CoroutineScope(this.coroutineContext).launch {
                if (locations.isNotEmpty()) {
                    addLocationsToMap(locations)
                } else {
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
                }
            }
        }
    }

    private fun addLocationsToMap(locations: List<Location>) {
        binding.mapView.getMapAsync { map ->
            map.style?.let { style ->
                // add locations  layer
                val locationPoints = locations.map { l ->
                    Feature.fromGeometry(
                        Point.fromLngLat(
                            l.longitude, l.latitude
                        )
                    )
                }
                style.addSource(GeoJsonSource(LOCATIONS_SOURCE).apply {
                    setGeoJson(FeatureCollection.fromFeatures(locationPoints))
                })
                val locationsLayer = CircleLayer(LOCATIONS_LAYER, LOCATIONS_SOURCE).withProperties(
                    PropertyFactory.circleColor(LOCATIONS_COLOR),
                    PropertyFactory.circleRadius(LOCATIONS_SIZE),
                    PropertyFactory.circleStrokeColor(LOCATIONS_STROKE_COLOR),
                    PropertyFactory.circleStrokeWidth(LOCATIONS_STROKE_SIZE)
                )
                style.addLayer(locationsLayer)
                val bounds = LatLngBounds.fromLatLngs(locations.map { l -> LatLng(l) })
                val update = CameraUpdateFactory.newLatLngBounds(bounds, 0)
                map.easeCamera(update)
            }
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
        private const val LOCATIONS_SIZE = 20f
        private const val LOCATIONS_STROKE_SIZE = 1f

        // replace with proper style, if available
        private const val TILE_SERVER = "https://demotiles.maplibre.org/style.json"
    }
}