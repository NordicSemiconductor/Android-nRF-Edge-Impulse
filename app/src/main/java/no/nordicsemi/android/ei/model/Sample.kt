package no.nordicsemi.android.ei.model

data class Sample(
    val id: Int,
    val filename: String,
    val signatureValidate: Boolean,
    val signatureMethod: String,
    val signatureKey: String,
    val created: String,
    val category: String,
    val coldstorageFilename: String,
    val label: String,
    val intervalMs: Number,
    val frequency: Number,
    val deviceName: String,
    val deviceType: String,
    val sensors: List<Sensor>,
    val valuesCount: Int,
    val totalLengthMs: Number,
    val added: String,
    val boundingBoxes: List<BoundingBox>
) {
    inner class Sensor(
        val name: String,
        val units: String
    )

    inner class BoundingBox(
        val label: String,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )
}
