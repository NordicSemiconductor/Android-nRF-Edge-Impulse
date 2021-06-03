package no.nordicsemi.android.ei.model

/**
 * Direction
 */
enum class Direction(val direction: String) {
    Receive(direction = "rx"),
    Send(direction = "tx")
}
