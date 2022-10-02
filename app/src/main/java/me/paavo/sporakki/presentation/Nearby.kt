package me.paavo.sporakki.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import me.paavo.sporakki.presentation.theme.SporakkiTheme
import androidx.compose.ui.graphics.Color as ComposeColor

// https://tyyliopas.hsl.fi/d/h8JR9dHeqfgd/braendi#/visuaalinen-ilme/vaerit
val COLOR_BUS = ComposeColor(0, 122, 201)
val COLOR_TRAM = ComposeColor(0, 152, 95)
val COLOR_METRO = ComposeColor(255, 99, 25)
val COLOR_RAIL = ComposeColor(140, 71, 153)
val COLOR_UNKNOWN = ComposeColor(240, 146, 205)

class StopTypeConfig(val name: String, val icon: ImageVector, val color: ComposeColor)

@Composable
fun Nearby(
    navController: NavHostController,
    state: State<NearbyUiState>,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        ScalingLazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            scalingParams = ScalingLazyColumnDefaults.scalingParams(
                minElementHeight = 0.1f,
                minTransitionArea = 0.2f,
            )
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
                is NearbyUiState.Loaded -> {
                    items(value.stops, key = { it.id }) { stop ->
                        StopCard(stop)
                    }
                }
                is NearbyUiState.WaitingForLocation -> {
                    item {
                        Text(text = "Waiting for location...", textAlign = TextAlign.Center)
                    }
                }
                is NearbyUiState.Loading -> {
                    item {
                        Text(text = "Loading...", textAlign = TextAlign.Center)
                    }
                }
                is NearbyUiState.Error -> {
                    item {
                        Text(text = "Error :(", textAlign = TextAlign.Center)
                    }
                }

            }
            
            item {
                Spacer(modifier = Modifier.height(32.0.dp))
            }
        }
    }
}

sealed class NearbyUiState {
    object Loading : NearbyUiState()
    object WaitingForLocation : NearbyUiState()
    object Error : NearbyUiState()
    class Loaded(val stops: List<Stop>) : NearbyUiState()
}

@Composable
fun NearbyView(navController: NavHostController, nearbyViewModel: NearbyViewModel) {
    val state = nearbyViewModel.nearbyStops()
    Nearby(navController, state)
}

@Composable
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
private fun NearbyPreview() {
    val defaultState = remember { mutableStateOf(NearbyUiState.Loaded(testStops)) }

    SporakkiTheme {
        Nearby(
            rememberSwipeDismissableNavController(),
            state = defaultState
        )
    }
}

