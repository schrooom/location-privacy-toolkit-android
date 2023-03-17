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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfConstants.UNIT_METERS
import com.mapbox.turf.TurfTransformation
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkit
import de.fh.muenster.locationprivacytoolkit.ui.LocationPrivacyConfigActivity
import de.fh.muenster.locationprivacytoolkitapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationToolkit: LocationPrivacyToolkit
    private val dateFormat = SimpleDateFormat("MM/dd HH:mm:ss")

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this)
        locationToolkit = LocationPrivacyToolkit(applicationContext)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.configButton.setOnClickListener {
            startActivity(Intent(this, LocationPrivacyConfigActivity::class.java))
        }

        binding.startTrackingButton.setOnClickListener {
            if (!checkPermissions()) {
                return@setOnClickListener
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationToolkit.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 1000, 10f, this)
            } else {
                locationToolkit.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, this)
            }
            binding.startTrackingButton.isEnabled = false
            binding.stopTrackingButton.isEnabled = true
        }

        binding.stopTrackingButton.setOnClickListener {
            locationToolkit.removeUpdates(this)
            binding.startTrackingButton.isEnabled = true
            binding.stopTrackingButton.isEnabled = false
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync { map ->
            map.setStyle(TILE_SERVER)
            centerMapTo(map, INITIAL_LATITUDE, INITIAL_LONGITUDE, INITIAL_ZOOM)
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
            addLocationToMap(map, l)
        }
    }

    private fun addLocationToMap(map: MapboxMap, l: Location) {
        map.getStyle { style ->
            val newLocationJson = Point.fromLngLat(l.longitude, l.latitude)
            val oldLocationSource = style.getSource(POSITION_SOURCE_ID) as? GeoJsonSource
            if (oldLocationSource != null) {
                oldLocationSource.setGeoJson(newLocationJson)
            } else {
                style.addSource(GeoJsonSource(POSITION_SOURCE_ID).apply {
                    setGeoJson(newLocationJson)
                })
            }
            val haloData = TurfTransformation.circle(Point.fromLngLat(l.longitude, l.latitude), l.accuracy.toDouble(), UNIT_METERS)
            val oldHaloSource = style.getSource(POSITION_HALO_SOURCE_ID) as? GeoJsonSource
            if (oldHaloSource != null) {
                oldHaloSource.setGeoJson(haloData)
            } else {
                style.addSource(GeoJsonSource(POSITION_HALO_SOURCE_ID).apply {
                    setGeoJson(haloData)
                })
            }
            if (style.getLayer(POSITION_LAYER_ID) == null) {
                val positionLayer = CircleLayer(POSITION_LAYER_ID, POSITION_SOURCE_ID).withProperties(
                    PropertyFactory.circleRadius(5f),
                    PropertyFactory.circleColor(Color.RED),
                    PropertyFactory.circleOpacity(0.75f)
                )
                style.addLayer(positionLayer)
                val haloLayer = FillLayer(POSITION_HALO_LAYER_ID, POSITION_HALO_SOURCE_ID).withProperties(
                    PropertyFactory.fillColor(Color.RED),
                    PropertyFactory.fillOpacity(0.1f)
                )
                style.addLayer(haloLayer)
            }
        }
    }

    private fun centerMapTo(map: MapboxMap, lat: Double, lon: Double, zoom: Double = POSITION_ZOOM) {
        val camera = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom)
        map.easeCamera(camera)
    }

    // LocationListener
    override fun onLocationChanged(l: Location) {
        val locationString = "[${dateFormat.format(Date(l.time))}] ${l.latitude} / ${l.longitude} (${l.accuracy}m)"
        if (binding.locationTextView.text.isBlank()) {
            binding.locationTextView.text = "${binding.locationTextView.text}${locationString}"
        } else {
            binding.locationTextView.text = "${binding.locationTextView.text}\n${locationString}"
        }
        processLocation(l)
    }

    companion object {
        const val PERMISSION_REQUEST_CURRENT_POSITION = 801
        private const val POSITION_SOURCE_ID = "last_location_source_id"
        private const val POSITION_LAYER_ID = "last_location_layer_id"
        private const val POSITION_ZOOM = 13.0
        private const val POSITION_HALO_SOURCE_ID = "last_location_halo_source_id"
        private const val POSITION_HALO_LAYER_ID = "last_location_halo_layer_id"

        // roughly Münster Westf.
        private const val INITIAL_LATITUDE = 51.961563
        private const val INITIAL_LONGITUDE = 7.628202
        private const val INITIAL_ZOOM = 8.0

        // replace with proper style, if available
        private const val TILE_SERVER = "https://demotiles.maplibre.org/style.json"
    }

}