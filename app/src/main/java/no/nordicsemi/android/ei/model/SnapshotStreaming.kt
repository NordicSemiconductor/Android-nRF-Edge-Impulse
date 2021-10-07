package no.nordicsemi.android.ei.model

/**
 * Snapshot streaming provides the user with a preview of a connected camera in the Studio.
 *
 * @see https://docs.edgeimpulse.com/reference#remote-management
 */
sealed class SnapshotStreaming {
    /**
     * When the user chooses a camera in the studio, the following message is sent
     * to the device (in CBOR or JSON depending on the hello request):
     */
    data class Start(val startSnapshot: Boolean) : SnapshotStreaming()

    /**
     * When snapshot streaming should stop, the following message is sent to the device
     * (in CBOR or JSON depending on the hello request):
     */
    data class Stop(val stopSnapshot: Boolean) : SnapshotStreaming()

    /**
     * To send a snapshot, send the following message from device.
     * Here snapshotFrame is a base64 encoded JPG file:
     */
    data class Send(val snapshotFrame: String) : SnapshotStreaming()
}
