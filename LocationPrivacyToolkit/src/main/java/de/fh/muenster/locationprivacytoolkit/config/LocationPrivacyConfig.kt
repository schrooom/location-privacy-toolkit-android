package de.fh.muenster.locationprivacytoolkit.config

import android.content.Context
import androidx.fragment.app.Fragment
import de.fh.muenster.locationprivacytoolkit.LocationPrivacyToolkitListener
import de.fh.muenster.locationprivacytoolkit.R
import de.fh.muenster.locationprivacytoolkit.processors.ui.LocationHistoryFragment
import de.fh.muenster.locationprivacytoolkit.processors.AbstractLocationProcessor
import de.fh.muenster.locationprivacytoolkit.processors.AccessProcessor
import de.fh.muenster.locationprivacytoolkit.processors.AccuracyProcessor
import de.fh.muenster.locationprivacytoolkit.processors.AutoDeletionProcessor
import de.fh.muenster.locationprivacytoolkit.processors.DelayProcessor
import de.fh.muenster.locationprivacytoolkit.processors.ExclusionZoneProcessor
import de.fh.muenster.locationprivacytoolkit.processors.HistoryProcessor
import de.fh.muenster.locationprivacytoolkit.processors.IntervalProcessor
import de.fh.muenster.locationprivacytoolkit.processors.ui.ExclusionZoneFragment

enum class LocationPrivacyConfig {
    Access,
    Accuracy,
    AutoDeletion,
    Delay,
    ExclusionZone,
    History,
    Interval,
    Visibility;

    val titleId: Int
        get() = when (this) {
            Access -> R.string.accessTitle
            Accuracy -> R.string.accuracyTitle
            Delay -> R.string.delayTitle
            ExclusionZone -> R.string.exclusionZoneTitle
            Interval -> R.string.intervalTitle
            Visibility -> R.string.visibilityTitle
            AutoDeletion -> R.string.autoDeletionTitle
            History -> R.string.historyTitle
        }

    val subtitleId: Int
        get() = when (this) {
            Access -> R.string.accessSubtitle
            Accuracy -> R.string.accuracySubtitle
            Delay -> R.string.delaySubtitle
            ExclusionZone -> R.string.exclusionZoneSubtitle
            Interval -> R.string.intervalSubtitle
            Visibility -> R.string.visibilitySubtitle
            AutoDeletion -> R.string.autoDeletionSubtitle
            History -> R.string.historySubtitle
        }

    val descriptionId: Int
        get() = when (this) {
            Access -> R.string.accessDescription
            Accuracy -> R.string.accuracyDescription
            Delay -> R.string.delayDescription
            ExclusionZone -> R.string.exclusionZoneDescription
            Interval -> R.string.intervalDescription
            Visibility -> R.string.visibilityDescription
            AutoDeletion -> R.string.autoDeletionDescription
            History -> R.string.historyDescription
        }

    val defaultValue: Int
        get() = when (this) {
            Access -> 0
            Accuracy -> 0
            Delay -> 0
            ExclusionZone -> 0
            Interval -> 0
            Visibility -> 0
            AutoDeletion -> 0
            History -> 0
        }

    val values: Array<Int>
        get() = when (this) {
            Access -> arrayOf(0, 1)
            Accuracy -> arrayOf(1000, 500, 100, 0)
            Delay -> arrayOf(1000, 300, 60, 10, 0)
            ExclusionZone -> emptyArray()
            Interval -> arrayOf(1000, 600, 60, 0)
            Visibility -> arrayOf(0, 1, 2, 3)
            AutoDeletion -> arrayOf(1000, 600, 60, 0)
            History -> emptyArray()
        }

    val userInterface: LocationPrivacyConfigInterface
        get() = when (this) {
            Access -> LocationPrivacyConfigInterface.Switch
            Accuracy -> LocationPrivacyConfigInterface.Slider
            Delay -> LocationPrivacyConfigInterface.Slider
            ExclusionZone -> LocationPrivacyConfigInterface.Fragment
            Interval -> LocationPrivacyConfigInterface.Slider
            Visibility -> LocationPrivacyConfigInterface.Slider
            AutoDeletion -> LocationPrivacyConfigInterface.Slider
            History -> LocationPrivacyConfigInterface.Fragment
        }

    val range: IntRange
        get() = IntRange(0, values.size - 1)

    val fragment: Fragment?
        get() = when (this) {
            ExclusionZone -> ExclusionZoneFragment()
            History -> LocationHistoryFragment()
            else -> null
        }

    fun getLocationProcessor(
        context: Context,
        listener: LocationPrivacyToolkitListener?
    ): AbstractLocationProcessor? {
        return when (this) {
            Access -> AccessProcessor(context)
            Accuracy -> AccuracyProcessor(context)
            Delay -> DelayProcessor(context)
            ExclusionZone -> ExclusionZoneProcessor(context)
            Interval -> IntervalProcessor(context)
            Visibility -> null
            AutoDeletion -> AutoDeletionProcessor(context, listener)
            History -> HistoryProcessor(context, listener)
        }
    }

    fun formatLabel(value: Int): String {
        return when (this) {
            Access, ExclusionZone, History -> ""
            Accuracy -> "${value}m"
            Interval, Delay, AutoDeletion -> "${value}s"
            Visibility -> {
                when (value) {
                    1 -> "Friends"
                    2 -> "Contacts"
                    3 -> "Everyone"
                    else -> "None"
                }
            }
        }
    }

    fun indexToValue(indexValue: Float): Int? {
        val configValues = this.values
        if (configValues.isNotEmpty()) {
            val index = indexValue.toInt()
            return configValues[index]
        }
        return null
    }

    fun valueToIndex(value: Int): Int? {
        val index = this.values.indexOf(value)
        if (index >= 0) {
            return index
        }
        return null
    }
}

enum class LocationPrivacyConfigInterface {
    Switch,
    Slider,
    Fragment
}