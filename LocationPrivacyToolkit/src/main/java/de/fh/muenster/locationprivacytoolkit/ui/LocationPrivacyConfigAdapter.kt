package de.fh.muenster.locationprivacytoolkit.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface
import de.fh.muenster.locationprivacytoolkit.databinding.ListItemLocationPrivacyConfigBinding

class LocationPrivacyConfigAdapter(private var listener: LocationPrivacyConfigAdapterListener): ListAdapter<LocationPrivacyConfig, LocationPrivacyConfigAdapter.LocationPrivacyConfigViewHolder>(
    diffCallback
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationPrivacyConfigViewHolder {
        val dataBinding = ListItemLocationPrivacyConfigBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LocationPrivacyConfigViewHolder(dataBinding, listener)
    }

    override fun onBindViewHolder(holder: LocationPrivacyConfigViewHolder, position: Int) {
        getItem(position)?.let { config ->
            holder.bindTo(config)
        }
    }


    class LocationPrivacyConfigViewHolder(
        private val dataBinding: ListItemLocationPrivacyConfigBinding,
        private val listener: LocationPrivacyConfigAdapterListener
    ) : RecyclerView.ViewHolder(dataBinding.root) {

        fun bindTo(config: LocationPrivacyConfig) {
            dataBinding.locationConfigTitle.text =
                dataBinding.root.context.getString(config.titleId)

            val hasLocationAccess = listener.getPrivacyConfigValue(LocationPrivacyConfig.Access) > 0

            when (config.userInterface) {
                LocationPrivacyConfigInterface.Switch -> initSwitch(config, hasLocationAccess)
                LocationPrivacyConfigInterface.Slider -> initSlider(config, hasLocationAccess)
            }
        }

        private fun initSwitch(config: LocationPrivacyConfig, hasLocationAccess: Boolean) {
            dataBinding.locationConfigSwitch.isChecked = listener.getPrivacyConfigValue(config) > 0
            val isLocationAccessConfig = config == LocationPrivacyConfig.Access
            dataBinding.locationConfigSwitch.setOnCheckedChangeListener { _, isChecked ->
                listener.onPrivacyConfigChanged(
                    config,
                    if (isChecked) 1 else 0
                )
                if (isLocationAccessConfig) {
                    listener.refreshRecyclerView()
                }
            }
            // enable, if location-access is enabled or if this is the button, to toggle location-access
            dataBinding.locationConfigSwitch.isEnabled = hasLocationAccess || isLocationAccessConfig
            dataBinding.locationConfigSwitch.visibility = View.VISIBLE
        }

        private fun initSlider(config: LocationPrivacyConfig, hasLocationAccess: Boolean) {
            val range = config.range
            dataBinding.locationConfigSlider.valueFrom = range.start.toFloat()
            dataBinding.locationConfigSlider.valueTo = range.endInclusive.toFloat()
            dataBinding.locationConfigSlider.stepSize = 1f
            dataBinding.locationConfigSlider.isTickVisible = true
            dataBinding.locationConfigSlider.setLabelFormatter { value ->
                val configValue = config.indexToValue(value)
                if (configValue != null) {
                    config.formatLabel(configValue)
                } else {
                    ""
                }
            }
            val currentValue = config.valueToIndex(listener.getPrivacyConfigValue(config))
            val initialValue = (currentValue ?: config.defaultValue).toFloat()
            dataBinding.locationConfigSlider.value = initialValue
            dataBinding.locationConfigSlider.addOnChangeListener { _, value, _ ->
                val configValue = config.indexToValue(value)
                if (configValue != null) {
                    listener.onPrivacyConfigChanged(
                        config,
                        configValue
                    )
                }
            }
            // enable, if location-access is enabled
            dataBinding.locationConfigSlider.isEnabled = hasLocationAccess
            dataBinding.locationConfigSlider.visibility = View.VISIBLE
        }
    }

    interface LocationPrivacyConfigAdapterListener {
        fun onPrivacyConfigChanged(config: LocationPrivacyConfig, value: Int)
        fun getPrivacyConfigValue(config: LocationPrivacyConfig): Int
        fun refreshRecyclerView()
    }

    companion object {
        val diffCallback: DiffUtil.ItemCallback<LocationPrivacyConfig> = object : DiffUtil.ItemCallback<LocationPrivacyConfig>() {
            override fun areItemsTheSame(oldItem: LocationPrivacyConfig, newItem: LocationPrivacyConfig): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: LocationPrivacyConfig, newItem: LocationPrivacyConfig): Boolean =
                oldItem == newItem
        }
    }
}