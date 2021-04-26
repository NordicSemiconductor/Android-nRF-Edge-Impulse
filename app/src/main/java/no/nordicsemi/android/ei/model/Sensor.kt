package no.nordicsemi.android.ei.model

data class Sensor(
    val name: String,
    val maxSampleLengths: Int,
    val frequencies: List<Int>
)
