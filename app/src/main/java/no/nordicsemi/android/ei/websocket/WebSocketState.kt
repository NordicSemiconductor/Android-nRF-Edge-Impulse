/*
 *
 *  * Copyright (c) 2022, Nordic Semiconductor
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */

package no.nordicsemi.android.ei.websocket

import okhttp3.Response

sealed class WebSocketState {
    data class Open(val response: Response): WebSocketState()
    data class Closing(val code: Int, val reason: String): WebSocketState()
    data class Closed(val code: Int, val reason: String): WebSocketState()
    data class Failed(val throwable: Throwable, val response: Response?): WebSocketState()
}
