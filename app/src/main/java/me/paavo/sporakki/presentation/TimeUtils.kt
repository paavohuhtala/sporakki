package me.paavo.sporakki.presentation

import java.time.LocalDateTime
import java.time.ZoneId

fun getHelsinkiOffset(time: LocalDateTime) =
    ZoneId.of("Europe/Helsinki").rules.getOffset(time)!!