package de.fh.muenster.locationprivacytoolkit.processors.ui

import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
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
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkit
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.databinding.FragmentLocationHistoryBinding
import de.fh.muenster.locationprivacytoolkit.processors.utils.LocationPrivacyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

private enum class HistoryMapMode {
    Timeline,
    Heatmap
}

private enum class HistoryMapFilterMode {
    Area,
    Time
}

class HistoryProcessorFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private var locationDatabase: LocationPrivacyDatabase? = null
    private var locationPrivacyConfig: LocationPrivacyConfigManager? = null
    private var lastLocations: List<Location>? = null
    private var isLayersFabExtended = false
    private var mapMode: HistoryMapMode = HistoryMapMode.Timeline

    // filter
    private val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT)
    private var mapFilterMode: HistoryMapFilterMode = HistoryMapFilterMode.Time
        set(value) {
            field = value
            when (value) {
                HistoryMapFilterMode.Time -> {
                    if (timeFilterRange == null) {
                        val startTime = lastLocations?.minBy { l -> l.time }?.time ?: 0
                        val endTime = lastLocations?.maxBy { l -> l.time }?.time ?: startTime
                        timeFilterRange = LongRange(startTime, endTime)
                        updateTimeFilterLabel()
                    }
                    loadLocations()
                }

                HistoryMapFilterMode.Area -> {
                    // TODO: add bounds
                    loadLocations()
                }
            }
        }
    private var timeFilterRange: LongRange? = null
    private var areaFilterBounds: LatLngBounds? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        context?.let {
            Mapbox.getInstance(it)
            locationPrivacyConfig = LocationPrivacyConfigManager(it)
            locationDatabase = LocationPrivacyDatabase.sharedInstance(it)
        }

        binding = FragmentLocationHistoryBinding.inflate(inflater, container, false)
        binding.mapView.getMapAsync { map ->
            map.setStyle(LocationPrivacyToolkit.mapTilesUrl)
            loadLocations()
        }

        updateLayerFabState()
        binding.timeLineLayerFab.visibility = View.GONE
        binding.timeLineLayerFabText.visibility = View.GONE
        binding.heatmapLayerFab.visibility = View.GONE
        binding.heatmapLayerFabText.visibility = View.GONE
        binding.layersFab.shrink()

        binding.layersFab.setOnClickListener {
            if (isLayersFabExtended) {
                isLayersFabExtended = false
                binding.timeLineLayerFab.hide()
                binding.heatmapLayerFab.hide()
                binding.layersFab.shrink()
                binding.timeLineLayerFabText.visibility = View.GONE
                binding.heatmapLayerFabText.visibility = View.GONE
            } else {
                isLayersFabExtended = true
                binding.timeLineLayerFab.show()
                binding.heatmapLayerFab.show()
                binding.timeLineLayerFabText.visibility = View.VISIBLE
                binding.heatmapLayerFabText.visibility = View.VISIBLE
                binding.layersFab.extend()
            }
        }

        binding.timeLineLayerFab.setOnClickListener {
            changeMapMode(HistoryMapMode.Timeline)
        }
        binding.timeLineLayerFabText.setOnClickListener {
            changeMapMode(HistoryMapMode.Timeline)
        }

        binding.heatmapLayerFab.setOnClickListener {
            changeMapMode(HistoryMapMode.Heatmap)
        }
        binding.heatmapLayerFabText.setOnClickListener {
            changeMapMode(HistoryMapMode.Heatmap)
        }

        binding.filterFab.setOnClickListener {
            val newVisibility =
                if (binding.filterCard.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            binding.filterCard.visibility = newVisibility
        }

        binding.filterToggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            when (checkedId) {
                binding.filterByTimeButton.id -> {
                    if (isChecked) {
                        binding.timeFilterLayout.visibility = View.VISIBLE
                        mapFilterMode = HistoryMapFilterMode.Time
                    } else {
                        binding.timeFilterLayout.visibility = View.GONE
                    }
                }

                binding.filterByAreaButton.id -> {
                    if (isChecked) {
                        binding.areaFilterLayout.visibility = View.VISIBLE
                        mapFilterMode = HistoryMapFilterMode.Area
                    } else {
                        binding.areaFilterLayout.visibility = View.GONE
                    }
                }

                else -> return@addOnButtonCheckedListener
            }
        }
        binding.filterToggleGroup.check(binding.filterByTimeButton.id)

        binding.filterCardDeleteButton.setOnClickListener {
            // TODO: apply filters
            removePersistedLocations()
        }

        binding.timeFilterDateRangeButton.setOnClickListener {
            val dateRangePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                    .setTitleText("Select dates")
                    .build()
            dateRangePicker.addOnPositiveButtonClickListener { range ->
                timeFilterRange = LongRange(range.first, range.second)
                updateTimeFilterLabel()
                loadLocations()
            }
            dateRangePicker.show(parentFragmentManager, null)
        }

        return binding.root
    }

    private fun changeMapMode(newMode: HistoryMapMode) {
        val locations = lastLocations ?: return
        if (newMode != mapMode) {
            mapMode = newMode
            addLocationsToMap(locations)
            updateLayerFabState()
        }
    }

    private fun updateLayerFabState() {
        when (mapMode) {
            HistoryMapMode.Timeline -> {
                binding.timeLineLayerFabText.typeface = Typeface.DEFAULT_BOLD
                binding.heatmapLayerFabText.typeface = Typeface.DEFAULT
            }

            HistoryMapMode.Heatmap -> {
                binding.timeLineLayerFabText.typeface = Typeface.DEFAULT
                binding.heatmapLayerFabText.typeface = Typeface.DEFAULT_BOLD
            }
        }
    }

    private fun updateTimeFilterLabel() {
        timeFilterRange?.let { range ->
            val startDate = Date(range.first)
            val endDate = Date(range.last)
            binding.timeFilterDateRangeStart.text = dateFormat.format(startDate)
            binding.timeFilterDateRangeEnd.text = dateFormat.format(endDate)
        }
    }

    private fun loadLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            val locations = locationDatabase?.loadLocations() ?: emptyList()
            lastLocations = filterLocations(locations)
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

    private fun removePersistedLocations() {
        val oldLocations = locationDatabase?.loadLocations() ?: emptyList()
        locationDatabase?.removeAll()
        val snackbar =
            Snackbar.make(binding.root, R.string.historyDeletedMessage, Snackbar.LENGTH_LONG)
        snackbar.setAction(
            R.string.exclusionZonesDeleteUndo
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                locationDatabase?.add(oldLocations)
            }
        }
        snackbar.show()
    }

    private fun filterLocations(locations: List<Location>): List<Location> {
        return when (mapFilterMode) {
            HistoryMapFilterMode.Time -> {
                timeFilterRange?.let { range ->
                    locations.filter { l -> l.time >= range.first && l.time <= range.last }
                } ?: locations
            }

            HistoryMapFilterMode.Area -> {
                areaFilterBounds?.let { bounds ->
                    locations.filter { l -> bounds.contains(LatLng(l.latitude, l.longitude)) }
                } ?: locations
            }
        }
    }

    private fun addLocationsToMap(locations: List<Location>) {
        if (locations.isEmpty()) return
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
            HistoryMapMode.Timeline -> CircleLayer(
                LOCATIONS_LAYER,
                LOCATIONS_SOURCE
            ).withProperties(
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
    }
}