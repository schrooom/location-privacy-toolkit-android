package de.fh.muenster.locationprivacytoolkit.processors.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
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
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolDragListener
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM
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
import kotlin.math.roundToInt


class ExclusionZoneProcessorFragment : Fragment() {

    private lateinit var binding: FragmentExclusionZoneBinding
    private var locationPrivacyConfig: LocationPrivacyConfigManager? = null
    private var symbolManager: SymbolManager? = null
    private var lastZoneCreationCenter: LatLng? = null
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
            showZoneCreationOverlay()
        }
        binding.removeZonesButton.setOnClickListener {
            removeAllZones()
        }
        binding.exclusionZoneCardCloseButton.setOnClickListener {
            hideZoneCreationOverlay()
        }
        binding.exclusionZoneCardCreateButton.setOnClickListener {
            createZone()
        }
        binding.exclusionZoneSlider.valueFrom = MIN_ZONE_RADIUS
        binding.exclusionZoneSlider.valueTo = MAX_ZONE_RADIUS
        binding.exclusionZoneSlider.value = INITIAL_ZONE_RADIUS
        binding.exclusionZoneSlider.stepSize = ZONE_STEP_SIZE
        binding.exclusionZoneSlider.setLabelFormatter { value -> "${value.roundToInt()} m" }

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

    private fun showZoneCreationOverlay() {
        binding.exclusionZoneCard.visibility = View.VISIBLE
        binding.addZoneButton.visibility = View.GONE
        binding.removeZonesButton.visibility = View.GONE

        // show marker on map
        binding.mapView.getMapAsync { map ->
            map.cameraPosition.target?.let { center ->
                map.style?.let { style ->
                    addImageToMap(style, ADD_ZONE_IMAGE, R.drawable.ic_add_location)
                    symbolManager = SymbolManager(binding.mapView, map, style)
                    symbolManager?.iconAllowOverlap = true
                    symbolManager?.iconIgnorePlacement = true
                    val symbolOptions = SymbolOptions()
                        .withLatLng(center)
                        .withIconImage(ADD_ZONE_IMAGE)
                        .withIconSize(2f)
                        .withIconAnchor(ICON_ANCHOR_BOTTOM)
                        .withDraggable(true)
                    symbolManager?.create(symbolOptions)
                    lastZoneCreationCenter = center
                    symbolManager?.addDragListener(object : OnSymbolDragListener {
                        override fun onAnnotationDragStarted(annotation: Symbol) = Unit
                        override fun onAnnotationDrag(annotation: Symbol) = Unit
                        override fun onAnnotationDragFinished(annotation: Symbol) {
                            lastZoneCreationCenter = annotation.latLng
                        }
                    })
                }
            }
        }
    }

    private fun addImageToMap(style: Style, id: String, resId: Int) {
        context?.let { c ->
            AppCompatResources.getDrawable(c, resId)?.let { image ->
                style.addImage(id, image)
            }
        }
    }

    private fun hideZoneCreationOverlay() {
        binding.exclusionZoneCard.visibility = View.GONE
        binding.addZoneButton.visibility = View.VISIBLE
        symbolManager?.deleteAll()
        reloadExclusionZones()
    }

    private fun createZone() {
        lastZoneCreationCenter?.let { center ->
            val radius = binding.exclusionZoneSlider.value
            val zone = ExclusionZone(center, radius.toInt())
            addExclusionZone(zone)
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
                    PropertyFactory.fillColor(CREATED_ZONE_COLOR),
                    PropertyFactory.fillOpacity(0.5f),
                    PropertyFactory.fillOutlineColor(CREATED_ZONE_COLOR),
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

        private const val MIN_ZONE_RADIUS = 100f
        private const val INITIAL_ZONE_RADIUS = 500f
        private const val MAX_ZONE_RADIUS = 10000f
        private const val ZONE_STEP_SIZE = 100f

        private const val NEW_ZONE_COLOR = Color.RED
        private const val CREATED_ZONE_COLOR = Color.GRAY
        private const val ADD_ZONE_IMAGE = "add_zone_image"
        private const val ZONE_LAYER = "exclusion_zone_layer"
        private const val ZONE_SOURCE = "exclusion_zone_source"

        // replace with proper style, if available
        private const val TILE_SERVER = "https://demotiles.maplibre.org/style.json"
    }
}