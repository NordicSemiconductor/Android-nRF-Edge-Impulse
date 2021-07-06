package no.nordicsemi.android.ei.service.param

import no.nordicsemi.android.ei.model.SocketToken

/**
 * Response to GetSocketToken request
 * @see https://docs.edgeimpulse.com/reference#getsockettoken
 */
class GetSocketTokenResponse(val success: Boolean, val error: String, val token: SocketToken)