package no.nordicsemi.android.ei.model

/**
 * SocketToken used to open a websocket for project deployment
 *
 * @param token Token
 * @param expires Expiration of the token
 */
data class SocketToken(val token: String, val expires: String)
