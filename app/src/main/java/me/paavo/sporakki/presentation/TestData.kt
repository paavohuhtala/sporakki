package me.paavo.sporakki.presentation

val testStops = listOf(
    Stop(
        stopType = StopType.Tram,
        id = "baz",
        name = "Pyöveli Gunnarsonin Katu",
        distance = 4500,
        stopTimes = listOf(
            StopTime(name ="506", timeToDeparture = 8)
        )
    ),
    Stop(
        stopType = StopType.Tram,
        id = "foo",
        name = "Arabiankatu",
        distance = 50,
        stopTimes = listOf(
            StopTime(name = "6", timeToDeparture = 2, headsign = "Malmi as. via Viikinmäki"),
            StopTime(name = "8", timeToDeparture = 6, headsign = "Eiranranta via Sörnäinen (M)"),
        )
    ),
    Stop(
        stopType = StopType.Bus, id = "bar", name = "Arabia", distance = 220, stopTimes = listOf(
            StopTime(name = "74", timeToDeparture = 1),
            StopTime(name = "55", timeToDeparture = 14),
            StopTime(name = "506", timeToDeparture = 17)
        )
    ),
)