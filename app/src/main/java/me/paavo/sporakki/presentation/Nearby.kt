package me.paavo.sporakki.presentation

import android.location.Location
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.apollographql.apollo3.cache.normalized.watch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.paavo.sporakki.NearbyStopsQuery
import me.paavo.sporakki.presentation.theme.SporakkiTheme
import me.paavo.sporakki.type.Mode
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import androidx.compose.ui.graphics.Color as ComposeColor

enum class StopType {
    Tram,
    Bus,
    Metro,
    Rail,
    Unknown;

    val config get(): StopTypeConfig = when (this) {
        Tram -> StopTypeConfig("Tram", Icons.Filled.Tram, COLOR_TRAM)
        Bus -> StopTypeConfig("Bus", Icons.Filled.DirectionsBus, COLOR_BUS)
        Metro -> StopTypeConfig("Metro", Icons.Filled.Subway, COLOR_METRO)
        Rail -> StopTypeConfig("Rail", Icons.Filled.DirectionsRailway, COLOR_RAIL)
        Unknown -> StopTypeConfig("Unknown", Icons.Filled.QuestionMark, COLOR_UNKNOWN)
    }
}

class StopTime(val name: String, val timeToArrivalMinutes: Long);

class Stop(val stopType: StopType, val id: String, val name: String, val distance: Int, val stopTimes: List<StopTime>)

// https://tyyliopas.hsl.fi/d/h8JR9dHeqfgd/braendi#/visuaalinen-ilme/vaerit
val COLOR_BUS = ComposeColor(0, 122, 201)
val COLOR_TRAM = ComposeColor(0, 152, 95)
val COLOR_METRO = ComposeColor(255, 99, 25)
val COLOR_RAIL = ComposeColor(140, 71, 153)
val COLOR_UNKNOWN = ComposeColor(240, 146, 205)

class StopTypeConfig(val name: String, val icon: ImageVector, val color: ComposeColor)

@Composable
public fun Nearby(navController: NavHostController, state: State<UiState>, isRefreshing: Boolean, onRefresh: () -> Unit) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = onRefresh
        ) {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp)
                    .focusable()
                    .focusRequester(focusRequester),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = "Nearby",
                        textAlign = TextAlign.Center,
                    )
                }

                when (val value = state.value) {
                    is UiState.Success -> {
                        items(value.stops) { stop ->
                            StopCard(stop)
                        }
                    }
                    is UiState.WaitingForLocation -> {
                        item {
                            Text(text = "Waiting for GPS...", textAlign = TextAlign.Center)
                        }
                    }
                    is UiState.Loading -> {
                        item {
                            Text(text = "Loading...", textAlign = TextAlign.Center)
                        }
                    }
                    is UiState.Error -> {
                        item {
                            Text(text = "Error :(", textAlign = TextAlign.Center)
                        }
                    }

                }
            }
        }
    }
}

sealed class UiState {
    object Loading : UiState()
    object WaitingForLocation : UiState()
    object Error : UiState()
    class Success(val stops: List<Stop>): UiState()
}

@Composable
public fun NearbyView(navController: NavHostController, location: Location?) {
    val context = LocalContext.current
    val paramsState: State<Pair<Location?, LocalDateTime>> = flow {
        while (true) {
            emit(Pair(location, LocalDateTime.now()))
            delay(10_000)
        }
    }.collectAsState(Pair(location, LocalDateTime.now()))


    val flow: Flow<UiState> = remember(paramsState) {
        val (location, time) = paramsState.value
        if (location == null){
            flow { emit(UiState.WaitingForLocation) }
        } else {
            getApolloClient(context)
                .query(
                    NearbyStopsQuery(
                        lat = location.latitude,
                        lon = location.longitude,
                        radius = 500,
                        startTime = time.toEpochSecond(ZoneId.of("Europe/Helsinki").rules.getOffset(time))
                    )
                )
                .watch()
                .map { response ->
                    if (response.hasErrors()) {
                        UiState.Error
                    } else {
                        val stops = response.data?.stopsByRadius?.edges?.mapNotNull { it?.node }
                            ?.mapNotNull {
                                convertStop(
                                    node = it,
                                    now = time,
                                )
                            }.orEmpty()

                        UiState.Success(stops)
                    }
                }.catch {
                    Log.e("WearApp", it.stackTraceToString())
                    emit(UiState.Error)
                }
        }
    }
    
    val state = flow.collectAsState(initial = UiState.Loading)
    Nearby(navController, state, isRefreshing = false, onRefresh = {})
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
        stopTimes = node.stop.stoptimesWithoutPatterns.orEmpty().map {
            val serviceDay = LocalDateTime.ofEpochSecond(
                (it!!.serviceDay as Int).toLong(),
                0,
                ZoneId.of("Europe/Helsinki").rules.getOffset(now)
            )!!
            val arrivalTime =
                serviceDay.plusSeconds((it.realtimeArrival ?: it.scheduledArrival!!).toLong())
            val timeToArrival = ChronoUnit.MINUTES.between(now, arrivalTime)
            StopTime(it.trip?.route?.shortName!!, timeToArrival)
        }.sortedBy { it.timeToArrivalMinutes }
    )
}

@Composable
private fun StopCard(stop: Stop) {
    val config = stop.stopType.config

    TitleCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = config.color,
            endBackgroundColor = config.color,
        ),
        contentColor = ComposeColor.White,
        onClick = { /*TODO*/ },
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = config.icon,
                    contentDescription = config.name,
                    modifier = Modifier
                        .size(CardDefaults.AppImageSize)
                        .wrapContentSize(align = Alignment.Center)
                )
                Text(text = "${stop.name} (${stop.distance}\u00A0m)")
            }
        }
    ) {
        val nextLineStops =
            stop.stopTimes.joinToString("\n") { "${it.name} (${it.timeToArrivalMinutes} min)" }
        Text(nextLineStops)
    }
}

@Composable
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
private fun NearbyPreview() {
    val defaultState = remember { mutableStateOf(UiState.Success(testStops)) }

    SporakkiTheme {
        Nearby(rememberSwipeDismissableNavController(), state = defaultState, isRefreshing = false, onRefresh = {})
    }
}

public val testStops = listOf(
    Stop(StopType.Tram,"foo", "Arabiankatu", 50, listOf(
        StopTime("6", 2),
        StopTime("8", 6),
    )),
    Stop(StopType.Bus,"bar", "Arabia", 220, listOf(
        StopTime("74", 1),
        StopTime("55", 14),
        StopTime("506", 17)
    )),
    Stop(StopType.Tram, "baz","Pyöveli Gunnarsonin Katu", 4500, listOf(
        StopTime("506", 8)
    )),
)