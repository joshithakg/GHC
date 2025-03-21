package com.mapbox.androidauto.car.map.impl

import android.graphics.Rect
import androidx.car.app.AppManager
import androidx.car.app.CarContext
import androidx.car.app.SurfaceCallback
import androidx.car.app.SurfaceContainer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.androidauto.MapboxCarApp
import com.mapbox.androidauto.car.map.MapboxCarMap
import com.mapbox.androidauto.car.map.MapboxCarMapObserver
import com.mapbox.androidauto.car.map.MapboxCarMapSurface
import com.mapbox.androidauto.logAndroidAuto
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapSurface
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener

/**
 * @see MapboxCarMap to create new map experiences.
 *
 * This class combines Android Auto screen lifecycle events
 * with SurfaceCallback lifecycle events. It then
 * sets the [CarMapSurfaceSession] which allows us to register onto
 * our own [MapboxCarMapObserver]
 */
internal class CarMapLifecycleObserver internal constructor(
    private val carContext: CarContext,
    private val carMapSurfaceSession: CarMapSurfaceSession,
    private val mapInitOptions: MapInitOptions
) : DefaultLifecycleObserver, SurfaceCallback {

    private var mapStyleUri: String

    init {
        mapStyleUri = if (carContext.isDarkMode) {
            MapboxCarApp.options.mapNightStyle ?: MapboxCarApp.options.mapDayStyle
        } else {
            MapboxCarApp.options.mapDayStyle
        }
    }

    /** Screen lifecycle events */

    override fun onCreate(owner: LifecycleOwner) {
        logAndroidAuto("CarMapSurfaceLifecycle Request surface")
        carContext.getCarService(AppManager::class.java)
            .setSurfaceCallback(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        logAndroidAuto("CarMapSurfaceLifecycle onStart")
    }

    override fun onStop(owner: LifecycleOwner) {
        logAndroidAuto("CarMapSurfaceLifecycle onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        logAndroidAuto("CarMapSurfaceLifecycle onDestroy")
    }

    /** Surface lifecycle events */

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        logAndroidAuto("CarMapSurfaceLifecycle Surface available $surfaceContainer")
        surfaceContainer.surface?.let {
            val mapSurface = MapSurface(carContext, it, mapInitOptions)
            mapSurface.onStart()
            mapSurface.surfaceCreated()
            mapSurface.getMapboxMap().loadStyleUri(
                mapStyleUri,
                onStyleLoaded = { style ->
                    logAndroidAuto("CarMapSurfaceLifecycle styleAvailable")
                    mapSurface.surfaceChanged(surfaceContainer.width, surfaceContainer.height)
                    val carMapSurface = MapboxCarMapSurface(carContext, mapSurface, surfaceContainer, style)
                    carMapSurfaceSession.carMapSurfaceAvailable(carMapSurface)
                },
                onMapLoadErrorListener = object : OnMapLoadErrorListener {
                    override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                        logAndroidAuto(
                            "CarMapSurfaceLifecycle updateMapStyle onMapLoadError " +
                                "${eventData.type} ${eventData.message}"
                        )
                    }
                }
            )
        }
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        logAndroidAuto("CarMapSurfaceLifecycle Visible area changed visibleArea:$visibleArea")
        carMapSurfaceSession.surfaceVisibleAreaChanged(visibleArea)
    }

    override fun onStableAreaChanged(stableArea: Rect) {
        // Have not found a need for this.
        // logAndroidAuto("CarMapSurfaceLifecycle Stable area changed stable:$stableArea")
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        logAndroidAuto("CarMapSurfaceLifecycle Surface destroyed")
        carMapSurfaceSession.carMapSurfaceDestroyed()
    }

    /** Map modifiers */

    fun updateMapStyle(mapStyle: String) {
        if (this.mapStyleUri == mapStyle) return
        this.mapStyleUri = mapStyle

        logAndroidAuto("CarMapSurfaceLifecycle updateMapStyle $mapStyle")
        val previousCarMapSurface = carMapSurfaceSession.mapboxCarMapSurface
        val mapSurface = previousCarMapSurface?.mapSurface
        mapSurface?.getMapboxMap()?.loadStyleUri(
            mapStyle,
            onStyleLoaded = { style ->
                logAndroidAuto("CarMapSurfaceLifecycle updateMapStyle styleAvailable")
                val carMapSurface = MapboxCarMapSurface(
                    carContext,
                    mapSurface,
                    previousCarMapSurface.surfaceContainer,
                    style,
                )
                carMapSurfaceSession.carMapSurfaceAvailable(carMapSurface)
            },
            onMapLoadErrorListener = object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    logAndroidAuto(
                        "CarMapSurfaceLifecycle updateMapStyle onMapLoadError ${eventData.type} ${eventData.message}",
                    )
                }
            }
        )
    }
}
