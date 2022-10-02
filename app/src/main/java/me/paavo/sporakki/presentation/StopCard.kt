package me.paavo.sporakki.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text

@Composable
fun StopCard(stop: Stop) {
    val config = stop.stopType.config

    Card(
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = config.color,
            endBackgroundColor = config.color,
        ),
        contentColor = Color.White,
        onClick = {}
    ) {
        val title = if (stop.platform != null) {
            "${stop.name} (${stop.platform})"
        } else {
            stop.name
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = title)
                    Text(
                        fontSize = 12.sp,
                        text = "${stop.distance}\u00A0m"
                    )
                }
                Icon(
                    imageVector = config.icon,
                    contentDescription = config.name,
                    modifier = Modifier
                        .size(24.dp)
                        .wrapContentSize(align = Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                stop.stopTimes.forEachIndexed { index, stopTime ->
                    StopTimeItem(stopTime, showHeadsign = index == 0)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun StopTimeItem(it: StopTime, showHeadsign: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(it.name, fontSize = 12.sp)
            Text("${it.timeToDeparture} min", fontSize = 12.sp)
        }

        if (showHeadsign && it.headsign != null) {
            Text(
                text = it.headsign,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
@Preview(device = Devices.WEAR_OS_RECT, showSystemUi = true)
fun StopCardPreview() {
    Column {
        StopCard(
            testStops[0]
        )
    }
}