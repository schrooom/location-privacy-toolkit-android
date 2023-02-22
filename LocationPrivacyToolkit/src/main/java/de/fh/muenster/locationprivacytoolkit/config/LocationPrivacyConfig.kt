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
}
