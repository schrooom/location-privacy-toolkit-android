package de.fh.muenster.locationprivacytoolkit.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.databinding.ActivityLocationPrivacyConfigBinding

class LocationPrivacyConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLocationPrivacyConfigBinding
    private val configAdapter = LocationPrivacyConfigAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationPrivacyConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val keys = LocationPrivacyConfig.values().toList()
        configAdapter.submitList(keys)
        configAdapter.notifyItemRangeChanged(0, keys.size)
        binding.locationConfigRecyclerView.adapter = configAdapter
    }
}
