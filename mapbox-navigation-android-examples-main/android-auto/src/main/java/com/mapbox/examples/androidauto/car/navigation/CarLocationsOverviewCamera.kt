package com.mapbox.examples.androidauto.car.navigation

import android.graphics.Rect
import android.location.Location
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions

class CarLocationsOverviewCamera(
    val mapboxNavigation: MapboxNavigation,
    private val initialCameraOptions: CameraOptions = CameraOptions.Builder()
        .zoom(DEFAULT_INITIAL_ZOOM)
        .build()
) : MapboxCarMapObserver {

    internal var mapboxCarMapSurface: MapboxCarMapSurface? = null
        private set
    internal lateinit var navigationCamera: NavigationCamera
        private set
    internal lateinit var viewportDataSource: MapboxNavigationViewportDataSource
        private set
    internal var isLocationInitialized = false
        private set
    private var latestLocation: Location? = null

    private val locationObserver = object : LocationObserver {

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            // Initialize the camera at the current location. The next location will
            // transition into the overview mode.
            latestLocation = locationMatcherResult.enhancedLocation
            viewportDataSource.onLocationChanged(locationMatcherResult.enhancedLocation)
            viewportDataSource.evaluate()
            if (!isLocationInitialized) {
                isLocationInitialized = true
                val instantTransition = NavigationCameraTransitionOptions.Builder()
                    .maxDuration(0)
                    .build()

                navigationCamera.requestNavigationCameraToOverview(stateTransitionOptions = instantTransition)
            }
        }
    }

    override fun loaded(mapboxCarMapSurface: MapboxCarMapSurface) {
        super.loaded(mapboxCarMapSurface)
        this.mapboxCarMapSurface = mapboxCarMapSurface
        logAndroidAuto("LocationsOverviewCamera loaded $mapboxCarMapSurface")

        val mapboxMap = mapboxCarMapSurface.mapSurface.getMapboxMap().also {
            it.setCamera(initialCameraOptions)
        }
        viewportDataSource = MapboxNavigationViewportDataSource(
            mapboxCarMapSurface.mapSurface.getMapboxMap()
        )
        navigationCamera = NavigationCamera(
            mapboxMap,
            mapboxCarMapSurface.mapSurface.camera,
            viewportDataSource
        )

        mapboxNavigation.registerLocationObserver(locationObserver)
    }

    override fun visibleAreaChanged(visibleArea: Rect, edgeInsets: EdgeInsets) {
        super.visibleAreaChanged(visibleArea, edgeInsets)
        logAndroidAuto("LocationsOverviewCamera visibleAreaChanged $visibleArea $edgeInsets")

        viewportDataSource.overviewPadding = EdgeInsets(
            edgeInsets.top + OVERVIEW_PADDING,
            edgeInsets.left + OVERVIEW_PADDING,
            edgeInsets.bottom + OVERVIEW_PADDING,
            edgeInsets.right + OVERVIEW_PADDING
        )

        viewportDataSource.evaluate()
    }

    override fun detached(mapboxCarMapSurface: MapboxCarMapSurface?) {
        super.detached(mapboxCarMapSurface)
        logAndroidAuto("LocationsOverviewCamera detached $mapboxCarMapSurface")

        mapboxNavigation.unregisterLocationObserver(locationObserver)
        this.mapboxCarMapSurface = null
        isLocationInitialized = false
    }

    fun updateWithLocations(points: List<Point>) {
        if (points.isNotEmpty()) {
            logAndroidAuto("LocationsOverviewCamera updateWithLocations")
            viewportDataSource.additionalPointsToFrameForOverview(points)
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }

    private companion object {
        private const val OVERVIEW_PADDING = 15
        const val DEFAULT_INITIAL_ZOOM = 15.0
    }
}
