package de.fh.muenster.locationprivacytoolkitapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkit
import de.fh.muenster.locationprivacytoolkit.ui.LocationPrivacyConfigActivity
import de.fh.muenster.locationprivacytoolkitapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationToolkit: LocationPrivacyToolkit
    private val dateFormat = SimpleDateFormat("MM/dd HH:mm:ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationToolkit = LocationPrivacyToolkit(applicationContext)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.configButton.setOnClickListener { view: View ->
            startActivity(Intent(this, LocationPrivacyConfigActivity::class.java))
        }

        binding.startTrackingButton.setOnClickListener { view: View ->
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
                return@setOnClickListener
            }
            locationToolkit.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 1000, 10f, this)
            binding.startTrackingButton.isEnabled = false
            binding.stopTrackingButton.isEnabled = true
        }

        binding.stopTrackingButton.setOnClickListener { view: View ->
            locationToolkit.removeUpdates(this)
            binding.startTrackingButton.isEnabled = true
            binding.stopTrackingButton.isEnabled = false
        }
    }

    // LocationListener
    override fun onLocationChanged(l: Location) {
        val locationString = "[${dateFormat.format(Date(l.time))}] ${l.latitude} / ${l.longitude} (${l.accuracy}m)"
        if (!binding.locationTextView.text.isBlank()) {
            binding.locationTextView.text = "${binding.locationTextView.text}\n${locationString}"
        } else {
            binding.locationTextView.text = "${binding.locationTextView.text}${locationString}"
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CURRENT_POSITION = 801
    }

}