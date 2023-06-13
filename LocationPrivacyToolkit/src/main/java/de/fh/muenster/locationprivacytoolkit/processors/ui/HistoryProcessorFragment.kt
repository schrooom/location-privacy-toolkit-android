package de.fh.muenster.locationprivacytoolkit.processors.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.LineString
import com.mapbox.geojson.MultiPoint
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfJoins
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


private enum class HistoryMapContentMode {
    Timeline, Heatmap
}

private enum class HistoryMapFilterMode {
    Area, Time
}

private enum class HistoryMapTouchMode {
    Move, Draw
}

class HistoryProcessorFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private var locationDatabase: LocationPrivacyDatabase? = null
    private var locationPrivacyConfig: LocationPrivacyConfigManager? = null
    private var lastLocations: List<Location>? = null
    private var isLayersFabExtended = false

    // map modes
    private var mapContentMode: HistoryMapContentMode = HistoryMapContentMode.Timeline
    private var mapTouchMode: HistoryMapTouchMode = HistoryMapTouchMode.Move
        @SuppressLint("ClickableViewAccessibility") set(value) {
            field = value
            when (value) {
                HistoryMapTouchMode.Move -> {
                    binding.mapView.setOnTouchListener(null)
                    binding.filterCardCreateAreaButton.visibility = View.VISIBLE
                    binding.filterCardAreaHintText.visibility = View.GONE
                }

                HistoryMapTouchMode.Draw -> {
                    binding.mapView.setOnTouchListener(drawTouchListener)
                    binding.filterCardCreateAreaButton.visibility = View.GONE
                    binding.filterCardAreaHintText.visibility = View.VISIBLE
                }
            }
        }
    private var mapFilterMode: HistoryMapFilterMode = HistoryMapFilterMode.Time
        set(value) {
            field = value
            when (value) {
                HistoryMapFilterMode.Time -> {
                    // reset map touch mode
                    mapTouchMode = HistoryMapTouchMode.Move
                    updateAreaFilterOnMap(hide = true)
                    loadLocations()
                    updateTimeFilterLabel()
                }

                HistoryMapFilterMode.Area -> {
                    // TODO: add bounds
                    loadLocations()
                }
            }
        }

    // filter
    private val dateFormat = SimpleDateFormat.getDateInstance(DateFormat.SHORT)
    private var timeFilterRange: LongRange? = null
    private val areaFilterPolygon = mutableListOf<Point>()
    private val areaFilterOutline = mutableListOf<Point>()

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
            map.getStyle {
                initMapForAreaFilterSelection()
            }
            map.setStyle(LocationPrivacyToolkit.mapTilesUrl)
            loadLocations()
        }

        initFloatingActionButtons()
        initFilterOptions()

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

    private fun initFloatingActionButtons() {
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
            changeMapContentMode(HistoryMapContentMode.Timeline)
        }
        binding.timeLineLayerFabText.setOnClickListener {
            changeMapContentMode(HistoryMapContentMode.Timeline)
        }

        binding.heatmapLayerFab.setOnClickListener {
            changeMapContentMode(HistoryMapContentMode.Heatmap)
        }
        binding.heatmapLayerFabText.setOnClickListener {
            changeMapContentMode(HistoryMapContentMode.Heatmap)
        }
    }

    private fun initFilterOptions() {
        binding.filterCardCloseButton.setOnClickListener {
            binding.filterCard.visibility = View.GONE
        }
        binding.filterFab.setOnClickListener {
            val showFilter = binding.filterCard.visibility == View.GONE
            binding.filterCard.visibility = if (showFilter) View.VISIBLE else View.GONE
            if (showFilter && timeFilterRange == null) {
                // init timeFilterRange
                val startTime = lastLocations?.minBy { l -> l.time }?.time ?: 0
                val endTime = lastLocations?.maxBy { l -> l.time }?.time ?: startTime
                timeFilterRange = LongRange(startTime, endTime)
                updateTimeFilterLabel()
            }
        }

        binding.filterToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
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

        binding.filterCardResetButton.setOnClickListener {
            areaFilterOutline.clear()
            areaFilterPolygon.clear()
            updateAreaFilterOnMap()
        }
        binding.filterCardDeleteButton.setOnClickListener {
            // TODO: apply filters
            removePersistedLocations()
        }

        binding.filterCardCreateAreaButton.setOnClickListener {
            mapTouchMode = HistoryMapTouchMode.Draw
        }

        binding.timeFilterDateRangeButton.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker().apply {
                setTitleText(R.string.historyFilterByTimeRange)
                timeFilterRange?.let { range ->
                    setSelection(Pair(range.first, range.last))
                }
            }.build()
            dateRangePicker.addOnPositiveButtonClickListener { range ->
                timeFilterRange = LongRange(range.first, range.second)
                updateTimeFilterLabel()
                loadLocations()
            }
            dateRangePicker.show(parentFragmentManager, null)
        }
    }

    private fun initMapForAreaFilterSelection() {
        binding.mapView.getMapAsync { map ->
            map.style?.let { style ->
                style.addSource(GeoJsonSource(AREA_FILTER_LINE_SOURCE))
                style.addSource(GeoJsonSource(AREA_FILTER_FILL_SOURCE))

                style.addLayer(
                    LineLayer(
                        AREA_FILTER_LINE_LAYER, AREA_FILTER_LINE_SOURCE
                    ).withProperties(
                        PropertyFactory.lineWidth(AREA_FILTER_LINE_WIDTH),
                        PropertyFactory.lineJoin(LINE_JOIN_ROUND),
                        PropertyFactory.lineOpacity(AREA_FILTER_LINE_OPACITY),
                        PropertyFactory.lineColor(Color.parseColor(AREA_FILTER_LINE_COLOR))
                    )
                )
                style.addLayerBelow(
                    FillLayer(
                        AREA_FILTER_FILL_LAYER, AREA_FILTER_FILL_SOURCE
                    ).withProperties(
                        PropertyFactory.fillColor(Color.RED),
                        PropertyFactory.fillOpacity(AREA_FILTER_FILL_OPACITY)
                    ), AREA_FILTER_LINE_LAYER
                )

            }
        }
    }

    private fun changeMapContentMode(newMode: HistoryMapContentMode) {
        val locations = lastLocations ?: return
        if (newMode != mapContentMode) {
            mapContentMode = newMode
            addLocationsToMap(locations)
            updateLayerFabState()
        }
    }

    private fun updateLayerFabState() {
        when (mapContentMode) {
            HistoryMapContentMode.Timeline -> {
                binding.timeLineLayerFabText.typeface = Typeface.DEFAULT_BOLD
                binding.heatmapLayerFabText.typeface = Typeface.DEFAULT
            }

            HistoryMapContentMode.Heatmap -> {
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
        CoroutineScope(Dispatchers.IO).launch {
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
            withContext(Dispatchers.Main) {
                snackbar.show()
            }
        }
    }

    private fun filterLocations(locations: List<Location>): List<Location> {
        return when (mapFilterMode) {
            HistoryMapFilterMode.Time -> {
                timeFilterRange?.let { range ->
                    locations.filter { l -> l.time >= range.first && l.time <= range.last }
                } ?: locations
            }

            HistoryMapFilterMode.Area -> {
                val polygon: Polygon = Polygon.fromLngLats(listOf(areaFilterPolygon))
                locations.filter { l ->
                    TurfJoins.inside(Point.fromLngLat(l.longitude, l.latitude), polygon)
                }
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
        return when (mapContentMode) {
            HistoryMapContentMode.Timeline -> CircleLayer(
                LOCATIONS_LAYER, LOCATIONS_SOURCE
            ).withProperties(
                PropertyFactory.circleColor(LOCATIONS_COLOR),
                PropertyFactory.circleRadius(LOCATIONS_SIZE),
                PropertyFactory.circleOpacity(LOCATIONS_OPACITY),
                PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP),
                PropertyFactory.circleStrokeColor(LOCATIONS_STROKE_COLOR),
                PropertyFactory.circleStrokeWidth(LOCATIONS_STROKE_SIZE)
            )

            HistoryMapContentMode.Heatmap -> HeatmapLayer(
                LOCATIONS_LAYER, LOCATIONS_SOURCE
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val drawTouchListener = OnTouchListener { _, motionEvent ->
        binding.mapView.getMapAsync { map ->
            val latLngTouchCoordinate: LatLng = map.projection.fromScreenLocation(
                PointF(motionEvent.x, motionEvent.y)
            )
            val screenTouchPoint = Point.fromLngLat(
                latLngTouchCoordinate.longitude, latLngTouchCoordinate.latitude
            )

            // update polygon and outline
            areaFilterOutline.add(screenTouchPoint)
            if (areaFilterPolygon.size < 2) {
                areaFilterPolygon.add(screenTouchPoint)
            } else {
                if (areaFilterPolygon.size > 2) {
                    areaFilterPolygon.removeAt(
                        areaFilterPolygon.size - 1
                    )
                }
                areaFilterPolygon.add(screenTouchPoint)
                areaFilterPolygon.add(
                    areaFilterPolygon[0]
                )
            }
            // draw polygon and outline
            updateAreaFilterOnMap()

            if (motionEvent.action == MotionEvent.ACTION_UP) {
                // add the first screen touch point to the end of the outline
                areaFilterOutline.add(
                    areaFilterOutline[0]
                )

                // drawing is done, reset to move mode
                mapTouchMode = HistoryMapTouchMode.Move
            }
        }
        true
    }

    private fun updateAreaFilterOnMap(hide: Boolean = false) {
        val polyline = LineString.fromLngLats(
            areaFilterOutline
        )
        val polygon: Polygon = Polygon.fromLngLats(listOf(areaFilterPolygon))
        binding.mapView.getMapAsync { map ->
            map.style?.let { style ->
                style.getSourceAs<GeoJsonSource>(AREA_FILTER_LINE_SOURCE)?.setGeoJson(
                    if (hide) null else polyline
                )
                style.getSourceAs<GeoJsonSource>(AREA_FILTER_FILL_SOURCE)?.setGeoJson(
                    if (hide) null else polygon
                )
            }
        }
    }

    companion object {

        // roughly Münster Westf.
        private const val INITIAL_LATITUDE = 51.961563
        private const val INITIAL_LONGITUDE = 7.628202
        private const val INITIAL_ZOOM = 3.0

        // general locations
        private const val LOCATIONS_LAYER = "exclusion_zone_layer"
        private const val LOCATIONS_SOURCE = "exclusion_zone_source"
        private const val LOCATIONS_COLOR = Color.BLUE
        private const val LOCATIONS_STROKE_COLOR = Color.WHITE
        private const val LOCATIONS_SIZE = 8f
        private const val LOCATIONS_OPACITY = 0.7f
        private const val LOCATIONS_STROKE_SIZE = 2f
        private const val LOCATIONS_PADDING = 100

        // area filter selection
        private const val AREA_FILTER_LINE_SOURCE = "area_filter_line_source"
        private const val AREA_FILTER_FILL_SOURCE = "area_filter_fill_source"
        private const val AREA_FILTER_LINE_LAYER = "area_filter_line_layer"
        private const val AREA_FILTER_FILL_LAYER = "area_filter_fill_layer"
        private const val AREA_FILTER_LINE_COLOR = "#a0861c"
        private const val AREA_FILTER_LINE_WIDTH = 5f
        private const val AREA_FILTER_LINE_OPACITY = 1f
        private const val AREA_FILTER_FILL_OPACITY = .4f
    }
}