# Location Privacy Toolkit Android &middot; SIMPORT

</br>

[![](https://github.com/schrooom/location-privacy-toolkit-android/blob/main/resources/simport_bmbf_logo.png)](https://simport.net/)

---

Location Privacy Toolkit, that can be included into Android apps. This toolkit is aimed to be used as a replacement for usual location APIs. The goal of this toolkit is to allow users to actively control and monitor, which data is used by an app. Furthermore users can set preferences to the usage of location data using fine-grained control mechanisms. This is part of the [SIMPORT][simport] project.

> **Status**: prototypical implementation done 🛠️

## Contents

- ### [Library Usage](#library-usage)
  - [Architecture](#architecture)
  - [API](#api)
  - [Example](#example)
- ### [License](#license)

<p align="center">
  <img src="https://github.com/schrooom/location-privacy-toolkit-android/blob/main/resources/general_structure.png" width="75%">
</p>

## Library Usage

### API

The accessible API of the Location Privacy Toolkit is basically given by the following two classes.

#### LocationPrivacyToolkit

The `LocationPrivacyToolkit` encapsules the system location-service – the [Android LocationManager](https://developer.android.com/reference/android/location/LocationManager) – and provides the app with the usual interfaces to request location data. This includes methods to get single location updates, such as the following:

```kt
getLastKnownLocation(…): Location?
getCurrentLocation(…): Location?
```

and also request for continuous location updates by registering a listener including the ability to unregister it later on.

```kt
requestLocationUpdates(…)
```

```kt
removeUpdates(…)
```

Additionally the `Location Service` provides the following helper methods for the access-status.

```kt
isLocationEnabled(): Boolean
isProviderEnabled(provider: String): Boolean
```

#### Location Processors

The core of this toolkit lies in the location processors, which enable users to change the behaviour of the toolkit and how it handles location data. All locations that are requested via this toolkit are processed by those processors. Each processor comes with a user interface (see [LocationPrivacyConfigActivity](#locationprivacyconfigactivity)), thus its behaviour can be directly influenced by users. If for example a user only likes to share coarse locations, they are able to adjust exactly that with the `AccuracyProcessor`.
The `LocationPrivacyToolkit` currently comes with a total of 8 predefined processors.

<p align="center">
  <img src="https://github.com/schrooom/location-privacy-toolkit-android/blob/main/resources/location_processors.png" width="75%">
</p>

| Location Processor |                                       Options (default option is bold)                                       |                                                                                                                        Effect                                                                                                                         |
| ------------------ | :----------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| Access             |                                           <b>Disallow</b><br>Allow                                           |                                                                                                              Blocks or passes locations                                                                                                               |
| Accuracy           |              <b>0 meters (off)</b><br>100 meters<br>500 meters<br>1 kilometers<br>5 kilometers               |                                                                  Changes the accuracy of location into the specified radius. Randomly selects a new location in the given diameter.                                                                   |
| Auto Deletion      |                 <b>0 seconds (off)</b><br>10 seconds<br>1 minute<br>10 minutes<br>30 minutes                 |                                       Automatically delets the location from the integrated location database after given time. The implementing application will also be notified about deletion of locations.                                       |
| Delay              |          <b>0 seconds (off)</b><br>10 seconds<br>30 seconds<br>1 minute<br>5 minutes<br>30 minutes           |                                                                    Delays the users current location with the given time. Therefore an app will receive delayed locations updates.                                                                    |
| Exclusion Zones    | Creation of exclusion zones with a diameter between 100 meters and 10 kilometers.<br><b>No default zones</b> |                                                                             Locations within defined exclusion zones are blocked and not passed onto an implementing app                                                                              |
| History            |                                       Explore and delete location data                                       | The history processor has no active location processing and simply passes on the received location. It does however store each location into the integrated location database and users are able to explore and delete their location data precisely. |
| Interval           |                 <b>0 seconds (off)</b><br>10 seconds<br>1 minute<br>10 minutes<br>30 minutes                 |                                                       Frequency of location updates is altered to the given time. Therefore an app will only receive new locations every n-th seconds/minutes.                                                        |
| Visibility         |                            <b>Everyone (off)</b><br>Contacts<br>Friends<br>Nobody                            |          The history processor has no active location processing and hands over the preference to the implementing app, because the toolkit has no context information. Therefore the app is in charge to comply with the users preference.           |

Additionally developers have the chance to inject external processors into the toolkit to enable specific behaviour for users. This can be done using the following companion object:

```kt
LocationPrivacyToolkit.externalProcessors: MutableList<AbstractExternalLocationProcessor>
```

#### Database

The `LocationPrivacyToolkit` includes a local [Room database](https://developer.android.com/jetpack/androidx/releases/room) that stores all locations, that the toolkit has processed. The user is able to explore and delete that data using the [LocationPrivacyConfigActivity](#locationprivacyconfigactivity).
This enables developers to use this toolkit not only as a replacement for the `LocationManager`, but also as a private location storage where users have full control over their personal location data.

This database can be accessed with the following methods.

```kt
loadAllLocations(): List<Location>
loadLocations(fromTimestamp: Long, toTimestamp: Long): List<Location>
loadLocations(atTimestamp: Long): Location
```

#### LocationPrivacyConfigActivity

The `LocationPrivacyConfigActivity` is the central activity, that ultimately controls the output of the `LocationPrivacyToolkit`. This activitiy is designed to be presented to the users of location based services. They can use it to define when, how and which location data is shared with the service. The `LocationPrivacyConfigActivity` is designed to be included into the implementing Android app and can be placed freely to accomodate the apps structure needs.

##### Themes

Developers that implement this toolkit can alter its look to match their app using themes. For more information take a look at [Android: Styles and themes](https://developer.android.com/develop/ui/views/theming/themes).

The toolkit includes map-views implemented using [MapLibre](https://github.com/maplibre/maplibre-native) within the configuration activity. In order to alter the look of those map, developers can set a map-tiles url – the default value for this url is set to `"https://demotiles.maplibre.org/style.json"`

```kt
LocationPrivacyToolkit.mapTilesUrl: String
```

### Architecture

<p align="center">
  <img src="https://github.com/schrooom/location-privacy-toolkit-android/blob/main/resources/detail_structure.png" width="75%">
</p>

The `LocationPrivacyToolkit` follows this high-level structure.

<p align="center">
  <img src="https://github.com/schrooom/location-privacy-toolkit-android/blob/main/resources/architecture.png" width="85%">
</p>

The architecture used in this `LocationPrivacyToolkit` is inspired by the following work of Mehrnaz Ataei: [_Location Data Privacy : Principles to Practice_](https://doi.org/10.6035/14123.2018.783210).

<p align="center">
  <img src="https://github.com/schrooom/location-privacy-toolkit-android/blob/main/resources/architecture-draft.png" width="60%">
</p>

### Example

In course of the project a sample app was created. The _LocationPrivacyToolkitApp_ is included in this repository and can be seen as an example on how to integrate the `LocationPrivacyToolkit`.

## License

```
SIMPORT Location Privacy Toolkit
Copyright (c) 2023 Sitcom Lab
```

[Further information](LICENSE)

[simport]: https://simport.net/
[git]: https://git-scm.com
