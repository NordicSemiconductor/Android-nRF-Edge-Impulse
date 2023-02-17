/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.model

/**
 * SocketToken used to open a websocket for project deployment
 *
 * @param socketToken   Token
 * @param expires       Expiration of the token
 */
data class SocketToken(val socketToken: String, val expires: String)
