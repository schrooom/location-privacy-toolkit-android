package de.fh.muenster.locationprivacytoolkit.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.databinding.ActivityLocationPrivacyConfigBinding

class LocationPrivacyConfigActivity : AppCompatActivity(), LocationPrivacyConfigAdapter.LocationPrivacyConfigAdapterListener {

    private lateinit var binding: ActivityLocationPrivacyConfigBinding
    private lateinit var configAdapter: LocationPrivacyConfigAdapter
    private lateinit var configManager: LocationPrivacyConfigManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configManager = LocationPrivacyConfigManager(this)
        binding = ActivityLocationPrivacyConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configAdapter = LocationPrivacyConfigAdapter(this).apply {
            val keys = LocationPrivacyConfig.values().toList()
            submitList(keys)
            notifyItemRangeChanged(0, keys.size)
        }
        binding.locationConfigRecyclerView.adapter = configAdapter
    }

    // LocationPrivacyConfigAdapterListener

    override fun onPrivacyConfigChanged(config: LocationPrivacyConfig, value: Int) {
        configManager.setPrivacyConfig(config, value)
    }

    override fun getPrivacyConfigValue(config: LocationPrivacyConfig): Int {
        return configManager.getPrivacyConfig(config) ?: config.defaultValue
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun refreshRecyclerView() {
        configAdapter.notifyDataSetChanged()
    }

}
