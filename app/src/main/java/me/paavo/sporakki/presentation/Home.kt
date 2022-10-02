package me.paavo.sporakki.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material.*
import me.paavo.sporakki.presentation.theme.SporakkiTheme

@Composable
public fun Home(onNavigateToNearby: () -> Unit, onNavigateToFavorites: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dp(16.0f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Sporakki",
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dp(16.0f)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onNavigateToNearby) {
                Icon(imageVector = Icons.Filled.Explore, contentDescription = "Nearby stops")
            }
            Button(onClick = onNavigateToFavorites) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Saved stops")
            }
        }
    }
}

@Composable
@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
private fun HomePreview() {
    SporakkiTheme {
        Home(onNavigateToNearby = {  }, onNavigateToFavorites = { })
    }
}
