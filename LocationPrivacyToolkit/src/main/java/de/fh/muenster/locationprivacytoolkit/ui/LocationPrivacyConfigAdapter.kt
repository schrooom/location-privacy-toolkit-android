package de.fh.muenster.locationprivacytoolkit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.databinding.ListItemLocationPrivacyConfigBinding

class LocationPrivacyConfigAdapter: ListAdapter<LocationPrivacyConfig, LocationPrivacyConfigAdapter.LocationPrivacyConfigViewHolder>(
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
        return LocationPrivacyConfigViewHolder(dataBinding)
    }

    override fun onBindViewHolder(holder: LocationPrivacyConfigViewHolder, position: Int) {
        getItem(position)?.let { configKey ->
            holder.bindTo(configKey)
        }
    }


    class LocationPrivacyConfigViewHolder(
        private val dataBinding: ListItemLocationPrivacyConfigBinding
    ) : RecyclerView.ViewHolder(dataBinding.root) {
        fun bindTo(
            configKey: LocationPrivacyConfig,
        ) {
            dataBinding.locationConfigTitle.text = dataBinding.root.context.getString(configKey.titleId)
        }
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