# Location Privacy Toolkit Android &middot; SIMPORT

</br>

[![](https://github.com/schrooom/location-privacy-toolkit-android/blob/main/resources/simport_bmbf_logo.png)](https://simport.net/)

---

Location Privacy Toolkit, that can be included into Android apps. This toolkit is aimed to be used as a replacement for usual location APIs. The goal of this toolkit is to allow users to actively control and monitor, which data is used by an app. Furthermore users can set preferences to the usage of location data using fine-grained control mechanisms. This is part of the [SIMPORT][simport] project.

> **Status**: in development 🛠️

## Contents

- ### [Library Usage](#library-usage)
  - [Architecture](#architecture)
  - [API](#api)
  - [Example](#example)
- ### [License](#license)

## Library Usage

### API

The accessible API of the Location Privacy Toolkit is basically given by the following two classes.

#### LocationPrivacyToolkit

The `LocationPrivacyToolkit` encapsules the system location-service (`LocationManager`) and provides the app with the usual interfaces to request location data. This includes methods to get single location updates, such as the following:

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

#### LocationPrivacyConfigActivity

The `LocationPrivacyConfigActivity` is the central activity, that ultimately controls the output of the `LocationPrivacyToolkit`. This activitiy is designed to be presented to the users of location based services. They can use it to define when, how and which location data is shared with the service. The `LocationPrivacyConfigActivity` is designed to be included into the implementing Android app and can be placed freely to accomodate the apps structure needs.

### Architecture

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
