package no.nordicsemi.android.ei.service.param

/**
 * Returns a socket token to open a websocket that can be used for deployment.
 * @see https://docs.edgeimpulse.com/reference#getsockettoken
 * @param projectId Project ID
 */
data class GetSocketTokenRequest(val projectId: Int)