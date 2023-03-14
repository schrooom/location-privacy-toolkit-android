package de.fh.muenster.locationprivacytoolkit.config

import de.fh.muenster.locationprivacytoolkit.R

enum class LocationPrivacyConfig {
    Access,
    Accuracy,
    Interval,
    Visibility,
    AutoDeletion;

    val titleId: Int
        get() = when(this) {
            Access -> R.string.accessTitle
            Accuracy -> R.string.accuracyTitle
            Interval -> R.string.intervalTitle
            Visibility -> R.string.visibilityTitle
            AutoDeletion -> R.string.autoDeletionTitle
        }

    val descriptionId: Int
        get() = when(this) {
            Access -> R.string.accessDescription
            Accuracy -> R.string.accuracyDescription
            Interval -> R.string.intervalDescription
            Visibility -> R.string.visibilityDescription
            AutoDeletion -> R.string.autoDeletionDescription
        }

    val defaultValue: Int
        get() = when(this) {
            Access -> 0
            Accuracy -> 0
            Interval -> 0
            Visibility -> 0
            AutoDeletion -> 0
        }

    val userInterface: LocationPrivacyConfigInterface
        get() = when(this) {
            Access -> LocationPrivacyConfigInterface.Toggle
            Accuracy -> LocationPrivacyConfigInterface.Slider
            Interval -> LocationPrivacyConfigInterface.Slider
            Visibility -> LocationPrivacyConfigInterface.Slider
            AutoDeletion -> LocationPrivacyConfigInterface.Slider
        }
}

enum class LocationPrivacyConfigInterface {
    Toggle,
    Slider
}