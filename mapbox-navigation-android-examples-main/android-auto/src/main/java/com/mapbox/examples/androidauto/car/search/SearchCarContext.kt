package com.mapbox.examples.androidauto.car.search

import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.examples.androidauto.car.MainCarContext
import com.mapbox.examples.androidauto.car.preview.CarRouteRequest
import com.mapbox.search.MapboxSearchSdk

/**
 * Contains the dependencies for the search feature.
 */
class SearchCarContext(
    val mainCarContext: MainCarContext
) {
    /** MainCarContext **/
    val carContext = mainCarContext.carContext
    val distanceFormatter = mainCarContext.distanceFormatter

    /** SearchCarContext **/
    val carSearchEngine = CarSearchEngine(
        MapboxSearchSdk.createSearchEngine(),
        MapboxCarApp.carAppServices.location().navigationLocationProvider
    )
    val carRouteRequest = CarRouteRequest(
        mainCarContext.mapboxNavigation,
        MapboxCarApp.carAppServices.location().navigationLocationProvider
    )
}
