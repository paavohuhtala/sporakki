/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package me.paavo.sporakki.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import me.paavo.sporakki.presentation.theme.SporakkiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

const val VIEW_HOME = "home"
const val VIEW_NEARBY = "nearby"


@Composable
fun WearApp() {
    val context = LocalContext.current
    val locationManager =  LocalContext.current.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val locationGrantState = remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                locationGrantState.value = true
                Log.d("WearApp", "Granted")
            } else {
                Log.d("WearApp", "Permission denied")
            }
        }

    val nearbyViewModel: NearbyViewModel = viewModel()

    LaunchedEffect("start") {
        when (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )) {
            PackageManager.PERMISSION_GRANTED -> {
                Log.d("WearApp", "location grant true")
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                locationGrantState.value = true
                if (lastKnownLocation != null) {
                    nearbyViewModel.updateLocation(lastKnownLocation)
                }
            }
            else -> {
                Log.d("WearApp", "asking for permission")
                launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }


    DisposableEffect(locationGrantState.value) {
        val listener = LocationListener { location ->
            Log.d("WearApp", "Location update! $location")
            nearbyViewModel.updateLocation(location)
        }

        if (locationGrantState.value) {
            Log.d("WearApp", "Requesting location updates...")
            locationManager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                10_000,
                0.0F,
                listener,
                Looper.getMainLooper()
            )
        }

        onDispose {
            locationManager.removeUpdates(listener)
        }
    }

    val navController = rememberSwipeDismissableNavController()

    SporakkiTheme {
        SwipeDismissableNavHost(navController = navController, startDestination = VIEW_HOME) {
            composable(VIEW_HOME) {

                Home(
                    onNavigateToNearby = { navController.navigate(VIEW_NEARBY) },
                    onNavigateToFavorites = { }
                )
            }
            composable(VIEW_NEARBY) {
                NearbyView(navController, nearbyViewModel)
            }
        }
    }
}


@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}