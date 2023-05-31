package de.fh.muenster.locationprivacytoolkit.processors.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.databinding.FragmentLocationHistoryBinding


class HistoryProcessorFragment : Fragment() {

    private lateinit var binding: FragmentLocationHistoryBinding
    private var locationPrivacyConfig: LocationPrivacyConfigManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        context?.let {
            Mapbox.getInstance(it)
            locationPrivacyConfig = LocationPrivacyConfigManager(it)
        }

        binding = FragmentLocationHistoryBinding.inflate(inflater, container, false)

        binding.mapView.getMapAsync { map ->
            map.setStyle(TILE_SERVER)
            val initialLatLng = LatLng(
                INITIAL_LATITUDE,
                INITIAL_LONGITUDE
            )
            val camera = CameraUpdateFactory.newLatLngZoom(
                initialLatLng,
                INITIAL_ZOOM
            )
            map.easeCamera(camera)
        }

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

    companion object {

        // roughly Münster Westf.
        private const val INITIAL_LATITUDE = 51.961563
        private const val INITIAL_LONGITUDE = 7.628202
        private const val INITIAL_ZOOM = 3.0

        // replace with proper style, if available
        private const val TILE_SERVER = "https://demotiles.maplibre.org/style.json"
    }
}