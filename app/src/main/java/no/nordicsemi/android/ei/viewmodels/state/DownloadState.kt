package no.nordicsemi.android.ei.viewmodels.state

sealed class DownloadState {
    object Unknown : DownloadState()
    object Downloading : DownloadState()

    @Suppress("ArrayInDataClass")
    data class Saving(val fileName: String, val data: ByteArray) : DownloadState()
    object Finished : DownloadState()
}