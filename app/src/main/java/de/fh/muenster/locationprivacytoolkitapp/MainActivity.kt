package de.fh.muenster.locationprivacytoolkitapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.MultiPoint
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfConstants.UNIT_METERS
import com.mapbox.turf.TurfTransformation
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkit
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.processors.utils.LocationPrivacyVisibility
import de.fh.muenster.locationprivacytoolkit.ui.LocationPrivacyConfigActivity
import de.fh.muenster.locationprivacytoolkitapp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener, LocationPrivacyToolkitListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationToolkit: LocationPrivacyToolkit
    private val dateFormat = SimpleDateFormat("MM/dd HH:mm:ss")

    private val lastLocations: MutableList<Location> = mutableListOf()

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this)
        locationToolkit = LocationPrivacyToolkit(applicationContext, this)
        LocationPrivacyToolkit.mapTilesUrl = TILE_SERVER
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            lastLocations.addAll(locationToolkit.loadAllLocations())
            withContext(coroutineContext) {
                updateMapLocations()
            }
        }

        binding.configButton.setOnClickListener {
            startActivity(Intent(this, LocationPrivacyConfigActivity::class.java))
        }

        binding.startTrackingButton.setOnClickListener {
            if (!checkPermissions()) {
                return@setOnClickListener
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationToolkit.requestLocationUpdates(
                    LocationManager.FUSED_PROVIDER,
                    1000,
                    10f,
                    this
                )
            } else {
                locationToolkit.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000,
                    10f,
                    this
                )
            }
            binding.startTrackingButton.isEnabled = false
            binding.stopTrackingButton.isEnabled = true
            binding.clearTrackingButton.isEnabled = false
        }

        binding.stopTrackingButton.setOnClickListener {
            locationToolkit.removeUpdates(this)
            binding.startTrackingButton.isEnabled = true
            binding.stopTrackingButton.isEnabled = false
            binding.clearTrackingButton.isEnabled = binding.locationTextView.text.isNotBlank()
        }

        binding.clearTrackingButton.setOnClickListener {
            resetMap()
            binding.locationTextView.text = ""
            binding.clearTrackingButton.isEnabled = false
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            map.setStyle(TILE_SERVER)
            centerMapTo(map, INITIAL_LATITUDE, INITIAL_LONGITUDE, INITIAL_ZOOM)
        }
        binding.tilesButton.setOnClickListener {
            if (LocationPrivacyToolkit.mapTilesUrl == TILE_SERVER) {
                LocationPrivacyToolkit.mapTilesUrl = ALT_TILE_SERVER
                binding.tilesButton.setImageResource(R.drawable.ic_moon)
            } else {
                LocationPrivacyToolkit.mapTilesUrl = TILE_SERVER
                binding.tilesButton.setImageResource(R.drawable.ic_sun)
            }
            binding.mapView.getMapAsync { map ->
                map.setStyle(LocationPrivacyToolkit.mapTilesUrl)
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

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CURRENT_POSITION
            )
            return false
        }
        return true
    }

    private fun processLocation(l: Location) {
        binding.mapView.getMapAsync { map ->
            // Set the map view to new location
            centerMapTo(map, l.latitude, l.longitude)
            lastLocations.add(l)
            addLocationToMap(map, l)
        }
        addLocationToDebugText(l)
    }

    private fun addLocationToMap(map: MapboxMap, l: Location) {
        updateMapLocations()
        map.getStyle { style ->
            val haloPolygon = TurfTransformation.circle(
                Point.fromLngLat(l.longitude, l.latitude),
                l.accuracy.toDouble(),
                UNIT_METERS
            )
            val oldHaloSource = style.getSource(HALO_SOURCE_ID) as? GeoJsonSource
            if (oldHaloSource != null) {
                oldHaloSource.setGeoJson(haloPolygon)
            } else {
                style.addSource(GeoJsonSource(HALO_SOURCE_ID).apply {
                    setGeoJson(haloPolygon)
                })
            }
        }
    }

    private fun updateMapLocations() {
        binding.mapView.getMapAsync { map ->
            map.getStyle { style ->
                val newLocationPoints =
                    lastLocations.map { loc -> Point.fromLngLat(loc.longitude, loc.latitude) }
                val newLocationMultiPoint = MultiPoint.fromLngLats(newLocationPoints)
                val oldLocationSource = style.getSource(POSITION_SOURCE_ID) as? GeoJsonSource
                if (oldLocationSource != null) {
                    oldLocationSource.setGeoJson(newLocationMultiPoint)
                } else {
                    style.addSource(GeoJsonSource(POSITION_SOURCE_ID).apply {
                        setGeoJson(newLocationMultiPoint)
                    })
                }
                if (style.getLayer(POSITION_LAYER_ID) == null) {
                    val positionLayer =
                        CircleLayer(POSITION_LAYER_ID, POSITION_SOURCE_ID).withProperties(
                            PropertyFactory.circleRadius(6f),
                            PropertyFactory.circleColor(Color.RED),
                            PropertyFactory.circleOpacity(0.75f)
                        )
                    style.addLayer(positionLayer)
                    val haloLayer = FillLayer(HALO_LAYER_ID, HALO_SOURCE_ID).withProperties(
                        PropertyFactory.fillColor(Color.RED),
                        PropertyFactory.fillOpacity(0.1f),
                    )
                    style.addLayerBelow(haloLayer, POSITION_LAYER_ID)
                    val lineLayer = LineLayer(LINE_LAYER_ID, POSITION_SOURCE_ID).withProperties(
                        PropertyFactory.lineColor(Color.BLUE),
                        PropertyFactory.lineOpacity(0.5f),
                        PropertyFactory.lineWidth(4f),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
                    )
                    style.addLayerBelow(lineLayer, HALO_LAYER_ID)
                }
            }
        }
    }

    private fun centerMapTo(map: MapboxMap, lat: Double, lon: Double, zoom: Double? = null) {
        val camera =
            CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom ?: map.cameraPosition.zoom)
        map.easeCamera(camera)
    }

    private fun resetMap() {
        binding.mapView.getMapAsync { map ->
            map.getStyle { style ->
                style.removeLayer(POSITION_LAYER_ID)
                style.removeLayer(HALO_LAYER_ID)
                style.removeLayer(LINE_LAYER_ID)
                style.removeSource(POSITION_SOURCE_ID)
                style.removeSource(HALO_SOURCE_ID)
            }
        }
        lastLocations.clear()
    }

    private fun addLocationToDebugText(l: Location) {
        val date = dateFormat.format(Date(l.time))
        val lat = BigDecimal(l.latitude).setScale(5, RoundingMode.HALF_EVEN)
        val lon = BigDecimal(l.longitude).setScale(5, RoundingMode.HALF_EVEN)
        val acc = BigDecimal(l.accuracy.toDouble()).setScale(1, RoundingMode.HALF_EVEN)
        val locationString = "[${date}] $lat / $lon (${acc}m)"
        val addLineString = if (binding.locationTextView.text.isBlank()) "" else "\n"
        val locationDebugText = "${binding.locationTextView.text}${addLineString}${locationString}"
        binding.locationTextView.text = locationDebugText
        binding.locationScrollTextView.postDelayed({
            binding.locationScrollTextView.fullScroll(
                ScrollView.FOCUS_DOWN
            )
        }, 0)
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // LocationListener
    override fun onLocationChanged(l: Location) {
        processLocation(l)
    }

    companion object {
        const val PERMISSION_REQUEST_CURRENT_POSITION = 801
        private const val POSITION_SOURCE_ID = "location_source_id"
        private const val POSITION_LAYER_ID = "location_layer_id"
        private const val HALO_SOURCE_ID = "halo_source_id"
        private const val HALO_LAYER_ID = "halo_layer_id"
        private const val LINE_LAYER_ID = "line_layer_id"

        // roughly Münster Westf.
        private const val INITIAL_LATITUDE = 51.961563
        private const val INITIAL_LONGITUDE = 7.628202
        private const val INITIAL_ZOOM = 13.0

        // replace with proper style, if available
        // CAUTION: DO NOT COMMIT THIS URL
        private const val TILE_SERVER = "https://demotiles.maplibre.org/style.json"
        private const val ALT_TILE_SERVER = "https://demotiles.maplibre.org/style.json"
    }

    // LocationPrivacyToolkitListener

    override fun onRemoveLocation(l: Location) {
        if (this.lastLocations.remove(l)) {
            val date = dateFormat.format(Date(l.time))
            showMessage("Location at $date deleted")
            updateMapLocations()
        }
    }

    override fun onRemoveLocation(timestamp: Long) {
        if (this.lastLocations.removeAll { it.time == timestamp }) {
            val date = dateFormat.format(Date(timestamp))
            showMessage("Location at $date deleted")
            updateMapLocations()
        }
    }

    override fun onRemoveLocations(locations: List<Location>) {
        if (this.lastLocations.removeAll(locations)) {
            showMessage("${locations.count()} locations deleted")
            updateMapLocations()
        }
    }

    override fun onRemoveLocationRange(fromTimestamp: Long, toTimestamp: Long) {
        if (this.lastLocations.removeAll { it.time in fromTimestamp..toTimestamp }) {
            val fromDate = dateFormat.format(Date(fromTimestamp))
            val toDate = dateFormat.format(Date(toTimestamp))
            showMessage("Locations from $fromDate-$toDate deleted")
            updateMapLocations()
        }
    }

    override fun onUpdateVisibilityPreference(visibility: LocationPrivacyVisibility) {
        // update visibility accordingly
    }
}