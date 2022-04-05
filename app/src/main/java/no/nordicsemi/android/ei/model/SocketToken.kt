package no.nordicsemi.android.ei.model

/**
 * SocketToken used to open a websocket for project deployment
 *
 * @param socketToken   Token
 * @param expires       Expiration of the token
 */
data class SocketToken(val socketToken: String, val expires: String)
