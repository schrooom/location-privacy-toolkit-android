package de.fh.muenster.locationprivacytoolkit.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.databinding.ActivityLocationPrivacyConfigBinding

class LocationPrivacyConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationPrivacyConfigBinding
    private lateinit var configAdapter: LocationPrivacyConfigAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationPrivacyConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAdapter = LocationPrivacyConfigAdapter().apply {
            val keys = LocationPrivacyConfig.values().toList()
            submitList(keys)
            notifyItemRangeChanged(0, keys.size)
        }
        binding.locationConfigRecyclerView.adapter = configAdapter
    }
}
