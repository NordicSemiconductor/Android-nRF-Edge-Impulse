package no.nordicsemi.android.ei.util

enum class Engine(val engine: String) {
    TFLITE("tflite"),
    TFLITE_EON("tflite-eon"),
    REQUIRES_RETRAIN("requiresRetrain")
}