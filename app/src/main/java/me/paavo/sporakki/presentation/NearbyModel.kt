package me.paavo.sporakki.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class StopType {
    Tram,
    Bus,
    Metro,
    Rail,
    Unknown;

    val config
        get(): StopTypeConfig = when (this) {
            Tram -> StopTypeConfig("Tram", Icons.Filled.Tram, COLOR_TRAM)
            Bus -> StopTypeConfig("Bus", Icons.Filled.DirectionsBus, COLOR_BUS)
            Metro -> StopTypeConfig("Metro", Icons.Filled.Subway, COLOR_METRO)
            Rail -> StopTypeConfig("Rail", Icons.Filled.DirectionsRailway, COLOR_RAIL)
            Unknown -> StopTypeConfig("Unknown", Icons.Filled.QuestionMark, COLOR_UNKNOWN)
        }
}

class StopTime(
    val name: String,
    val timeToDeparture: Long,
    val headsign: String? = null,
)

class Stop(
    val stopType: StopType,
    val id: String,
    val name: String,
    val distance: Int,
    val stopTimes: List<StopTime>,
    val platform: String? = null
)