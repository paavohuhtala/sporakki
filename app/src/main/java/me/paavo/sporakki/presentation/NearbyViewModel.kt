package me.paavo.sporakki.presentation

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.AndroidViewModel
import com.apollographql.apollo3.cache.normalized.watch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import me.paavo.sporakki.NearbyStopsQuery
import me.paavo.sporakki.type.Mode
import java.lang.Long.max
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun timerFlow(time: Long): Flow<Unit> = flow {
    while (currentCoroutineContext().isActive) {
        emit(Unit)
        delay(time)
    }
}

class NearbyViewModel(application: Application) : AndroidViewModel(application) {
    private val currentLocation: MutableStateFlow<Location?> = MutableStateFlow(null)
    private val apolloClient = getApolloClient(application.applicationContext)
    private var refreshCounter = 0
    private val refreshFlow = MutableStateFlow(refreshCounter)

    fun refresh() {
        refreshFlow.value = ++refreshCounter
    }

    fun updateLocation(location: Location) {
        Log.d("NearbyViewModel", "Location updated: $location")
        currentLocation.value = location
    }

    private val paramsFlow: Flow<Location> =
        merge(timerFlow(10_000), refreshFlow)
            .withLatestFrom(currentLocation.filterNotNull()) { _, location -> location }

    private val nearbyStopsFlow = paramsFlow.flatMapLatest { location ->
        val now = LocalDateTime.now()

        val query = NearbyStopsQuery(
            lat = location.latitude,
            lon = location.longitude,
            startTime = now.toEpochSecond(getHelsinkiOffset(now)),
            radius = 500,
        )

        Log.d("NearbyViewModel", "Querying stops near ${location.latitude}, ${location.longitude}")

        apolloClient.query(query).watch().map { response ->
            Log.d("NearbyViewModel", "Got response: $response")
            if (response.hasErrors()) {
                NearbyUiState.Error
            } else {
                val stops = response.data?.stopsByRadius?.edges?.mapNotNull { it?.node }
                    ?.mapNotNull {
                        convertStop(it, now)
                    }.orEmpty()

                NearbyUiState.Loaded(stops)
            }
        }
    }.catch {
        Log.e("WearApp", it.stackTraceToString())
        emit(NearbyUiState.Error)
    }

    @Composable
    fun nearbyStops() = nearbyStopsFlow.collectAsState(initial = NearbyUiState.Loading)
}

private fun convertStop(
    node: NearbyStopsQuery.Node,
    now: LocalDateTime?
): Stop? {
    val stopType = when (node.stop?.vehicleMode) {
        Mode.BUS -> StopType.Bus
        Mode.TRAM -> StopType.Tram
        Mode.SUBWAY -> StopType.Metro
        Mode.RAIL -> StopType.Rail
        else -> StopType.Unknown
    }

    if (node.stop?.stoptimesWithoutPatterns?.isEmpty() == true) {
        return null
    }

    return Stop(
        stopType = stopType,
        id = node.stop!!.gtfsId,
        name = node.stop.name,
        distance = node.distance!!,
        stopTimes = node.stop.stoptimesWithoutPatterns.orEmpty().map { stopTime ->
            val serviceDay = LocalDateTime.ofEpochSecond(
                stopTime!!.serviceDay!!,
                0,
                ZoneId.of("Europe/Helsinki").rules.getOffset(now)
            )!!
            val departureTime =
                serviceDay.plusSeconds((stopTime.realtimeDeparture ?: stopTime.scheduledDeparture!!).toLong())
            val timeToDeparture = max(ChronoUnit.MINUTES.between(now, departureTime), 0)

            StopTime(
                name = stopTime.trip?.route?.shortName!!,
                timeToDeparture = timeToDeparture,
                headsign = stopTime.headsign,
            )
        }.sortedBy { it.timeToDeparture }
    )
}