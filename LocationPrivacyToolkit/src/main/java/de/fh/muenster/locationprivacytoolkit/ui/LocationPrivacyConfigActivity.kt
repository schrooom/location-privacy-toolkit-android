package de.fh.muenster.locationprivacytoolkit.ui

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfig
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.databinding.ActivityLocationPrivacyConfigBinding
import de.fh.muenster.locationprivacytoolkit.processors.utils.LocationPrivacyDatabase
import gov.nasa.worldwind.formats.gpx.GpxReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date


class LocationPrivacyConfigActivity : AppCompatActivity(),
    LocationPrivacyConfigAdapter.LocationPrivacyConfigAdapterListener {

    private lateinit var binding: ActivityLocationPrivacyConfigBinding
    private lateinit var configAdapter: LocationPrivacyConfigAdapter
    private lateinit var configManager: LocationPrivacyConfigManager
    private val database by lazy { LocationPrivacyDatabase.sharedInstance(this) }

    private var locationDataResultActivityLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                importExampleLocationData(result)
            }
        }

    @RequiresApi(Build.VERSION_CODES.M)
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
        val dividerItemDecoration = DividerItemDecoration(
            binding.locationConfigRecyclerView.context, RecyclerView.VERTICAL
        )
        binding.locationConfigRecyclerView.addItemDecoration(dividerItemDecoration)

        binding.locationConfigSystemSettingsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        }

        val hasFinePermission = checkSelfPermission(ACCESS_FINE_LOCATION)
        val hasCoarsePermission = checkSelfPermission(ACCESS_COARSE_LOCATION)
        binding.locationConfigSystemAccessValue.text = when (true) {
            (hasFinePermission == PackageManager.PERMISSION_GRANTED) -> "precise"
            (hasCoarsePermission == PackageManager.PERMISSION_GRANTED) -> "coarse"
            (hasCoarsePermission == PackageManager.PERMISSION_DENIED && hasFinePermission == PackageManager.PERMISSION_DENIED) -> "denied"
            else -> "unset"
        }

        val hasBackgroundPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkSelfPermission(ACCESS_BACKGROUND_LOCATION)
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        binding.locationConfigSystemBackgroundValue.text =
            if (hasBackgroundPermission == PackageManager.PERMISSION_GRANTED) {
                "yes"
            } else {
                "no"
            }
        binding.locationConfigShowMoreButton.setOnClickListener {
            toggleMoreMenu()
        }
        binding.locationConfigHideMoreButton.setOnClickListener {
            toggleMoreMenu()
        }
        val usesExampleData = configManager.getUseExampleData()
        binding.locationConfigMoreExampleDataSwitch.isChecked = usesExampleData
        binding.locationConfigImportExampleDataButton.isEnabled = usesExampleData
        binding.locationConfigDeleteExampleDataButton.isEnabled = usesExampleData
        binding.locationConfigMoreExampleDataSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleExampleDataUsage(useExampleData = isChecked)
        }
        binding.locationConfigImportExampleDataButton.setOnClickListener {
            locationDataResultActivityLauncher.launch("application/octet-stream")
        }
        binding.locationConfigDeleteExampleDataButton.setOnClickListener {
            deleteExampleLocationData()
        }
    }

    // LocationPrivacyConfigAdapterListener

    override val fragmentManager: FragmentManager
        get() = supportFragmentManager

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

    private fun toggleMoreMenu() {
        val showMore = binding.locationConfigShowMoreButton.visibility == View.VISIBLE
        if (showMore) {
            binding.locationConfigShowMoreButton.visibility = View.GONE
            binding.locationConfigHideMoreButton.visibility = View.VISIBLE
            binding.locationConfigMoreMenu.visibility = View.VISIBLE
        } else {
            binding.locationConfigHideMoreButton.visibility = View.GONE
            binding.locationConfigShowMoreButton.visibility = View.VISIBLE
            binding.locationConfigMoreMenu.visibility = View.GONE
        }
    }

    private fun toggleExampleDataUsage(useExampleData: Boolean) {
        configManager.setUseExampleData(useExampleData)
        binding.locationConfigImportExampleDataButton.isEnabled = useExampleData
        binding.locationConfigDeleteExampleDataButton.isEnabled = useExampleData
    }

    private fun deleteExampleLocationData() {
        MaterialAlertDialogBuilder(this).setTitle(R.string.systemPermissionDeleteExampleDataDialogTitle)
            .setMessage(R.string.systemPermissionDeleteExampleDataDialogMessage)
            .setNegativeButton(R.string.systemPermissionDeleteExampleDataDialogCancelButton, null)
            .setPositiveButton(R.string.systemPermissionDeleteExampleDataDialogDeleteButton) { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.removeExampleLocations()
                }
            }.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun importExampleLocationData(uri: Uri?) {
        if (uri == null) return
        CoroutineScope(Dispatchers.IO).launch {
            var stream: InputStream? = null
            try {
                stream = contentResolver.openInputStream(uri)
                val gpxReader = GpxReader()
                gpxReader.readStream(stream)
                val locationsToImport = gpxReader.tracks.flatMap { t ->
                    t.segments.flatMap { s ->
                        s.points.mapNotNull { p ->
                            val time = if (p.time != null) Instant.parse(p.time)?.toEpochMilli()
                                ?: 0L else 0L
                            Location("").also {
                                it.time = time
                                it.latitude = p.latitude
                                it.longitude = p.longitude
                                it.altitude = p.elevation
                            }
                        }
                    }
                }.toList().distinctBy { arrayOf(it.time, it.latitude, it.longitude) }
                if (locationsToImport.isNotEmpty()) {
                    database.addExampleLocations(locationsToImport.toList())
                }
                withContext(Dispatchers.Main) {
                    Snackbar.make(
                        binding.root,
                        "imported ${locationsToImport.size} locations",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Snackbar.make(binding.root, "${e.message}", Snackbar.LENGTH_LONG).show()
                }
            } finally {
                stream?.close()
            }
        }
    }
}
