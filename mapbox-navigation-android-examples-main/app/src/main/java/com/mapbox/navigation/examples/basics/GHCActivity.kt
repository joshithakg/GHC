package com.mapbox.navigation.examples.basics

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.res.Configuration
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.MapboxReplayer
import com.mapbox.navigation.core.replay.ReplayLocationEngine
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.databinding.ActivityGhcactivityBinding;
import com.mapbox.navigation.ui.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.*
import java.io.IOException
import android.content.DialogInterface
import android.widget.AdapterView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager


data class Hospitals(
    var hospitals: List<Hospital>? = null
)
data class Hospital (
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    //val phone: Long,
    val distance: Double
)
data class Commute (
    var commute: List<Alert>? = null
)

data class Alert (
    val commuteid: String,
    val latcurr: Double,
    val lngcurr: Double,
    val distance: Double
)

class GHCActivity : AppCompatActivity() {
    private lateinit var builder : AlertDialog.Builder
    private lateinit var binding: ActivityGhcactivityBinding
    private val addedWaypoints = Waypoints()
   // private val originPoint: Point;
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView
    private val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var navigationCamera: NavigationCamera
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private lateinit var tripProgressApi: MapboxTripProgressApi
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private lateinit var circleAnnotationManager: CircleAnnotationManager
    private lateinit var circleAnnotionOptions : CircleAnnotationOptions
    private var isRouteInProgress: Boolean = false
    private var isUpdateInProgress: Boolean = false
    private var commuteId: String = ""
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeOverviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            20.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val landscapeFollowingPadding: EdgeInsets by lazy {
        EdgeInsets(
            30.0 * pixelDensity,
            380.0 * pixelDensity,
            110.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()
    private lateinit var routeArrowView: MapboxRouteArrowView
    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
    }

    private val mapboxReplayer = MapboxReplayer()
    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)
    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
    //private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)

    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.routes.isNotEmpty()) {
            // log maneuvers to see the named waypoints
            // logManeuvers(routeUpdateResult.routes.first())

            // generate route geometries and render them
            val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }
            routeLineApi.setRoutes(
                routeLines
            ) { value ->
                mapboxMap.getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }
            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.routes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = mapboxMap.getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
            }
        }
    }

    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
            println("Lat1: "+rawLocation.latitude)
            println("Lng1: "+rawLocation.longitude)
            val urlString = "http://cgworkspace.cytogenie.org/ghc?op=update&latcurr="
                .plus(rawLocation.latitude).plus("&lngcurr=").plus(rawLocation.longitude)
                .plus("&commuteid=").plus(commuteId)
            println(urlString)
            runUpdate(urlString,builder)
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
           // println(locationMatcherResult.enhancedLocation.latitude)
           // println(locationMatcherResult.enhancedLocation.longitude)
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )
            // if this is the first location update the activity has received,
            // then move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                moveCameraTo(enhancedLocation)
            }

            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                navigationCamera.requestNavigationCameraToOverview(
                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                        .maxDuration(0) // instant transition
                        .build()
                )
            }
        }

        private fun moveCameraTo(location: Location) {
            val mapAnimationOptions = MapAnimationOptions.Builder().duration(0).build()
            binding.mapView.camera.easeTo(
                CameraOptions.Builder()
                    // Centers the camera to the lng/lat specified.
                    .center(Point.fromLngLat(location.longitude, location.latitude))
                    // specifies the zoom value. Increase or decrease to zoom in or zoom out
                    .zoom(15.0)
                    .build(),
                mapAnimationOptions
            )
        }
    }

    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        viewportDataSource.onRouteProgressChanged(routeProgress)
        viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = mapboxMap.getStyle()
        if (style != null) {
            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
       // println(routeProgress.route.routeOptions()!!.coordinates());
        // update bottom trip progress summary
        binding.tripProgressView.render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }

    fun run(url: String, builder: AlertDialog.Builder, originPoint: Point) {
        //val ctx=    this.applicationContext
        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val v = e.message.toString()
                println(v)
            }
            override fun onResponse(call: Call, response: Response) {
                val r = response.body?.string()
                println(r)
                val gson = Gson()
                val hospitals = gson.fromJson(r, Hospitals::class.java)
                val name1= hospitals.hospitals?.get(0)?.name;
                val distance1= hospitals.hospitals?.get(0)?.distance;
                println(name1)
                val namesKms: MutableList<String> = mutableListOf()
                hospitals.hospitals?.forEach {it->
                    val item= it.name.plus(" in ").plus(it.distance).plus(" kilometers")
                    namesKms.add(item)
                }
               builder.setSingleChoiceItems(
                    namesKms.toTypedArray(),
                    -1
                ){dialog, i ->}
                builder.setPositiveButton("Submit"){dialog,which->
                    val position = (dialog as AlertDialog).listView.checkedItemPosition
                    if (position !=-1){
                        val selectedItem = namesKms.get(position)
                        // Toast.makeText(this@GHCActivity,"You selected ".plus(selectedItem),Toast.LENGTH_SHORT).show();
                        //textView.text = "Favorite color : $selectedItem"
                        var lt: Double = (hospitals.hospitals?.get(position))?.lat!!;
                        val lg: Double? =hospitals.hospitals?.get(position)?.lng;
                        val dist: Double? =hospitals.hospitals?.get(position)?.distance;
                        val urlString = "http://cgworkspace.cytogenie.org/ghc?op=new&latstart="
                        .plus(originPoint.latitude()).plus("&lngstart=").plus(originPoint.longitude())
                        .plus("&latend=").plus(lt).plus("&lngend=").plus(lg)
                        .plus("&dest=").plus(selectedItem)
                        runNew(urlString,builder)
                        val destinationPoint = Point.fromLngLat(lg!!, lt!!);
                        addWaypoint(originPoint, destinationPoint);
                    }
                }

                // alert dialog other buttons
                builder.setNegativeButton("No",null)
                builder.setNeutralButton("Cancel",null)

                // set dialog non cancelable
                builder.setCancelable(false)
                runOnUiThread { object :Runnable{

                        override fun run() {

                        }
                }
                    try {
                        println("Hello3")
                        val dialog = builder.create()
                        dialog?.show()
                        // initially disable the positive button
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

                        // dialog list item click listener
                        dialog.listView.onItemClickListener =
                            AdapterView.OnItemClickListener { parent, view, position, id ->
                                // enable positive button when user select an item
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .isEnabled = position != -1
                            }
                    }
                    catch (e: Throwable) {
                        // handler
                        e.printStackTrace()
                        val ff = e.message
                    } finally {
                        // optional finally block
                    }
                }

            }
        })
    }

    fun runAlert(url: String, builder: AlertDialog.Builder, originPoint: Point) {
        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val v = e.message.toString()
                println(v)
            }
            override fun onResponse(call: Call, response: Response) {
                val r = response.body?.string()
                println(r)
                val gson = Gson()
                val commute = gson.fromJson(r, Commute::class.java)
                if (commute.commute!!.isNotEmpty()) {
                    val dist= commute.commute?.get(0)?.distance;
                    runOnUiThread {
                        Toast.makeText(this@GHCActivity,
                            "You have an ambulance approaching in $dist kilometers",Toast.LENGTH_SHORT).show();
                       }
                }
                else {
                    runOnUiThread {
                        Toast.makeText(this@GHCActivity,"No ambulance approaching you",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        })
    }

    fun runNew(url: String, builder: AlertDialog.Builder) {
        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val v = e.message.toString()
                println(v)
            }
            override fun onResponse(call: Call, response: Response) {
                val r = response.body?.string()
                if (r != null) {
                    commuteId = r
                }
                println(commuteId)
            }
        })
    }

    fun runUpdate(url: String, builder: AlertDialog.Builder) {
        if (isUpdateInProgress) {
            return;
        }
        isUpdateInProgress = true;
        val request = Request.Builder()
            .url(url)
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val v = e.message.toString()
                println(v)
                isUpdateInProgress = false
            }
            override fun onResponse(call: Call, response: Response) {
                val r = response.body?.string()
                println(r)
                isUpdateInProgress = false
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_ghcactivity)
        binding = ActivityGhcactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapboxMap = binding.mapView.getMapboxMap()
        circleAnnotationManager = binding.mapView.annotations.createCircleAnnotationManager(binding.mapView)
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this.applicationContext)
                    .accessToken(getString(R.string.mapbox_access_token))
                    // comment out the location engine setting block to disable simulation
                    .locationEngine(replayLocationEngine)
                    .build()
            )
        }

        // initialize Navigation Camera
        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        navigationCamera = NavigationCamera(
            mapboxMap,
            binding.mapView.camera,
            viewportDataSource
        )
        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        binding.mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
            // shows/hide the recenter button depending on the camera state
            when (navigationCameraState) {
                NavigationCameraState.TRANSITION_TO_FOLLOWING,
                NavigationCameraState.FOLLOWING -> binding.recenter.visibility = View.INVISIBLE
                NavigationCameraState.TRANSITION_TO_OVERVIEW,
                NavigationCameraState.OVERVIEW,
                NavigationCameraState.IDLE -> binding.recenter.visibility = View.VISIBLE
            }
        }
        // set the padding values depending on screen orientation and visible view layout
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.overviewPadding = landscapeOverviewPadding
        } else {
            viewportDataSource.overviewPadding = overviewPadding
        }
        if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewportDataSource.followingPadding = landscapeFollowingPadding
        } else {
            viewportDataSource.followingPadding = followingPadding
        }

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = mapboxNavigation.navigationOptions.distanceFormatterOptions
        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(this)
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(this)
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(this, TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

// initialize route line, the withRouteLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
        val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(this).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        // initialize Mapbox Navigation
       /* mapboxNavigation = MapboxNavigationProvider.create(
            NavigationOptions.Builder(this.applicationContext)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )*/
       // mapboxNavigation.startTripSession(withForegroundService = false)

        // initialize route line, the withRouteLineBelowLayerId is specified to place
        // the route line below road labels layer on the map
        // the value of this option will depend on the style that you are using
        // and under which layer the route line should be placed on the map layers stack
      /*  val mapboxRouteLineOptions = MapboxRouteLineOptions.Builder(this)
            .withRouteLineBelowLayerId("road-label")
            .build()
        routeLineApi = MapboxRouteLineApi(mapboxRouteLineOptions)
        routeLineView = MapboxRouteLineView(mapboxRouteLineOptions)*/
        builder = AlertDialog.Builder(this@GHCActivity)
        // load map style
        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            // add long click listener that search for a route to the clicked destination
            binding.mapView.gestures.addOnMapLongClickListener { point ->
                //addWaypoint(point)
                if (isRouteInProgress) {
                    circleAnnotionOptions = CircleAnnotationOptions()
                        .withPoint(point)
                        .withCircleColor("#ee4e8b")
                        .withCircleRadius(8.0)
                        .withCircleStrokeWidth(2.0)
                        .withCircleStrokeColor("#ffffff")
                    circleAnnotationManager.create(circleAnnotionOptions)
                    val urlString = "http://cgworkspace.cytogenie.org/ghc?op=alert&lat="
                        .plus(point.latitude()).plus("&lng=").plus(point.longitude());
                    runAlert(urlString, builder, point);
                }
                else {
                    isRouteInProgress = true;
                    showNearByHospitals(point, builder)

                }
                true
            }
        }

        // initialize view interactions
        binding.stop.setOnClickListener {
            clearRouteAndStopNavigation()
        }
        binding.recenter.setOnClickListener {
            navigationCamera.requestNavigationCameraToFollowing()
            binding.routeOverview.showTextAndExtend(GHCActivity.BUTTON_ANIMATION_DURATION)
        }
        binding.routeOverview.setOnClickListener {
            navigationCamera.requestNavigationCameraToOverview()
            binding.recenter.showTextAndExtend(GHCActivity.BUTTON_ANIMATION_DURATION)
        }

        // start the trip session to being receiving location updates in free drive
        // and later when a route is set also receiving route progress updates
        mapboxNavigation.startTripSession()
    }

    override fun onStart() {
        super.onStart()
        mapboxNavigation.registerRoutesObserver(routesObserver)
        mapboxNavigation.registerLocationObserver(locationObserver)
        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
       // mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
        if (mapboxNavigation.getRoutes().isEmpty()) {
            // if simulation is enabled (ReplayLocationEngine set to NavigationOptions)
            // but we're not simulating yet,
            // push a single location sample to establish origin
            mapboxReplayer.pushEvents(
                listOf(
                    ReplayRouteMapper.mapToUpdateLocation(
                        eventTimestamp = 0.0,
                        point = Point.fromLngLat(77.7247, 12.9672)
                    )
                )
            )
            mapboxReplayer.playFirstLocation()
        }
    }

    override fun onStop() {
        super.onStop()
        // unregister event listeners to prevent leaks or unnecessary resource consumption
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxNavigation.onDestroy()
        MapboxNavigationProvider.destroy()
        mapboxReplayer.finish()
        routeLineApi.cancel()
        routeLineView.cancel()
    }
    private fun showNearByHospitals(originPoint: Point, builder: AlertDialog.Builder ) {
        val urlString = "http://cgworkspace.cytogenie.org/ghc?op=hosp&lat="
            .plus(originPoint.latitude()).plus("&lng=").plus(originPoint.longitude());
        //this.runOnUiThread(java.lang.Runnable { })
        run(urlString, builder, originPoint);

    }

    private fun addWaypoint(originPoint: Point, destination: Point) {

      //  val originPoint = Point.fromLngLat(77.7247, 12.9672)
      /*  val originLocation = navigationLocationProvider.lastLocation
        val originPoint = originLocation?.let {
            Point.fromLngLat(it.longitude, it.latitude)
        } ?: return
*/
        // we always start a route from the current location
        //if (addedWaypoints.isEmpty) {
          //  addedWaypoints.addRegular(originPoint)
        //}
        addedWaypoints.addRegular(originPoint)
        addedWaypoints.addRegular(destination)

        mapboxNavigation.requestRoutes(
            RouteOptions.builder()
                .applyDefaultNavigationOptions()
                .applyLanguageAndVoiceUnitOptions(this)
                .coordinatesList(addedWaypoints.coordinatesList())
                .waypointIndicesList(addedWaypoints.waypointsIndices())
                // .waypointNamesList(addedWaypoints.waypointsNames())
                .bearingsList(
                    listOf(
                        Bearing.builder()
                           // .angle(originLocation.bearing.toDouble())
                            .degrees(45.0)
                            .build(),
                        null
                    )
                )
                .layersList(listOf(mapboxNavigation.getZLevel(), null))
                .build(),
            object : RouterCallback {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    //setRoute(routes)
                    setRouteAndStartNavigation(routes)
                }

                override fun onFailure(
                    reasons: List<RouterFailure>,
                    routeOptions: RouteOptions
                ) {
                    // no impl
                }

                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
                    // no impl
                }
            }
        )
    }
    private fun setRoute(routes: List<DirectionsRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setRoutes(routes)

        // show the "Reset the route" button
        /*binding.multipleWaypointResetRouteButton.apply {
            show()
            setOnClickListener {
                resetCurrentRoute()
                hide()
            }
        }

        binding.multipleWaypointStartRouteButton.apply {
            show()
            setOnClickListener {
                if (mapboxNavigation.getRoutes().isEmpty()) {
                    // if simulation is enabled (ReplayLocationEngine set to NavigationOptions)
                    // but we're not simulating yet,
                    // push a single location sample to establish origin
                    mapboxReplayer.pushEvents(
                        listOf(
                            ReplayRouteMapper.mapToUpdateLocation(
                                eventTimestamp = 0.0,
                                point = Point.fromLngLat(-122.39726512303575, 37.785128345296805)
                              //  point = addedWaypoints.getFirst().point //Point.fromLngLat(-122.39726512303575, 37.785128345296805)
                            )
                        )
                    )
                    mapboxReplayer.playFirstLocation()
                }
                hide()
            }
        }*/
    }

    // Resets the current route
    private fun resetCurrentRoute() {
        if (mapboxNavigation.getRoutes().isNotEmpty()) {
            mapboxNavigation.setRoutes(emptyList()) // reset route
            addedWaypoints.clear() // reset stored waypoints
        }
    }

    private fun setRouteAndStartNavigation(routes: List<DirectionsRoute>) {
        // set routes, where the first route in the list is the primary route that
        // will be used for active guidance
        mapboxNavigation.setRoutes(routes)

        // start location simulation along the primary route
        startSimulation(routes.first())

        // show UI elements
        binding.routeOverview.visibility = View.VISIBLE
        binding.tripProgressCard.visibility = View.VISIBLE

        // move the camera to overview when new route is available
        navigationCamera.requestNavigationCameraToOverview()
    }

    private fun clearRouteAndStopNavigation() {
        // clear
        mapboxNavigation.setRoutes(listOf())

        // stop simulation
        mapboxReplayer.stop()

        // hide UI elements
        binding.routeOverview.visibility = View.INVISIBLE
        binding.tripProgressCard.visibility = View.INVISIBLE
    }

    private fun startSimulation(route: DirectionsRoute) {
       val js= route.toJson();
        mapboxReplayer.run {
            stop()
            clearEvents()
            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
            pushEvents(replayEvents)
            seekTo(replayEvents.first())
            play()
        }
    }
}

class Waypoints {

    private val waypoints = mutableListOf<Waypoint>()

    val isEmpty get() = waypoints.isEmpty()

    fun addRegular(point: Point) {
        waypoints.add(Waypoint(point))
    }
    fun clear() {
        waypoints.clear()
    }

    fun waypointsIndices(): List<Int> {
        return waypoints.mapIndexedNotNull { index, _ ->
            index
        }
    }

    fun coordinatesList(): List<Point> {
        return waypoints.map { it.point }
    }

    fun getFirst(): Waypoint {
        return waypoints.get(0);
    }

}

data class Waypoint(val point: Point)