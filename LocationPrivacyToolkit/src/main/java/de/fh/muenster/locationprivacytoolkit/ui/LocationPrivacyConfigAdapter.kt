package de.fh.muenster.locationprivacytoolkit.ui

import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigInterface
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
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
        fun bindTo(
            config: LocationPrivacyConfig,
        ) {
            dataBinding.locationConfigTitle.text =
                dataBinding.root.context.getString(config.titleId)
            when (config.userInterface) {
                LocationPrivacyConfigInterface.Toggle -> {
                    dataBinding.locationConfigToggleButton.isChecked =
                        listener.getPrivacyConfigValue(config) > 0
                    dataBinding.locationConfigToggleButton.setOnCheckedChangeListener { _, isChecked ->
                        listener.onPrivacyConfigChanged(
                            config,
                            if (isChecked) 1 else 0
                        )
                    }
                    dataBinding.locationConfigToggleButton.visibility = View.VISIBLE
                }
                LocationPrivacyConfigInterface.Slider -> {
                    // TODO: update from-/to-values
                    dataBinding.locationConfigSlider.valueFrom = 0f
                    dataBinding.locationConfigSlider.valueTo = 1000f
                    dataBinding.locationConfigSlider.isTickVisible = true
                    dataBinding.locationConfigSlider.value =
                        listener.getPrivacyConfigValue(config).toFloat()
                    dataBinding.locationConfigSlider.addOnChangeListener { _, value, _ ->
                        listener.onPrivacyConfigChanged(
                            config,
                            value.toInt()
                        )
                    }
                    dataBinding.locationConfigSlider.visibility = View.VISIBLE
                }
            }
        }
    }

    interface LocationPrivacyConfigAdapterListener {
        fun onPrivacyConfigChanged(config: LocationPrivacyConfig, value: Int)
        fun getPrivacyConfigValue(config: LocationPrivacyConfig): Int
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