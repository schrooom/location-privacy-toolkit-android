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
import java.lang.ref.WeakReference
import java.util.concurrent.Executor
import java.util.function.Consumer

class LocationPrivacyToolkit {

    private var locationManager: LocationManager
    private val contextReference: WeakReference<Context>

    constructor(context: Context) {
        locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        contextReference = WeakReference(context)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun isLocationEnabled(): Boolean {
        return locationManager.isLocationEnabled
    }

    fun isProviderEnabled(provider: String): Boolean {
        throw RuntimeException("Stub!")
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun getLastKnownLocation(provider: String): Location? {
        return locationManager.getLastKnownLocation(provider)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun getCurrentLocation(
        provider: String,
        cancellationSignal: CancellationSignal?,
        executor: Executor,
        consumer: Consumer<Location?>
    ) {
        locationManager.getCurrentLocation(provider, cancellationSignal, executor, consumer)
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
        locationManager.getCurrentLocation(provider, locationRequest, cancellationSignal, executor, consumer)
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        minTimeMs: Long,
        minDistanceM: Float,
        listener: LocationListener
    ) {
        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, listener)
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        minTimeMs: Long,
        minDistanceM: Float,
        listener: LocationListener,
        looper: Looper?
    ) {
        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, listener, looper)
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
        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, executor, listener)
    }

    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        minTimeMs: Long,
        minDistanceM: Float,
        pendingIntent: PendingIntent
    ) {
        locationManager.requestLocationUpdates(provider, minTimeMs, minDistanceM, pendingIntent)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        locationRequest: LocationRequest,
        executor: Executor,
        listener: LocationListener
    ) {
        locationManager.requestLocationUpdates(provider, locationRequest, executor, listener)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(anyOf = [ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION])
    fun requestLocationUpdates(
        provider: String,
        locationRequest: LocationRequest,
        pendingIntent: PendingIntent
    ) {
        locationManager.requestLocationUpdates(provider, locationRequest, pendingIntent)
    }
}