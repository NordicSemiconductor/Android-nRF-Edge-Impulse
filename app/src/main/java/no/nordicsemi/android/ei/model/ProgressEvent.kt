package no.nordicsemi.android.ei.model

/**
 * Progress event messages
 *
 * @see https://docs.edgeimpulse.com/reference#remote-management
 */
sealed class ProgressEvent {

    /**
     * Indicates that the device started sampling. Send the following message right when sampling starts:
     */
    data class Started(val sampleStarted: Boolean) : ProgressEvent()

    /**
     * If the device is done sampling, but is processing the sample before uploading,
     * such as a device preprocessing audio or signing the file. Send the following message
     * to indicate that the device is processing:
     */
    data class Processing(val sampleProcessing: Boolean) : ProgressEvent()

    /**
     * If the device is not connected directly to the internet, the daemon needs to pull the data
     * from the device over serial, which could be slow for large files. Send the following message
     * to indicate that this process started, and how far the process is along:
     */
    data class Reading(val sampleReading: Boolean, val progressPercentage: Int) : ProgressEvent()

    /**
     * Before you start the upload, send:
     */
    data class Uploading(val sampleUploading: Boolean) : ProgressEvent()

    /**
     * After sampling is completed and the file is uploaded, send:
     */
    data class Finished(val sampleFinished: Boolean) : ProgressEvent()
}
