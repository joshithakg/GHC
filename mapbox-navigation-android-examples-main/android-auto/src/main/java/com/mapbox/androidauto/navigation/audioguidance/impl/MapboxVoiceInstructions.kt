package com.mapbox.androidauto.navigation.audioguidance.impl

import androidx.annotation.VisibleForTesting
import com.mapbox.api.directions.v5.models.VoiceInstructions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.core.trip.session.TripSessionStateObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * This class converts [MapboxNavigation] callback streams into [Flow].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MapboxVoiceInstructions(
    val mapboxNavigation: MapboxNavigation
) {
    fun voiceInstructions(): Flow<State> {
        return tripSessionStateFlow()
            .flatMapLatest { tripSessionState ->
                if (tripSessionState == TripSessionState.STARTED) {
                    routesUpdatedResultToVoiceInstructions()
                } else {
                    flowOf(MapboxVoiceInstructionsState(false, null))
                }
            }
    }

    private fun routesUpdatedResultToVoiceInstructions(): Flow<State> {
        return routesFlow()
            .flatMapLatest { routesUpdatedResult ->
                if (routesUpdatedResult.routes.isNotEmpty()) {
                    voiceInstructionsFlow()
                } else {
                    flowOf(MapboxVoiceInstructionsState(false, null))
                }
            }
    }

    private fun tripSessionStateFlow() = channelFlow {
        val tripSessionStateObserver = TripSessionStateObserver { tripSessionState ->
            trySend(tripSessionState)
        }
        mapboxNavigation.registerTripSessionStateObserver(tripSessionStateObserver)
        awaitClose {
            mapboxNavigation.unregisterTripSessionStateObserver(tripSessionStateObserver)
        }
    }

    private fun routesFlow() = channelFlow {
        val routesObserver = RoutesObserver { routesUpdatedResult ->
            trySend(routesUpdatedResult)
        }
        mapboxNavigation.registerRoutesObserver(routesObserver)
        awaitClose {
            mapboxNavigation.unregisterRoutesObserver(routesObserver)
        }
    }

    private fun voiceInstructionsFlow() = channelFlow {
        val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
            trySend(MapboxVoiceInstructionsState(true, voiceInstructions))
        }
        trySend(MapboxVoiceInstructionsState(true, null))
        mapboxNavigation.registerVoiceInstructionsObserver(voiceInstructionsObserver)
        awaitClose {
            mapboxNavigation.unregisterVoiceInstructionsObserver(voiceInstructionsObserver)
            trySend(MapboxVoiceInstructionsState(false, null))
        }
    }

    interface State {
        val isPlayable: Boolean
        val voiceInstructions: VoiceInstructions?
    }
}

@VisibleForTesting
internal data class MapboxVoiceInstructionsState(
    override val isPlayable: Boolean = false,
    override val voiceInstructions: VoiceInstructions? = null
) : MapboxVoiceInstructions.State
