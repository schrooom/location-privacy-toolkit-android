package de.fh.muenster.locationprivacytoolkit

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.PendingIntent
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.*
import android.os.*
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import de.fh.muenster.locationprivacytoolkit.config.LocationPrivacyConfigManager
import de.fh.muenster.locationprivacytoolkit.processors.AccuracyProcessor
import de.fh.muenster.locationprivacytoolkit.processors.IntervalProcessor
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.function.Consumer

class LocationPrivacyToolkit(context: Context): LocationListener {

    private val contextReference: WeakReference<Context>
    private val locationManager: LocationManager
    private var config: LocationPrivacyConfigManager

    private val accuracyProcessor: AccuracyProcessor
    private val intervalProcessor: IntervalProcessor

    private val internalListeners: MutableList<LocationListener> = mutableListOf()
    private val internalPendingIntents: MutableList<PendingIntent> = mutableListOf()

    init {
        contextReference = WeakReference(context)
        locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        config = LocationPrivacyConfigManager(context)

        accuracyProcessor = AccuracyProcessor(context)
        intervalProcessor = IntervalProcessor(context)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun isLocationEnabled(): Boolean {
        return locationManager.isLocationEnabled
    }

    fun isProviderEnabled(provider: String): Boolean {
        return locationManager.isProviderEnabled(provider)
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun getLastKnownLocation(provider: String): Location? {
        val lastLocation = locationManager.getLastKnownLocation(provider) ?: return null
        return processLocation(lastLocation)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun getCurrentLocation(
        provider: String,
        cancellationSignal: CancellationSignal?,
        executor: Executor,
        consumer: Consumer<Location?>
    ) {
        // TODO: locationManager.getCurrentLocation(provider, cancellationSignal, executor, consumer)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun getCurrentLocation(
        provider: String,
        locationRequest: LocationRequest,
        cancellationSignal: CancellationSignal?,
        executor: Executor,
        consumer: Consumer<Location?>
    ) {
        // TODO: locationManager.getCurrentLocation(provider, locationRequest, cancellationSignal, executor, consumer)
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        minTimeMs: Long,
        minDistanceM: Float,
        listener: LocationListener
    ) {
        internalListeners.add(listener)
        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, this)
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        minTimeMs: Long,
        minDistanceM: Float,
        listener: LocationListener,
        looper: Looper?
    ) {
        internalListeners.add(listener)
        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, this, looper)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        minTimeMs: Long,
        minDistanceM: Float,
        executor: Executor,
        listener: LocationListener
    ) {
        internalListeners.add(listener)
        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, executor, this)
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        minTimeMs: Long,
        minDistanceM: Float,
        pendingIntent: PendingIntent
    ) {
        this.internalPendingIntents.add(pendingIntent)
        // TODO: locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, pendingIntent)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        locationRequest: LocationRequest,
        executor: Executor,
        listener: LocationListener
    ) {
        internalListeners.add(listener)
        locationManager.requestLocationUpdates(provider, locationRequest, executor, this)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        locationRequest: LocationRequest,
        pendingIntent: PendingIntent
    ) {
        this.internalPendingIntents.add(pendingIntent)
        // TODO: locationManager.requestLocationUpdates(provider, locationRequest, pendingIntent)
    }

    fun removeUpdates(listener: LocationListener) {
        internalListeners.remove(listener)
        if (internalListeners.isEmpty()) {
            locationManager.removeUpdates(this)
        }
    }

    fun removeUpdates(pendingIntent: PendingIntent) {
        internalPendingIntents.remove(pendingIntent)
        if (internalPendingIntents.isEmpty()) {
            locationManager.removeUpdates(pendingIntent)
        }
    }

    fun processLocation(location: Location?): Location? {
        // pipe location through all processors
        return location
                .let { accuracyProcessor.process(it) }
                .let { intervalProcessor.process(it) }
    }

    // LocationListener

    override fun onLocationChanged(l: Location) {
        val processedLocation = processLocation(l) ?: return
        internalListeners.forEach { it.onLocationChanged(processedLocation) }
        internalPendingIntents.forEach { /* TODO */ }
    }
}